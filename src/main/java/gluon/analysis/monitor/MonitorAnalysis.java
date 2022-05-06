/* This file is part of Gluon.
 *
 * Gluon is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Gluon is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Gluon.  If not, see <http://www.gnu.org/licenses/>.
 */

package gluon.analysis.monitor;

import gluon.Main;
import soot.*;
import soot.jimple.EnterMonitorStmt;
import soot.jimple.ExitMonitorStmt;
import soot.jimple.InstanceInvokeExpr;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.UnitGraph;

import java.io.File;
import java.io.FileOutputStream;
import java.util.*;

class StackNode<E>
{
    private StackNode<E> parent;
    private E element;

    public StackNode(StackNode<E> p, E elem)
    {
        parent=p;
        element=elem;
    }

    public StackNode<E> parent()
    {
        return parent;
    }

    public E element()
    {
        return element;
    }
}

public class MonitorAnalysis
{
    private static final String FILE_THRESHOLD_NAME = "synchronizedBlocksPercentage.txt";

    private final Scene scene;
    private Map<EnterMonitorStmt,Collection<ExitMonitorStmt>> exitMon;
    private Collection<SynchedSection> synchedSections;
    private Set<Unit> visited;
    /**
     * File with all the percentage of times a sequence (executed at least once atomically) is executed atomically
     */
    private File syncBlocksFile;
    private FileOutputStream outputStreamFile;
    /**
     * For each sequence of methods that were previously stated as atomically executed at least once, we are going to see
     * how many times in total they are executed, and then we are able to compare the times they are executed atomically or not
     */
    private Map<List<SootMethod>, Integer> totalCounter;
    /**
     * For each sequence of methods that were previously stated as atomically executed at least once, we are going to see
     * how many times they are executed in each module
     */
    private Map<String,Map<List<SootMethod>,Integer>> totalCounterByModule;
    /**
     * synch -> type -> (module name) -> calls
     */
    private Map<Object,Map<Type,Map<String,List<SootMethod>>>> synchCalls;

    public MonitorAnalysis(Scene s)
    {
        scene=s;
        visited=null;
        exitMon=new HashMap<EnterMonitorStmt,Collection<ExitMonitorStmt>>();
        synchedSections=new LinkedList<SynchedSection>();
        syncBlocksFile=null;
        outputStreamFile=null;
        totalCounter = new HashMap<List<SootMethod>, Integer>();
        synchCalls=new HashMap<Object,Map<Type,Map<String,List<SootMethod>>>>();

        if(gluon.Main.SEARCH_BY.equals(gluon.Main.THRESHOLD)) {
            initializeFile();
        }
        else if(gluon.Main.SEARCH_BY.equals(gluon.Main.MODULE)) {
            totalCounterByModule = new HashMap<String,Map<List<SootMethod>,Integer>>();
        }
    }

    private void initializeFile() {
        try {
            syncBlocksFile = new File(FILE_THRESHOLD_NAME);
            if(!syncBlocksFile.isFile()) {
                syncBlocksFile.createNewFile();
            }
            outputStreamFile = new FileOutputStream(FILE_THRESHOLD_NAME);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    private void analyzeUnit(String module,SootMethod method, Unit unit, UnitGraph cfg,
                             StackNode<EnterMonitorStmt> stack)
    {
        if (visited.contains(unit))
            return; /* Unit already taken care of */

        visited.add(unit);

        if (unit instanceof EnterMonitorStmt)
            stack=new StackNode<EnterMonitorStmt>(stack,(EnterMonitorStmt)unit);
        else if (unit instanceof ExitMonitorStmt
                && stack != null)
        {
            EnterMonitorStmt enter;

            enter=stack.element();

            if (!exitMon.containsKey(enter))
                exitMon.put(enter,new ArrayList<ExitMonitorStmt>(8));

            exitMon.get(enter).add((ExitMonitorStmt)unit);

            stack=stack.parent();
        }

        if (stack != null)
            thisIsSynch(module,unit,stack.element());

        for (Unit succ: cfg.getSuccsOf(unit))
            analyzeUnit(module,method,succ,cfg,stack);
    }

    public Collection<ExitMonitorStmt> getExitMonitor(EnterMonitorStmt enterMon)
    {
        assert exitMon.containsKey(enterMon);

        return exitMon.get(enterMon);
    }

    public Collection<SynchedSection> getSynchedSections()
    {
        /* return set of (entry unit, unitgraph, set of exit units) */

        // TODO
        return null;
    }

    private void thisIsSynch(String module, Unit unit, Object synchContext)
    {
        soot.jimple.InstanceInvokeExpr invoke;
        soot.jimple.InvokeExpr expr;
        Type t;
        SootMethod m;

        if (!((soot.jimple.Stmt)unit).containsInvokeExpr())
            return;

        expr=((soot.jimple.Stmt)unit).getInvokeExpr();

        if (!(expr instanceof InstanceInvokeExpr))
            return;

        invoke=(InstanceInvokeExpr)expr;

        t=invoke.getBase().getType();
        m=invoke.getMethod();

        if (m.isConstructor() || m.isPrivate())
            return;

        if (!synchCalls.containsKey(synchContext))
            synchCalls.put(synchContext,
                    new HashMap<Type, Map<String,List<SootMethod>>>());

        Map<Type, Map<String, List<SootMethod>>> auxMap = synchCalls.get(synchContext);
        if (!auxMap.containsKey(t))
            auxMap.put(t, new HashMap<String,List<SootMethod>>());

        Map<String, List<SootMethod>> auxMap2 = auxMap.get(t);
        if (!auxMap2.containsKey(module))
            auxMap2.put(module, new LinkedList<SootMethod>());

        auxMap2.get(module).add(m);
    }

    /**
     * See if the sequence passed by parameter is contained in the other (in the correct order) and how many times
     * @param totalSootMethods - methods that were at least once executed atomically
     * @param seq - sequence of methods we want to see if contained in each of the others (in the correct order)
     */
    private void subArrayCounter(String module, SootMethod[] totalSootMethods, List<SootMethod> seq) {
        SootMethod[] seqArray = seq.toArray(new SootMethod[seq.size()]);
        int totalLength = totalSootMethods.length;
        int syncLength = seqArray.length;
        int i = 0, j = 0;

        // Traverse both arrays simultaneously
        while (i < totalLength && j < syncLength) {
            if (totalSootMethods[i] == seqArray[j]) {
                i++;
                j++;

                if (j == syncLength) {
                    if((gluon.Main.SEARCH_BY).equals(gluon.Main.MODULE)) {
                        if(!totalCounterByModule.containsKey(module))
                            totalCounterByModule.put(module, new HashMap<List<SootMethod>,Integer>());

                        incSubArrayCounter(totalCounterByModule.get(module), seq);
                    }
                    else {
                        incSubArrayCounter(totalCounter, seq);
                    }

                    i=i-j+1;
                    j=0;
                }
            }
            else {
                i=i-j+1;
                j=0;
            }
        }
    }

    // Increment the map value by one
    private void incSubArrayCounter(Map<List<SootMethod>, Integer> map, List<SootMethod> seq) {
        if (!map.containsKey(seq))
            map.put(seq, 0);

        map.put(seq, map.get(seq) + 1);
    }

    private void printSynchCalls()
    {
        final boolean SCRIPT=true;

        Map<List<SootMethod>,Integer> m = new HashMap<List<SootMethod>,Integer>();
        Map<String,Map<List<SootMethod>,Integer>> mModule = new HashMap<String,Map<List<SootMethod>,Integer>>();
        Map<List<SootMethod>,Integer> auxMap;

        for (Object synchContext: synchCalls.keySet()) {
            for (Type t: synchCalls.get(synchContext).keySet()) {
                for (String module: synchCalls.get(synchContext).get(t).keySet()) {
                    List<SootMethod> seq=synchCalls.get(synchContext).get(t).get(module);

                    if (seq.size() <= 1)
                        continue;

                    if((gluon.Main.SEARCH_BY).equals(gluon.Main.MODULE)) {
                        if (!mModule.containsKey(module))
                            mModule.put(module, new HashMap<List<SootMethod>, Integer>());

                        auxMap = mModule.get(module);
                        if (!auxMap.containsKey(seq))
                            auxMap.put(seq, 0);

                        auxMap.put(seq,auxMap.get(seq)+1);
                    }
                    if (!m.containsKey(seq))
                        m.put(seq,0);

                    m.put(seq,m.get(seq)+1);
                }
            }
        }

        if(!(gluon.Main.SEARCH_BY).equals(gluon.Main.NUMBER_ATOMIC_BLOCKS)) {
            for (SootClass sC: scene.getClasses()) {
                if (gluon.Main.WITH_JAVA_LIB || !sC.isJavaLibraryClass()) {
                    for (SootMethod sM: sC.getMethods()) {
                        List<SootMethod> listOfSootMethods = new LinkedList<>();
                        if (sM.hasActiveBody()) {
                            for (Unit u : sM.getActiveBody().getUnits()) {
                                soot.jimple.InstanceInvokeExpr invoke;
                                soot.jimple.InvokeExpr expr;
                                Type t;
                                SootMethod mS;

                                if (!((soot.jimple.Stmt) u).containsInvokeExpr())
                                    continue;

                                expr = ((soot.jimple.Stmt) u).getInvokeExpr();

                                if (!(expr instanceof InstanceInvokeExpr))
                                    continue;

                                invoke = (InstanceInvokeExpr) expr;
                                mS = invoke.getMethod();

                                if (mS.isConstructor())
                                    continue;

                                listOfSootMethods.add(mS);
                            }
                            if(!listOfSootMethods.isEmpty()){
                                // After storing the methods that invoked in a method, we are going to see if they are contained in the sequences of methods that need to be executed atomically
                                for (List<SootMethod> seq : m.keySet()) {
                                    if (listOfSootMethods.containsAll(seq)) {
                                        // Despite containing the methods it is necessary to see how many times they are executed and if they are called in the correct order
                                        subArrayCounter(sC.getPackageName(), listOfSootMethods.toArray(new SootMethod[listOfSootMethods.size()]), seq);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if(gluon.Main.SEARCH_BY.equals(gluon.Main.THRESHOLD)) {
            try {
                String headerFile = "The chosen form of generation of contracts is based on the need of having a sequence executed atomically more than "
                        + gluon.Main.RANGE_THRESHOLD + "% of times.\n\n"
                        + "This file contains all the sequence of methods that were at least once executed atomically in this program and their atomic percentage.\n"
                        + "Useful for a more detailed analysis.\n\n\n";
                outputStreamFile.write(headerFile.getBytes());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (SCRIPT)
        {
            System.err.println("#! /bin/bash");
            System.err.println();
            System.err.println("cd ..");
            System.err.println();
            System.err.println("rm -f tests_out");
            System.err.println();
        }

        for (List<SootMethod> seq: m.keySet())
        {
            int count = m.get(seq);
            SootClass c = seq.get(0).getDeclaringClass();

            if((gluon.Main.SEARCH_BY).equals(gluon.Main.NUMBER_ATOMIC_BLOCKS)) {
                // If a given sequence is executed atomically a number of times lower than what is defined, we ignore it
                if (count < gluon.Main.RANGE_NUMBER_BLOCKS)
                    continue;
            }
            else if((gluon.Main.SEARCH_BY).equals(gluon.Main.THRESHOLD)) {
                int totalCount = totalCounter.get(seq);
                double res = ((double)count/ totalCount);

                // Body of the syncBlocksFile
                String bodyFile = "Class "+c.getName()+": counter: "+res+" -> result: "+count+" / "+totalCount+".\n"
                        + "     Synchronized methods: "+seq+"\n\n";
                try {
                    outputStreamFile.write(bodyFile.getBytes());
                }
                catch(Exception e) {
                    e.printStackTrace();
                }

                // If the percentage of times this sequence is executed atomically is lower than the one defined, we ignore it
                if( res < Main.RANGE_THRESHOLD) {
                    continue;
                }
            }
            else /*if((gluon.Main.SEARCH_BY).equals(gluon.Main.MODULE))*/ {
                boolean hasZeroAtomExec = false, hasMaxAtomExec = false;
                Map<List<SootMethod>,Integer> auxM, auxTotal;

                for (String module : totalCounterByModule.keySet()) {
                    if(hasMaxAtomExec && hasZeroAtomExec)
                        break;

                    auxTotal = totalCounterByModule.get(module);
                    if(!mModule.containsKey(module)) {
                        hasZeroAtomExec = true;
                        continue;
                    }
                    auxM = mModule.get(module);
                    if(!auxM.containsKey(seq)) {
                        hasZeroAtomExec = true;
                        continue;
                    }

                    if(auxM.get(seq).intValue() == auxTotal.get(seq)) {
                        hasMaxAtomExec = true;
                    }
                }

                // This sequence does not have a module where it is always executed atomically and other were it
                // is never executed atomically (signal of forgetfulness of the programmer)
                if(!hasMaxAtomExec || !hasZeroAtomExec)
                    continue;
            }

            if (SCRIPT)
            {
                int i;

                System.err.println("./gluon.sh --timeout 5 -t -p -s -y -r "
                        +"--classpath ../cassandra-2.0.9/build/test/classes:../cassandra-2.0.9/build/classes/main:../cassandra-2.0.9/build/classes/stress \\");
                System.err.println("    --module "+c.getName()+" \\");
                System.err.print("    --contract \"");

                i=0;
                for (SootMethod method: seq)
                    System.err.print((i++ > 0 ? " " : "")+method.getName());

                System.err.println("\" \\");
                System.err.println("    org.apache.cassandra.stress.StressServer "
                        +">> tests_out");
                System.err.println();
            }
            else
            {
                System.err.print(count+" "+c.getName()+":   ");

                for (SootMethod method: seq)
                    System.err.print(" "+method.getName());

                System.err.println();
            }
        }

        if(gluon.Main.SEARCH_BY.equals(gluon.Main.THRESHOLD)) {
            try {
                outputStreamFile.close();
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void analyze()
    {
        visited=new HashSet<Unit>();

        for (SootClass c: scene.getClasses())
            if (!c.isJavaLibraryClass()
                    || gluon.Main.WITH_JAVA_LIB)
                for (SootMethod m: c.getMethods())
                {
                    UnitGraph cfg;

                    if (!m.hasActiveBody())
                        continue;

                    cfg=new BriefUnitGraph(m.getActiveBody());

                    if (m.isSynchronized())
                        for (Unit head: cfg.getHeads())
                        {
                            SynchedSection synchSect;

                            synchSect=new SynchedSection(head,cfg,cfg.getTails());
                            synchedSections.add(synchSect);
                        }

                    for (Unit head: cfg.getHeads())
                        analyzeUnit(c.getPackageName(),m,head,cfg,null);
                }

        // private Map<EnterMonitorStmt,Collection<ExitMonitorStmt>> exitMon;


        for (EnterMonitorStmt entMon: exitMon.keySet())
        {
            SynchedSection synchSect;
            List<Unit> exits=new LinkedList<Unit>();

            exits.addAll(exitMon.get(entMon));

            // TODO: null
            synchSect=new SynchedSection(entMon,null,exits);
            synchedSections.add(synchSect);
        }


        // TODO
        if (true)
        {
            printSynchCalls();
            System.exit(0);
        }

        visited=null;
        synchCalls=null;
    }
}
