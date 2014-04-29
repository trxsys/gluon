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

package gluon.analysis.programBehavior;

/* This analysis creates a grammar that describes the access patterns to
 * to the module under analysis.
 *
 * The grammar is extracted from the flow control graph. It's terminals are
 * the methods of the module we are analyzing.
 */

import gluon.grammar.Cfg;
import gluon.grammar.Production;
import gluon.grammar.Symbol;
import gluon.grammar.NonTerminal;

import gluon.analysis.pointsTo.PointsToInformation;
import gluon.analysis.monitor.MonitorAnalysis;

import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Queue;
import java.util.LinkedList;
import java.util.List;
import java.util.Collection;

import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.SootField;
import soot.Unit;
import soot.Value;
import soot.Local;

import soot.toolkits.graph.UnitGraph;
import soot.toolkits.graph.BriefUnitGraph;

import soot.jimple.Stmt;
import soot.jimple.InvokeExpr;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.StaticInvokeExpr;
import soot.jimple.EnterMonitorStmt;
import soot.jimple.ExitMonitorStmt;

import soot.jimple.spark.pag.AllocNode;

class NonTerminalAliasCreator
{
    private static final char[] RADIX_CHARS
        ="ABCDEFGHIJKLMNOPQSRTUVWXYZ".toCharArray();
    private static final int RADIX=RADIX_CHARS.length;

    private int counter;
    private Map<Object,Integer> map;

    public NonTerminalAliasCreator()
    {
        counter=0;
        map=new HashMap<Object,Integer>();
    }

    private static String intToString(int n)
    {
        String r="";

        do
        {
            r=RADIX_CHARS[n%RADIX]+r;
            n/=RADIX;
        } while (n > 0);

        return r;
    }

    public String makeAlias(Object o)
    {
        if (map.containsKey(o))
            return intToString(map.get(o));

        map.put(o,counter);

        return intToString(counter++);
    }

    public String freshAlias()
    {
        return intToString(counter++);
    }
}

public abstract class BehaviorAnalysis
{
    private static final boolean DEBUG=false;

    private SootClass module; /* module under analysis */
    /* Allocation site of the "object" under analysis.
     * null if we are performing the analysis for a static module
     */
    private AllocNode allocSite;
    protected Cfg grammar;

    protected Set<Unit> visited;
    private NonTerminalAliasCreator aliasCreator;

    private MonitorAnalysis monitorAnalysis;

    private boolean conservativePointsTo;

    public BehaviorAnalysis(SootClass modClass, AllocNode aSite)
    {
        module=modClass;
        allocSite=aSite;

        grammar=new Cfg();

        monitorAnalysis=null;

        visited=null;

        conservativePointsTo=false;

        aliasCreator=new NonTerminalAliasCreator();
    }

    /* For conservative points-to analisys */
    public BehaviorAnalysis(SootClass modClass)
    {
        this(modClass,null);
        conservativePointsTo=true;
    }

    protected void dprintln(String s)
    {
        if (DEBUG)
            System.out.println(this.getClass().getSimpleName()+": "+s);
    }

    /* Set the grammar generation to handle synchronized blocks.
     */
    public void setSynchMode(MonitorAnalysis monAnalysis)
    {
        monitorAnalysis=monAnalysis;
    }

    public boolean isSynchMode()
    {
        return monitorAnalysis != null;
    }

    protected String alias(Object o)
    {
        return aliasCreator.makeAlias(o);
    }

    private enum ModCall { NEVER, SOMETIMES, ALWAYS }

    private ModCall invokeCallsModule(InvokeExpr expr)
    {
        SootMethod calledMethod=expr.getMethod();

        if (calledMethod.isConstructor()
            || calledMethod.isPrivate())
            return ModCall.NEVER;

        if (conservativePointsTo
            && expr instanceof InstanceInvokeExpr)
        {
            Value obj=((InstanceInvokeExpr)expr).getBase();

            return obj.getType().equals(module.getType()) ? ModCall.SOMETIMES
                                                          : ModCall.NEVER;
        }

        /* We only consider "modules" as instances from objects.
         * So a call to a static method of a class is not considered
         * a usage of a module.
         */
        if (expr instanceof InstanceInvokeExpr)
        {
            Value obj=((InstanceInvokeExpr)expr).getBase();
            boolean hasModule=false;
            Collection<AllocNode> allocSites=null;

            /* The object being called may be a local variable or a field
             */
            if (obj instanceof Local)
            {
                Local l=(Local)obj;

                allocSites=PointsToInformation.getReachableAllocSites(l);

                for (AllocNode ac: allocSites)
                    if (ac.equals(allocSite))
                        hasModule=true;
            }
            else if (obj instanceof SootField)
            {
                /* This might not need to be handled since it seems that
                 * in jimple/shimple the fields are accessed through JimpleLocals.
                 */
                assert false : "Do we need to handle this?";
            }

            assert allocSites != null;

            if (!hasModule)
                return ModCall.NEVER;

            return allocSites.size() == 1 ? ModCall.ALWAYS : ModCall.SOMETIMES;
        }
        else if (expr instanceof StaticInvokeExpr
                 && allocSite == null)
        {
            SootClass base=((StaticInvokeExpr)expr).getMethod().getDeclaringClass();

            return base.equals(module) ? ModCall.ALWAYS : ModCall.NEVER;
        }

        return ModCall.NEVER;
    }

    protected abstract void foundMethodCall(SootMethod method);

    protected abstract boolean ignoreMethodCall(SootMethod method);

    private void analyzeSuccessors(SootMethod method, UnitGraph cfg, Unit unit)
    {
        for (Unit succ: cfg.getSuccsOf(unit))
            analyzeUnit(method,cfg,succ);
    }

    /* If we have the following CFG:
     *
     *        A  monstart
     *              ↓
     *           B  b
     *              ↓
     *              ⋮
     *              ↓
     *        C  monend
     *              ↓
     *           D  d
     *
     * We will generate:
     *
     *   A  → A@ D
     *   A@ → B
     */
    private void analyzeUnitEnterMonitor(SootMethod method, UnitGraph cfg,
                                         EnterMonitorStmt unit)
    {
        PPNonTerminal synchNonTerm=new PPNonTerminal(alias(unit)+"@",method);

        synchNonTerm.setSynchBlock();
        synchNonTerm.setNoRemove();

        /* Add A  → A@ D */
        for (ExitMonitorStmt exitmon: monitorAnalysis.getExitMonitor(unit))
        {
            assert cfg.getSuccsOf(exitmon).size() > 0;

            for (Unit exitmonsucc: cfg.getSuccsOf(exitmon))
            {
                PPNonTerminal head=new PPNonTerminal(alias(unit),method);
                Production production=new Production(head);

                production.appendToBody(synchNonTerm);

                production.appendToBody(new PPNonTerminal(alias(exitmonsucc),
                                                          method));

                grammar.addProduction(production);
            }
        }

        /* Add A@ → B */
        for (Unit succ: cfg.getSuccsOf(unit))
        {
            Production production=new Production(synchNonTerm);

            production.appendToBody(new PPNonTerminal(alias(succ),method));

            grammar.addProduction(production);
        }

        analyzeSuccessors(method,cfg,unit);
    }

    /* An exitmonitor just reduce to ε. See analyzeUnitEnterMonitor() comment.
     */
    private void analyzeUnitExitMonitor(SootMethod method, UnitGraph cfg,
                                        ExitMonitorStmt unit)
    {
        addUnitToEmptyProduction(unit,method);

        analyzeSuccessors(method,cfg,unit);
    }

    private void analyzeUnit(SootMethod method, UnitGraph cfg, Unit unit)
    {
        Symbol prodBodyPrefix=null;
        boolean addProdSkipPrefix=false;

        if (visited.contains(unit))
            return; /* Unit already taken care of */

        gluon.profiling.Profiling.inc("final:cfg-nodes");

        visited.add(unit);

        if (isSynchMode())
        {
            if (unit instanceof EnterMonitorStmt)
            {
                analyzeUnitEnterMonitor(method,cfg,(EnterMonitorStmt)unit);
                return;
            }
            else if (unit instanceof ExitMonitorStmt)
            {
                analyzeUnitExitMonitor(method,cfg,(ExitMonitorStmt)unit);
                return;
            }
        }

        if (((Stmt)unit).containsInvokeExpr())
        {
            InvokeExpr expr=((Stmt)unit).getInvokeExpr();
            SootMethod calledMethod=expr.getMethod();

            switch (invokeCallsModule(expr))
            {
            case NEVER:
            {
                if (calledMethod.hasActiveBody()
                    && (!calledMethod.isJavaLibraryMethod()
                        || gluon.Main.WITH_JAVA_LIB))
                {
                    foundMethodCall(calledMethod);

                    if (!ignoreMethodCall(calledMethod))
                        prodBodyPrefix=new PPNonTerminal(alias(calledMethod),method);
                }
                break;
            }
            case SOMETIMES: addProdSkipPrefix=true; /* fall through */
            case ALWAYS: prodBodyPrefix=new PPTerminal(calledMethod,unit,method); break;
            }
        }

        assert addProdSkipPrefix ? prodBodyPrefix != null : true;

        PPNonTerminal prodHead=new PPNonTerminal(alias(unit),method);

        for (Unit succ: cfg.getSuccsOf(unit))
        {
            PPNonTerminal succNonTerm=new PPNonTerminal(alias(succ),method);

            if (prodBodyPrefix == null || addProdSkipPrefix)
                addUnitToSymbol(unit,succNonTerm,method);

            if (prodBodyPrefix != null)
                addUnitToTwoSymbols(unit,prodBodyPrefix,succNonTerm,
                                            method);
        }

        if (cfg.getSuccsOf(unit).size() == 0)
        {
            assert prodBodyPrefix == null : "If we have no successors we "
                +"should be a return statement, and therefore have no method "
                +"calls";

            addUnitToEmptyProduction(unit,method);
        }

        analyzeSuccessors(method,cfg,unit);
    }

    private void addUnitToTwoSymbols(Unit unit,
                                     Symbol body1,
                                     Symbol body2,
                                     SootMethod method)
    {
        PPNonTerminal head=new PPNonTerminal(alias(unit),method);
        Production production=new Production(head);

        production.appendToBody(body1);
        production.appendToBody(body2);

        grammar.addProduction(production);
    }

    private void addUnitToSymbol(Unit unit, Symbol body,
                                 SootMethod method)
    {
        PPNonTerminal head=new PPNonTerminal(alias(unit),method);
        Production production=new Production(head);

        production.appendToBody(body);

        grammar.addProduction(production);
    }

    private void addUnitToEmptyProduction(Unit unit, SootMethod method)
    {
        PPNonTerminal head=new PPNonTerminal(alias(unit),method);
        Production production=new Production(head);

        grammar.addProduction(production);
    }

    private void addMethodToHeadProduction(SootMethod method,
                                           Unit entryPoint)
    {
        PPNonTerminal head=new PPNonTerminal(alias(method),method);
        Production production=new Production(head);
        Symbol body=new PPNonTerminal(alias(entryPoint),method);

        head.setNoRemove();

        production.appendToBody(body);

        grammar.addProduction(production);
    }

    /* Adds the patterns of method to grammar
     */
    protected PPNonTerminal analyzeMethod(SootMethod method)
    {
        UnitGraph cfg;

        dprintln("Analyzing method "+method);

        assert method.hasActiveBody() : "No active body";

        cfg=new BriefUnitGraph(method.getActiveBody());

        assert cfg.getHeads().size() != 0
            : "There are no entry points of the cfg of method "+method;

        for (Unit head: cfg.getHeads())
        {
            addMethodToHeadProduction(method,head);
            analyzeUnit(method,cfg,head);
        }

        return new PPNonTerminal(alias(method),method);
    }

    public Cfg getGrammar()
    {
        assert grammar.getStart() != null : "analyze() must first be called";

        return grammar;
    }

    protected void addNewStart()
    {
        NonTerminal oldStart=grammar.getStart();
        NonTerminal newStart=new PPNonTerminal(oldStart.toString()+'\'',
                                               null);
        Production prod=new Production(newStart);

        prod.appendToBody(oldStart);

        grammar.addProduction(prod);
        grammar.setStart(newStart);
    }

    public abstract void analyze();
}
