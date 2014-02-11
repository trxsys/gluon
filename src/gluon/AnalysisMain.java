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

package gluon;

import soot.Scene;
import soot.SceneTransformer;
import soot.SootMethod;
import soot.SootClass;
import soot.PointsToAnalysis;
import soot.Value;
import soot.Unit;

import soot.jimple.Stmt;
import soot.jimple.AssignStmt;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.spark.pag.AllocNode;

import soot.tagkit.AnnotationTag;
import soot.tagkit.Tag;
import soot.tagkit.VisibilityAnnotationTag;
import soot.tagkit.AnnotationStringElem;

import gluon.analysis.thread.ThreadAnalysis;
import gluon.analysis.programBehavior.ProgramBehaviorAnalysis;
import gluon.analysis.programBehavior.PPTerminal;
import gluon.analysis.programBehavior.PPNonTerminal;
import gluon.analysis.atomicMethods.AtomicMethods;

import gluon.grammar.Cfg;
import gluon.grammar.LexicalElement;
import gluon.grammar.Terminal;
import gluon.grammar.NonTerminal;
import gluon.grammar.Production;
import gluon.grammar.parsing.parsingTable.ParsingTable;
import gluon.grammar.parsing.parsingTable.parsingAction.*;
import gluon.grammar.parsing.parser.Parser;
import gluon.grammar.parsing.parseTree.ParseTree;
import gluon.grammar.parsing.parser.ParserCallback;

import gluon.contract.ContractVisitorExtractWords;
import gluon.contract.node.*;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.Map;

import java.util.LinkedList;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;

import java.io.PushbackReader;
import java.io.StringReader;

public class AnalysisMain
    extends SceneTransformer
{
    private static final boolean DEBUG=false;

    private static final String CONTRACT_ANNOTATION="Contract";
    
    private static final AnalysisMain instance=new AnalysisMain();
    
    private Scene scene;
    private String moduleName; // name of the module to analyze
    private SootClass module;  // class of the module to analyze
    private AtomicMethods atomicMethods;
    private Collection<List<Terminal>> contract;

    private String contractRaw;
    
    private AnalysisMain()
    {
        scene=null;
        moduleName=null;
        module=null;
        atomicMethods=null;
        contract=null;
        contractRaw=null;
    }
    
    private void dprint(String s)
    {
        if (DEBUG)
            System.out.print(s);
    }
    
    private void dprintln(String s)
    {
        dprint(s+"\n");
    }
    
    public static AnalysisMain instance() 
    {
        return instance;
    }
    
    public void setModuleToAnalyze(String m)
    {
        moduleName=m;
    }

    private static String actionsStr(List<ParsingAction> actions)
    {
        String r="";

        for (ParsingAction a: actions)
        {
            if (a instanceof ParsingActionShift)
                r+="s ; ";
            else if (a instanceof ParsingActionReduce)
                r+=((ParsingActionReduce)a).getProduction()+" ; ";
            else if (a instanceof ParsingActionAccept)
                r+=a.toString();
        }

        return r;
    }
    
    private static String wordStr(List<Terminal> word)
    {
        String r="";

        for (int i=0; i < word.size(); i++)
        {
            Terminal term=word.get(i);

            if (term instanceof gluon.grammar.EOITerminal)
                break;

            assert term instanceof PPTerminal;

            r+=(i > 0 ? " " : "")+((PPTerminal)term).getFullName();
        }

        return r;
    }

    private Collection<SootMethod> getThreads()
    {
        ThreadAnalysis ta=new ThreadAnalysis(scene.getCallGraph());
        Collection<SootMethod> allThreads;
        Collection<SootMethod> relevantThreads;
        
        ta.analyze();
        
        allThreads=ta.getThreadsEntryMethod();
        
        relevantThreads=new ArrayList<SootMethod>(allThreads.size());
        
        for (SootMethod m: allThreads)
            if (!m.isJavaLibraryMethod())
                relevantThreads.add(m);
        
        return relevantThreads;
    }

    private List<PPTerminal> getParsingTerminals(List<Terminal> word,
                                                 List<ParsingAction> actions)
    {
        ParseTree tree=new ParseTree(word,actions);
        List<PPTerminal> ppterms;
        List<Terminal> terms;

        tree.buildTree();

        terms=tree.getTerminals();
        ppterms=new ArrayList<PPTerminal>(terms.size());

        for (Terminal t: terms)
            ppterms.add((PPTerminal)t);

        return ppterms;
    }

    private boolean assertLCASanityCheck(List<Terminal> word, 
                                         List<ParsingAction> actions,
                                         NonTerminal lca)
    {
        if (DEBUG)
        {
            ParseTree ptree=new ParseTree(word,actions);

            ptree.buildTree();
        
            return lca.equals(ptree.getLCA());
        }
        else
            return true;
    }

    private int tryUnify(Map<String,Value> unif,
                         String contractVar,
                         Value v)
    {
        if (contractVar == null)
            return 0;

        if (unif.containsKey(contractVar))
        {
            if (v == null || unif.get(contractVar) == null)
                return unif.get(contractVar) == v ? 0 : -1;

            return unif.get(contractVar).equivTo(v) ? 0 : -1;
        }

        unif.put(contractVar,v);
        
        return 0;
    }

    private boolean argumentsMatch(List<Terminal> word, List<ParsingAction> actions)
    {
        List<PPTerminal> parsedWord;
        /* contract variable -> program value */
        Map<String,Value> unif=new HashMap<String,Value>();

        parsedWord=getParsingTerminals(word,actions);

        assert parsedWord.size() == word.size()-1; /* -1 because of the '$' */

        for (int i=0; i < parsedWord.size(); i++)
        {
            PPTerminal termC=(PPTerminal)word.get(i);
            PPTerminal termP=parsedWord.get(i);
            List<String> argumentsC;
            Unit unit;

            assert termC.equals(termP); /* equals ignores arguments */

            // System.out.println("-> "+termC);

            argumentsC=termC.getArguments();

            unit=termP.getCodeUnit();

            if (termC.getReturn() != null)
            {
                String contractVar=termC.getReturn();

                if (unit instanceof AssignStmt)
                {
                    Value v=((AssignStmt)unit).getLeftOp();
                    
                    // System.out.println("* unify "+contractVar+" <-> "+v);
                    
                    if (tryUnify(unif,contractVar,v) != 0)
                        return false;
                    
                    //System.out.println("  OK");
                    
                    dprintln("      Unifyed "+contractVar+" <-> "+v);
                }
                else
                {
                    /* If control reaches here then the contract does specify a
                     * return value but the word in the client program does not
                     * assign the return value to a variable.  In this case
                     * we don't fail immediatly because it is possible that the
                     * variable is not used elsewhere.  So we unify the variable
                     * with null.  That variable will no be able to be unified
                     * with anything but null values.
                     */

                    if (tryUnify(unif,contractVar,null) != 0)
                        return false;

                }
            }

            // System.out.println(call);

            for (int j=0; argumentsC != null && j < argumentsC.size(); j++)
            {
                InvokeExpr call=((Stmt)unit).getInvokeExpr();
                String contractVar=argumentsC.get(j);
                Value v=null;

                v=call.getArg(j);

                // System.out.println("unify "+contractVar+" <-> "+v);

                if (tryUnify(unif,contractVar,v) != 0)
                    return false;

                // System.out.println("  OK");

                dprintln("      Unifyed "+contractVar+" <-> "+v);
            }
        }

        // System.out.println("unification accepted");

        return true;
    }

    private int checkThreadWordParse(SootMethod thread,
                                     List<Terminal> word, 
                                     List<ParsingAction> actions,
                                     Set<SootMethod> reported,
                                     NonTerminal lca)
    {
        SootMethod lcaMethod;
        boolean atomic;

        assert assertLCASanityCheck(word,actions,lca);

        assert lca instanceof PPNonTerminal;

        lcaMethod=((PPNonTerminal)lca).getMethod();

        if (reported.contains(lcaMethod))
            return 1;

        if (!argumentsMatch(word,actions))
        {
            gluon.profiling.Profiling.inc("final:discarded-trees-args-not-match");
            return 1;
        }

        gluon.profiling.Profiling.inc("final:parse-trees");

        reported.add(lcaMethod);

        atomic=atomicMethods.isAtomic(lcaMethod);

        dprintln("      Lowest common ancestor: "+lca);

        System.out.println("      Method: "+lcaMethod.getDeclaringClass().getShortName()
                           +"."+lcaMethod.getName()+"()");

        System.out.print("      Calls Location:");

        for (PPTerminal t: getParsingTerminals(word,actions))
        {
            int linenum=t.getLineNumber();
            String source=t.getSourceFile();

            System.out.print(" "+source+":"+(linenum > 0 ? ""+linenum : "?"));
        }

        System.out.println();

        System.out.println("      Atomic: "+(atomic ? "YES" : "NO"));

        return atomic ? 0 : -1;
    }
    
    private void checkThreadWord(final SootMethod thread,
                                 final List<Terminal> word)
    {
        final Set<SootMethod> reported=new HashSet<SootMethod>();
        Collection<AllocNode> moduleAllocSites;

        System.out.println("  Verifying word "+wordStr(word)+":");
        System.out.println();

        moduleAllocSites=PointsToInformation.getModuleAllocationSites(module);

        /* for static modules */
        moduleAllocSites.add(null);

        for (soot.jimple.spark.pag.AllocNode as: moduleAllocSites)
        {
            Parser parser=makeParser(thread,as);

            gluon.profiling.Timer.start("final:total-parsing");
            gluon.profiling.Timer.start("parsing");
            parser.parse(word, new ParserCallback(){
                    public int callback(List<ParsingAction> actions,
                                        NonTerminal lca)
                    {
                        int ret;
                        
                        gluon.profiling.Timer.stop("parsing");
                        ret=checkThreadWordParse(thread,word,actions,reported,lca);
                        gluon.profiling.Timer.start("parsing");
                        
                        if (ret <= 0)
                            System.out.println();
                        
                        return ret < 0 ? -1 : 0;
                    }
                });
            gluon.profiling.Timer.stop("parsing");
            gluon.profiling.Timer.stop("final:total-parsing");
        }

        if (reported.size() == 0)
        {
            System.out.println("    No occurrences");
            System.out.println();
        }
    }

    private Parser makeParser(SootMethod thread, AllocNode as)
    {
        ProgramBehaviorAnalysis programBehavior
            =new ProgramBehaviorAnalysis(thread,module,as);
        ParsingTable parsingTable;
        Cfg grammar;
        
        dprintln(" Checking allocsite "+as);
        
        gluon.profiling.Profiling.inc("final:alloc-sites-total");
        
        programBehavior=new ProgramBehaviorAnalysis(thread,module,as);

        gluon.profiling.Timer.start("final:analysis-behavior");
        programBehavior.analyze();
        gluon.profiling.Timer.stop("final:analysis-behavior");

        grammar=programBehavior.getGrammar();

        gluon.profiling.Profiling.inc("final:grammar-productions-total",
                                      grammar.size());

        parsingTable=new ParsingTable(grammar);

        gluon.profiling.Timer.start("final:build-parsing-table");
        parsingTable.buildParsingTable();
        gluon.profiling.Timer.stop("final:build-parsing-table");

        gluon.profiling.Profiling.inc("final:parsing-table-state-number-total",
                                      parsingTable.numberOfStates());

        return new Parser(parsingTable);
    }
    
    private void checkThread(SootMethod thread)
    {
        System.out.println("Checking thread "
                           +thread.getDeclaringClass().getShortName()
                           +"."+thread.getName()+"():");
        System.out.println();
        
        for (List<Terminal> word: contract)
            checkThreadWord(thread,word);
    }
    
    private void runMethodAtomicityAnalysis(Collection<SootMethod> threads)
    {
        atomicMethods=new AtomicMethods(scene.getCallGraph(),threads);
        
        atomicMethods.analyze();
    }
    
    private SootClass getModuleClass()
    {
        assert moduleName != null;

        for (SootClass c: scene.getClasses())
            if (c.getName().equals(moduleName))
                return c;
    
        return null;
    }

    public void setContract(String contract)
    {
        contractRaw=contract;
    }

    private void loadRawContractClause(String clause)
    {
        Start ast;
        ContractVisitorExtractWords visitorWords
            =new ContractVisitorExtractWords();

        ast=parseContract(clause);
        ast.apply(visitorWords);

        for (List<PPTerminal> word: visitorWords.getWords())
        {
            for (PPTerminal t: word)
                if (module.declaresMethodByName(t.getName()))
                {
                    try
                    {
                        SootMethod m=module.getMethodByName(t.getName());

                        if (t.getArguments() != null
                            && t.getArguments().size() != m.getParameterCount())
                            Main.fatal(t.getName()
                                       +": wrong number of parameters!");
                    }
                    catch (Exception _)
                    {
                        Main.fatal(t.getName()+": ambiguous method!");
                    }
                }
                else
                    Main.fatal(t.getName()+": no such method!");
            

            List<Terminal> contractWord
                =new ArrayList<Terminal>(word.size()+1);

            contractWord.addAll(word);
            contractWord.add(new gluon.grammar.EOITerminal());
            
            contract.add(contractWord);
        }
    }

    private void loadRawContract()
    {
        contract=new LinkedList<List<Terminal>>();

        for (String clause: contractRaw.split(";"))
        {
            clause=clause.trim();

            if (clause.length() == 0)
                continue;

            loadRawContractClause(clause);
        }

        dprintln("contract: "+contract);
    }

    private String extractContractRaw()
    {
         Tag tag=module.getTag("VisibilityAnnotationTag");
         
         if (tag == null)
            return null;

        VisibilityAnnotationTag visibilityAnnotationTag=(VisibilityAnnotationTag) tag;
        List<AnnotationTag> annotations=visibilityAnnotationTag.getAnnotations();
        
        for (AnnotationTag annotationTag: annotations) 
            if (annotationTag.getType().endsWith("/"+CONTRACT_ANNOTATION+";")
                && annotationTag.getNumElems() == 1
                && annotationTag.getElemAt(0) instanceof AnnotationStringElem
                && annotationTag.getElemAt(0).getName().equals("clauses"))
            {
                AnnotationStringElem e=(AnnotationStringElem)annotationTag.getElemAt(0);

                return e.getValue();
            }

        return null;
    }

    private Start parseContract(String clause)
    {
        PushbackReader reader=new PushbackReader(new StringReader(clause));
        gluon.contract.lexer.Lexer lexer
            =new gluon.contract.lexer.Lexer(reader);
        gluon.contract.parser.Parser parser
            =new gluon.contract.parser.Parser(lexer);
        Start ast=null;
                
        try
        {
            ast=parser.parse();
        }
        catch (gluon.contract.parser.ParserException pe)
        {
            Main.fatal("syntax error in contract: "+clause+": "+pe.getMessage());
        }
        catch (gluon.contract.lexer.LexerException pe)
        {
            Main.fatal("syntax error in contract: "+clause+": "+pe.getMessage());
        }
        catch (java.io.IOException _)
        {
            assert false : "this should not happen";
        }
        
        assert ast != null;
        
        return ast;
    }
    
    private void extractAnnotatedContract()
    {
        contractRaw=extractContractRaw();
    }

    @Override
    protected void internalTransform(String paramString, 
                                     @SuppressWarnings("rawtypes") java.util.Map paramMap) 
    {
        Collection<SootMethod> threads;
        
        scene=Scene.v();
        assert scene.getMainMethod() != null;

        gluon.profiling.Timer.stop("final:soot-init");

        module=getModuleClass();

        if (module == null)
            Main.fatal(moduleName+": module's class not found");
        
        /* if the contract was not passed by the command line then extract it
         * from the module's annotation @Contract.
         */
        if (contractRaw == null)
            extractAnnotatedContract();

        loadRawContract();

        if (contract.size() == 0)
            Main.fatal("empty contract");
        
        gluon.profiling.Timer.start("analysis-threads");
        threads=getThreads();
        gluon.profiling.Timer.stop("analysis-threads");

        gluon.profiling.Profiling.set("threads",threads.size());

        gluon.profiling.Timer.start("analysis-atomicity");
        runMethodAtomicityAnalysis(threads);
        gluon.profiling.Timer.stop("analysis-atomicity");
        
        for (SootMethod m: threads)
            checkThread(m);
    }
}
