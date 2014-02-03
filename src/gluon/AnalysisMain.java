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

import java.util.LinkedList;
import java.util.ArrayList;
import java.util.HashSet;

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

        for (int i=0; i < word.size()-1; i++)
            r+=(i > 0 ? " " : "")+word.get(i).toString();

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

    private List<PPTerminal> getCodeUnits(List<ParsingAction> actions,
                                          List<Terminal> word)
    {
        ArrayList<PPTerminal> terms=new ArrayList<PPTerminal>(word.size()-1);

        for (ParsingAction a: actions)
            if (a instanceof ParsingActionReduce)
            {
                Production red=((ParsingActionReduce)a).getProduction();

                for (LexicalElement e: red.getBody())
                    if (e instanceof PPTerminal)
                        terms.add((PPTerminal)e);
            }
        
        java.util.Collections.reverse(terms);

        assert terms.size() == word.size()-1;

        return terms;
    }

    private boolean assertLCASanityCheck(List<Terminal> word, 
                                         List<ParsingAction> actions,
                                         NonTerminal lca)
    {
        ParseTree ptree=new ParseTree();

        ptree.buildTree(word,actions);

        return lca.equals(ptree.getLCA());
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

        gluon.profiling.Profiling.inc("final:parse-trees");

        reported.add(lcaMethod);

        atomic=atomicMethods.isAtomic(lcaMethod);

        dprintln("      Lowest common ancestor: "+lca);

        System.out.println("      Method: "+lcaMethod.getDeclaringClass().getShortName()
                           +"."+lcaMethod.getName()+"()");

        System.out.print("      Calls Location:");

        for (PPTerminal t: getCodeUnits(actions,word))
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

    public void loadRawContract()
    {
        contract=new LinkedList<List<Terminal>>();

        for (String clause: contractRaw.split(";"))
        {
            Start ast;
            ContractVisitorExtractWords visitorWords
                =new ContractVisitorExtractWords();

            clause=clause.trim();

            if (clause.length() == 0)
                continue;

            ast=parseContract(clause);
            ast.apply(visitorWords);

            for (List<Terminal> word: visitorWords.getWords())
            {
                for (Terminal t: word)
                    if (!module.declaresMethodByName(t.getName()))
                        Main.fatal(t.getName()+": no such method!");

                word.add(new gluon.grammar.EOITerminal());

                contract.add(word);
            }
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
        catch (gluon.contract.parser.ParserException _)
        {
            Main.fatal("syntax error in contract.");
        }
        catch (gluon.contract.lexer.LexerException _)
        {
            Main.fatal("syntax error in contract.");
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
