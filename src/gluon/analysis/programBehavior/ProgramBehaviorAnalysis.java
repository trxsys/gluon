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
import gluon.grammar.LexicalElement;
import gluon.grammar.NonTerminal;

import gluon.PointsToInformation;

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
import soot.jimple.internal.JReturnStmt;
import soot.jimple.internal.JReturnVoidStmt;

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

public class ProgramBehaviorAnalysis
{
    private static final boolean DEBUG=false;

    private SootClass module; // module under analysis
    private SootMethod entryMethod;
    private AllocNode allocSite; // allocation site of the "object" under analysis

    private Cfg grammar;
    
    private Set<Unit> visited;
    
    private NonTerminalAliasCreator aliasCreator;
    
    private Queue<SootMethod> methodQueue; // queue of methods to analyse
    private Set<SootMethod> enqueuedMethods;
    
    public ProgramBehaviorAnalysis(SootMethod method, SootClass modClass,
                                   AllocNode aSite)
    {
        entryMethod=method;
        module=modClass;
        allocSite=aSite;

        grammar=new Cfg();
        
        visited=null;
        
        aliasCreator=new NonTerminalAliasCreator();
        
        methodQueue=null;
        enqueuedMethods=null;
    }
    
    private void dprintln(String s)
    {
        if (DEBUG)
            System.out.println(this.getClass().getSimpleName()+": "+s);
    }
    
    private String alias(Object o)
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
                assert false : "Do we need to handle this?";

            assert allocSites != null;

            if (!hasModule)
                return ModCall.NEVER;

            return allocSites.size() == 1 ? ModCall.ALWAYS : ModCall.SOMETIMES;
        }

        return ModCall.NEVER;
    }
    
    private void analyzeUnit(SootMethod method, Unit unit, UnitGraph cfg)
    {
        LexicalElement prodBodyPrefix=null;
        boolean addProdSkipPrefix=false;
        
        if (visited.contains(unit))
            return; // Unit already taken care of

        gluon.profiling.Profiling.inc("cfg-nodes."
                                  +entryMethod.getDeclaringClass().getShortName()
                                  +"."+entryMethod.getName());
        gluon.profiling.Profiling.inc("final:cfg-nodes");

        
        visited.add(unit);
        
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
                    prodBodyPrefix=new PPNonTerminal(alias(calledMethod),method); 
                    
                    if (!enqueuedMethods.contains(calledMethod))
                    {
                        methodQueue.add(calledMethod);
                        enqueuedMethods.add(calledMethod);
                    }
                }
                break;
            }
            case SOMETIMES: addProdSkipPrefix=true; /* fall through */
            case ALWAYS: prodBodyPrefix=new PPTerminal(calledMethod,unit); break;
            }
        }
        
        assert addProdSkipPrefix ? prodBodyPrefix != null : true;

        PPNonTerminal prodHead=new PPNonTerminal(alias(unit),method);

        for (Unit succ: cfg.getSuccsOf(unit))
        {
            PPNonTerminal succNonTerm=new PPNonTerminal(alias(succ),method);

            if (prodBodyPrefix == null || addProdSkipPrefix)
                addUnitToLexicalElement(unit,succNonTerm,method);

            if (prodBodyPrefix != null)
                addUnitToTwoLexicalElements(unit,prodBodyPrefix,succNonTerm,
                                            method);
        }

        if (cfg.getSuccsOf(unit).size() == 0)
        {
            assert prodBodyPrefix == null : "If we have no successors we "
                +"should be a return statement, and therefore have no method "
                +"calls";

            addUnitToEmptyProduction(unit,method);
        }

        for (Unit succ: cfg.getSuccsOf(unit))
            analyzeUnit(method,succ,cfg);
    }

    private void addUnitToTwoLexicalElements(Unit unit,
                                             LexicalElement body1,
                                             LexicalElement body2,
                                             SootMethod method)
    {
        PPNonTerminal head=new PPNonTerminal(alias(unit),method);
        Production production=new Production(head);
        
        production.appendToBody(body1);
        production.appendToBody(body2);
        
        grammar.addProduction(production);        
    }

    private void addUnitToLexicalElement(Unit unit, LexicalElement body,
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
        LexicalElement body=new PPNonTerminal(alias(entryPoint),method);
        
        production.appendToBody(body);
        
        grammar.addProduction(production);
    }
    
    // Adds the patterns of method to grammar
    private void analyzeMethod(SootMethod method)
    {
        UnitGraph cfg;
        
        // dprintln("Analyzing method "+method);
        
        assert method.hasActiveBody() : "No active body";
        
        cfg = new BriefUnitGraph(method.getActiveBody());
        
        assert cfg.getHeads().size() != 0
            : "There are no entry points of the cfg of method "+method;
        
        for (Unit head: cfg.getHeads())
        {
            addMethodToHeadProduction(method,head);
            analyzeUnit(method,head,cfg);
        }
    }
    
    private void analyzeReachableMethods(SootMethod entryMethod)
    {
        visited=new HashSet<Unit>();
        
        methodQueue=new LinkedList<SootMethod>();
        enqueuedMethods=new HashSet<SootMethod>();
        
        methodQueue.add(entryMethod);
        enqueuedMethods.add(entryMethod);
        
        while (methodQueue.size() > 0)
        {
            SootMethod method=methodQueue.poll();
            
            analyzeMethod(method);
        }
        
        enqueuedMethods=null;
        methodQueue=null;
        
        visited=null;
    }
    
    private void addNewStart()
    {
        NonTerminal oldStart=grammar.getStart();
        NonTerminal newStart=new PPNonTerminal(oldStart.toString()+'\'',
                                               entryMethod);
        Production prod=new Production(newStart);

        prod.appendToBody(oldStart);

        grammar.addProduction(prod);
        grammar.setStart(newStart);
    }

    public void analyze()
    {
        analyzeReachableMethods(entryMethod);

        grammar.setStart(new PPNonTerminal(alias(entryMethod),entryMethod));

        if (!gluon.Main.NO_GRAMMAR_OPTIMIZE)
        {
            gluon.profiling.Timer.start("final:analysis-behavior-grammar-opt");
            grammar.optimize();
            gluon.profiling.Timer.stop("final:analysis-behavior-grammar-opt");
        }

        gluon.profiling.Timer.start("analysis-behavior-grammar-add-subwords");
        grammar.subwordClosure();
        gluon.profiling.Timer.stop("analysis-behavior-grammar-add-subwords");

        if (!gluon.Main.NO_GRAMMAR_OPTIMIZE)
        {
            gluon.profiling.Timer.start("final:analysis-behavior-grammar-opt");
            grammar.optimize();
            gluon.profiling.Timer.stop("final:analysis-behavior-grammar-opt");
        }

        addNewStart();

        dprintln("Grammar: "+grammar);

        assert grammar.hasUniqueStart();
    }
    
    public Cfg getGrammar()
    {
        assert grammar.getStart() != null : "analyze() must first be called";

        return grammar;
    }
}
