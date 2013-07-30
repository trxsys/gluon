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

import java.util.Collection;
import java.util.List;
import java.util.Set;

import java.util.LinkedList;
import java.util.ArrayList;
import java.util.HashSet;

import soot.Scene;
import soot.SceneTransformer;
import soot.SootMethod;
import soot.SootClass;

import soot.tagkit.AnnotationTag;
import soot.tagkit.LineNumberTag;
import soot.tagkit.SourceFileTag;
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

public class AnalysisMain
    extends SceneTransformer
{
    private static final boolean DEBUG=false;

    private static final String CONTRACT_ANNOTATION="Contract";
    
    private static final AnalysisMain instance = new AnalysisMain();
    
    private Scene scene;
    private String moduleName; // name of the module to analyze
    private SootClass module;  // class of the module to analyze
    private AtomicMethods atomicMethods;
    private Collection<ArrayList<Terminal>> contract;
    
    private AnalysisMain()
    {
        scene=null;
        moduleName=null;
        module=null;
        atomicMethods=null;
        contract=null;
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
    
    private static String wordStr(ArrayList<Terminal> word)
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
                                          ArrayList<Terminal> word)
    {
        ArrayList<PPTerminal> terms=new ArrayList<PPTerminal>(word.size()-1);

        for (int i=0; i < word.size()-1; i++)
            terms.add(null);
        
        for (ParsingAction a: actions)
            if (a instanceof ParsingActionReduce)
            {
                Production red=((ParsingActionReduce)a).getProduction();

                for (LexicalElement e: red.getBody())
                    if (e instanceof PPTerminal)
                    {
                        int idx=word.indexOf(e);

                        assert idx >= 0;

                        terms.set(idx,(PPTerminal)e);
                    }
            }
        
        return terms;
    }

    private boolean assertLCASanityCheck(ArrayList<Terminal> word, 
                                         List<ParsingAction> actions,
                                         NonTerminal lca)
    {
        ParseTree ptree=new ParseTree();

        ptree.buildTree(word,actions);

        return lca.equals(ptree.getLCA());
    }

    private int checkThreadWordParse(SootMethod thread,
                                     ArrayList<Terminal> word, 
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

        System.out.print("      Calls Line Number:");

        for (PPTerminal t: getCodeUnits(actions,word))
        {
            soot.Unit unit=t.getCodeUnit();
            LineNumberTag lineTag=(LineNumberTag)unit.getTag("LineNumberTag");
            SootClass c=t.getCodeMethod().getDeclaringClass();
            SourceFileTag sourceTag=(SourceFileTag)c.getTag("SourceFileTag");
            int linenum=-1;
            String source="?";

            if (lineTag != null)
                linenum=lineTag.getLineNumber();

            if (sourceTag != null)
                source=sourceTag.getSourceFile();

            System.out.print(" "+source+":"+(linenum > 0 ? ""+linenum : "?"));
        }

        System.out.println();

        System.out.println("      Atomic: "+(atomic ? "YES" : "NO"));

        return atomic ? 0 : -1;
    }
    
    private int checkThreadWord(final SootMethod thread,
                                Parser parser,
                                final ArrayList<Terminal> word)
    {
        int ret;
        final Set<SootMethod> reported=new HashSet<SootMethod>();

        System.out.println("  Verifying word "+wordStr(word)+":");
        System.out.println();

        gluon.profiling.Timer.start("final:total-parsing");
        gluon.profiling.Timer.start("parsing");
        ret=parser.parse(word, new ParserCallback(){
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

        if (reported.size() == 0)
        {
            System.out.println("    No occurrences");
            System.out.println();
        }

        return ret;
    }
    
    private void checkThread(SootMethod thread)
    {
        ProgramBehaviorAnalysis programBehavior
            =new ProgramBehaviorAnalysis(thread,module);
        ParsingTable parsingTable;
        Parser parser;
        Cfg grammar;

        gluon.profiling.Timer.start("final:analysis-behavior");
        programBehavior.analyze();
        gluon.profiling.Timer.stop("final:analysis-behavior");

        grammar=programBehavior.getGrammar();

        gluon.profiling.Profiling.inc("final:grammar-productions",grammar.size());

        parsingTable=new ParsingTable(grammar);

        gluon.profiling.Timer.start("final:build-parsing-table");
        parsingTable.buildParsingTable();
        gluon.profiling.Timer.stop("final:build-parsing-table");

        gluon.profiling.Profiling.inc("final:parsing-table-state-number",
                                      parsingTable.numberOfStates());

        parser=new Parser(parsingTable);
        
        System.out.println("Checking thread "
                           +thread.getDeclaringClass().getShortName()
                           +"."+thread.getName()+"():");
        System.out.println();

        for (ArrayList<Terminal> word: contract)
            checkThreadWord(thread,parser,word);
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

    private List<String> extractContractClauses()
    {
         Tag tag=module.getTag("VisibilityAnnotationTag");
         List<String> clauses=new ArrayList<String>(32);
         
         if (tag == null)
            return clauses;

        VisibilityAnnotationTag visibilityAnnotationTag=(VisibilityAnnotationTag) tag;
        List<AnnotationTag> annotations=visibilityAnnotationTag.getAnnotations();
        
        for (AnnotationTag annotationTag: annotations) 
            if (annotationTag.getType().endsWith("/"+CONTRACT_ANNOTATION+";")
                && annotationTag.getNumElems() == 1
                && annotationTag.getElemAt(0) instanceof AnnotationStringElem
                && annotationTag.getElemAt(0).getName().equals("clauses"))
            {
                AnnotationStringElem e=(AnnotationStringElem)annotationTag.getElemAt(0);

                for (String clause: e.getValue().split(";"))
                    if (clause.trim().length() > 0)
                        clauses.add(clause.trim());
            }

        return clauses;
    }

    private void extractContract()
    {
        List<String> contractClauses;

        contract=new LinkedList<ArrayList<Terminal>>();
        
        contractClauses=extractContractClauses();

        /* TODO: this should be parsed intro a StarFreeRegex.
         */
        for (String clause: contractClauses)
        {
            ArrayList<gluon.grammar.Terminal> word
                =new ArrayList<gluon.grammar.Terminal>();
            
            for (String m: clause.split(" "))
                if (m.trim().length() > 0)
                {
                    String methodName=m.trim();

                    if (!module.declaresMethodByName(methodName))
                        Main.fatal(methodName+": no such method!");

                    word.add(new PPTerminal(methodName));
                }

            word.add(new gluon.grammar.EOITerminal());

            contract.add(word);
        }

        dprintln("contract: "+contract);
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
        
        extractContract();

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
