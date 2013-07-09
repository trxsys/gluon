package x;

import java.util.Collection;
import java.util.List;

import java.util.LinkedList;
import java.util.ArrayList;

import soot.Scene;
import soot.SceneTransformer;
import soot.SootMethod;
import soot.SootClass;

import x.analysis.thread.ThreadAnalysis;
import x.analysis.programBehavior.ProgramBehaviorAnalysis;
import x.analysis.programBehavior.PPTerminal;
import x.analysis.programBehavior.PPNonTerminal;
import x.analysis.atomicMethods.AtomicMethods;

import x.cfg.LexicalElement;
import x.cfg.Terminal;
import x.cfg.NonTerminal;
import x.cfg.parsing.parsingTable.ParsingTable;
import x.cfg.parsing.parsingTable.parsingAction.*;
import x.cfg.parsing.tomitaParser.TomitaParser;
import x.cfg.parsing.parseTree.ParseTree;

public class AnalysisMain
    extends SceneTransformer
{
    private static final boolean DEBUG=false;
    
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
            r+=(i > 1 ? " " : "")+word.get(i).toString();

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
    
    private int checkThreadWordParse(ArrayList<Terminal> word, 
                                     List<ParsingAction> actions)
    {
        ParseTree ptree=new ParseTree();
        NonTerminal lca;
        SootMethod lcaMethod;
        boolean atomic;

        System.out.println("    "+actionsStr(actions));

        ptree.buildTree(word,actions);

        lca=ptree.getLCA();

        assert lca instanceof PPNonTerminal;

        lcaMethod=((PPNonTerminal)lca).getMethod();
        atomic=atomicMethods.isAtomic(lcaMethod);

        System.out.println("      Lowest common ancestor: "+lca);
        System.out.println("      Method: "+lcaMethod.getName()+"()");
        System.out.println("      Atomic: "+(atomic ? "YES" : "NO"));

        return atomic ? 0 : -1;
    }
    
    private int checkThreadWord(TomitaParser parser,
                                ArrayList<Terminal> word)
    {
        Collection<List<ParsingAction>> actionsSet=parser.parse(word);
        boolean error=false;

        assert actionsSet != null;
        
        System.out.println("  Verifying word "+wordStr(word)+":");
        
        for (List<ParsingAction> actions: actionsSet)
        {
            if (checkThreadWordParse(word,actions) != 0)
                error=true;

            System.out.println();
        }

        return error ? -1 : 0;
    }
    
    private void checkThread(SootMethod thread)
    {
        ProgramBehaviorAnalysis programPattern
            =new ProgramBehaviorAnalysis(thread,module);
        ParsingTable parsingTable;
        TomitaParser parser;

        programPattern.analyze();
        
        parsingTable=new ParsingTable(programPattern.getGrammar());
        parsingTable.buildParsingTable();
        
        parser=new TomitaParser(parsingTable);
        
        System.out.println("Checking thread "
                           +thread.getDeclaringClass().getShortName()
                           +"."+thread.getName()+"():");

        for (ArrayList<Terminal> word: contract)
            checkThreadWord(parser,word);
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

    private void extractContract()
    {
        contract=new LinkedList<ArrayList<Terminal>>();

        /* TODO */
        ArrayList<x.cfg.Terminal> word=new ArrayList<x.cfg.Terminal>();
        {
            word.add(new PPTerminal("a"));
            word.add(new PPTerminal("b"));
            word.add(new PPTerminal("c"));
            
            word.add(new x.cfg.EOITerminal());
        }

        ArrayList<x.cfg.Terminal> word1=new ArrayList<x.cfg.Terminal>();
        {
            word1.add(new PPTerminal("a"));
            word1.add(new PPTerminal("b"));
            
            word1.add(new x.cfg.EOITerminal());
        }

        contract.add(word);
        contract.add(word1);
    }

    @Override
    protected void internalTransform(String paramString, 
                                     @SuppressWarnings("rawtypes") java.util.Map paramMap) 
    {
        Collection<SootMethod> threads;
        
        scene=Scene.v();
        assert scene.getMainMethod() != null;

        module=getModuleClass();

        if (module == null)
        {
            System.err.println(moduleName+": module's class not found");
            System.exit(-1);
        }
        
        extractContract();

        threads=getThreads();
        
        runMethodAtomicityAnalysis(threads);
        
        for (SootMethod m: threads)
            checkThread(m);
    }
}
