package x.analysis.programPattern;

/* This analysis creates a grammar that describes the access patterns to
 * to the module unde analysis.
 *
 * The grammar is extracted from the flow control graph. It's terminals are
 * the methods of the module we are analyzing. 
 */

import x.cfg.Cfg;
import x.cfg.Production;
import x.cfg.LexicalElement;

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

import soot.tagkit.AnnotationTag;
import soot.tagkit.Tag;
import soot.tagkit.VisibilityAnnotationTag;

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

public class ProgramPatternAnalysis
{
    private static final String ATOMIC_METHOD_ANNOTATION="Atomic";
    
    private static final boolean DEBUG=true;
    
    private SootMethod entryMethod;
    private Cfg grammar;
    
    private String moduleName; // module under analysis
    
    private Set<Unit> visited;
    
    private NonTerminalAliasCreator aliasCreator;
    
    private Queue<SootMethod> methodQueue; // queue of methods to analyse
    private Set<SootMethod> enqueuedMethods;
    
    public ProgramPatternAnalysis(SootMethod method, String module)
    {
        entryMethod=method;
        grammar=new Cfg();
        
        moduleName=module;
        
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
    
    private void analyzeUnit(Unit unit, UnitGraph cfg)
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
                =calledMethod.getDeclaringClass().getName().equals(moduleName);
            
            if (isTargetModule 
                && !calledMethod.isConstructor()
                && calledMethod.isPublic()) // TODO what about static methods?
            {
                prodBodyPrefix=new PPTerminal(calledMethod);
                
                /* Instead of adding { S → A | A ∈ V } we add it here, only for
                 * production that includes terminals.
                 */
                addStartToUnitProduction(unit);
            }
            else if (calledMethod.hasActiveBody())
            {
                prodBodyPrefix=new PPNonTerminal(alias(calledMethod),
                                                 isAtomicMethod(calledMethod)); 
                
                if (!enqueuedMethods.contains(calledMethod))
                {
                    methodQueue.add(calledMethod);
                    enqueuedMethods.add(calledMethod);
                }
            }
        }
        
        PPNonTerminal prodHead=new PPNonTerminal(alias(unit));

        for (Unit succ: cfg.getSuccsOf(unit))
            if (prodBodyPrefix == null)
                addUnitToLexicalElement(unit,new PPNonTerminal(alias(succ)));
            else
                addUnitToTwoLexicalElements(unit,prodBodyPrefix,
                                            new PPNonTerminal(alias(succ)));

        if (cfg.getSuccsOf(unit).size() == 0)
        {
            assert prodBodyPrefix == null : "If we have no successors we "
                +"should be a return statement, and therefore have no method "
                +"calls";

            addUnitToEmptyProduction(unit);
        }
        else if (prodBodyPrefix != null)
            addUnitToLexicalElement(unit,prodBodyPrefix);

        for (Unit succ: cfg.getSuccsOf(unit))
            analyzeUnit(succ,cfg);
    }
    
    private static boolean isAtomicMethod(SootMethod method)
    {
        Tag tag = method.getTag("VisibilityAnnotationTag");
        
        if (tag == null)
            return false;
        
        VisibilityAnnotationTag visibilityAnnotationTag = (VisibilityAnnotationTag) tag;
        List<AnnotationTag> annotations = visibilityAnnotationTag.getAnnotations();
        
        for (AnnotationTag annotationTag : annotations) 
            if (annotationTag.getType().endsWith("/"+ATOMIC_METHOD_ANNOTATION+";"))
                return true;
        
        return false;
    }

    private void addUnitToTwoLexicalElements(Unit unit,
                                             LexicalElement body1,
                                             LexicalElement body2)
    {
        PPNonTerminal head=new PPNonTerminal(alias(unit));
        Production production=new Production(head);
        
        production.appendToBody(body1);
        production.appendToBody(body2);
        
        grammar.addProduction(production);        
    }

    private void addUnitToLexicalElement(Unit unit,
                                         LexicalElement body)
    {
        PPNonTerminal head=new PPNonTerminal(alias(unit));
        Production production=new Production(head);
        
        production.appendToBody(body);
        
        grammar.addProduction(production);        
    }

    private void addUnitToEmptyProduction(Unit unit)
    {
        PPNonTerminal head=new PPNonTerminal(alias(unit));
        Production production=new Production(head);
        
        grammar.addProduction(production);
    }

    private void addStartToUnitProduction(Unit unit)
    {
        x.cfg.NonTerminal head=grammar.getStart();
        Production production=new Production(head);
        LexicalElement body=new PPNonTerminal(alias(unit));
        
        production.appendToBody(body);
        
        grammar.addProduction(production);
    }

    private void addMethodToHeadProduction(SootMethod method, 
                                           Unit entryPoint)
    {
        PPNonTerminal head=new PPNonTerminal(alias(method),isAtomicMethod(method));
        Production production=new Production(head);
        LexicalElement body=new PPNonTerminal(alias(entryPoint));
        
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
            analyzeUnit(head,cfg);
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
        NonTerminal newStart=new PPNonTerminal(oldStart.toString()+'\'');
        Production prod=new Production(newStart);

        prod.appendToBody(oldStart);

        grammar.addProduction(prod);
        grammar.setStart(newStart);
    }

    public void analyze()
    {
        grammar.setStart(new PPNonTerminal(alias(entryMethod),
                                           isAtomicMethod(entryMethod)));

        analyzeReachableMethods(entryMethod);
        
        dprintln("Grammar size before optimizing: "+grammar.size());
        grammar.optimize();
        dprintln("Grammar size after optimizing: "+grammar.size());

        addNewStart();

        assert grammar.hasUniqueStart();
    }
    
    public Cfg getGrammar()
    {
        assert grammar.getStart() != null : "analyze() must first be called";

        return grammar;
    }
}
