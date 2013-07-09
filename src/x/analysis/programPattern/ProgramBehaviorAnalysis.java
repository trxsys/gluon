package x.analysis.programBehavior;

/* This analysis creates a grammar that describes the access patterns to
 * to the module under analysis.
 *
 * The grammar is extracted from the flow control graph. It's terminals are
 * the methods of the module we are analyzing. 
 */

import x.cfg.Cfg;
import x.cfg.Production;
import x.cfg.LexicalElement;
import x.cfg.NonTerminal;

import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Queue;
import java.util.LinkedList;
import java.util.List;

import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;

import soot.toolkits.graph.UnitGraph;
import soot.toolkits.graph.BriefUnitGraph;

import soot.jimple.Stmt;
import soot.jimple.InvokeExpr;
import soot.jimple.internal.JReturnStmt;
import soot.jimple.internal.JReturnVoidStmt;

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
        
        // System.out.println(o+" = "+intToString(counter));

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
    
    private SootMethod entryMethod;
    private Cfg grammar;
    
    private SootClass module; // module under analysis
    
    private Set<Unit> visited;
    
    private NonTerminalAliasCreator aliasCreator;
    
    private Queue<SootMethod> methodQueue; // queue of methods to analyse
    private Set<SootMethod> enqueuedMethods;
    
    public ProgramBehaviorAnalysis(SootMethod method, SootClass modClass)
    {
        entryMethod=method;
        module=modClass;

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
    
    private void analyzeUnit(SootMethod method, Unit unit, UnitGraph cfg)
    {
        LexicalElement prodBodyPrefix=null;
        
        if (visited.contains(unit))
            return; // Unit already taken care of
        
        visited.add(unit);
        
        if (((Stmt)unit).containsInvokeExpr()) 
        {
            InvokeExpr expr=((Stmt)unit).getInvokeExpr();
            SootMethod calledMethod=expr.getMethod();
            boolean isTargetModule
                =calledMethod.getDeclaringClass().equals(module);
            
            if (isTargetModule
                && !calledMethod.isConstructor()
                && calledMethod.isPublic())
                prodBodyPrefix=new PPTerminal(calledMethod);
            else if (calledMethod.hasActiveBody()
                     && (x.Main.WITH_JAVA_LIB
                         || !calledMethod.isJavaLibraryMethod()))
            {
                prodBodyPrefix=new PPNonTerminal(alias(calledMethod),method); 
                
                if (!enqueuedMethods.contains(calledMethod))
                {
                    methodQueue.add(calledMethod);
                    enqueuedMethods.add(calledMethod);
                }
            }
        }
        
        PPNonTerminal prodHead=new PPNonTerminal(alias(unit),method);

        for (Unit succ: cfg.getSuccsOf(unit))
        {
            PPNonTerminal succNonTerm=new PPNonTerminal(alias(succ),method);

            if (prodBodyPrefix == null)
                addUnitToLexicalElement(unit,succNonTerm,method);
            else
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

        dprintln("Grammar size before optimizing: "+grammar.size());
        grammar.optimize();
        dprintln("Grammar size after optimizing: "+grammar.size());

        grammar.subwordClosure();

        grammar.optimize();
        dprintln("Grammar size after optimizing yet again "
                 +"(after subword closure): "+grammar.size());

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
