package x;

import java.util.Collection;
import java.util.List;

import java.util.ArrayList;

import soot.Scene;
import soot.SceneTransformer;
import soot.SootMethod;

import x.analysis.thread.ThreadAnalysis;
import x.analysis.programPattern.ProgramPatternAnalysis;
import x.analysis.programPattern.PPTerminal;
import x.analysis.programPattern.PPNonTerminal;
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
    private String moduleName; // module to analyze
    private AtomicMethods atomicMethods;
    
    private AnalysisMain()
    {
        scene=null;
        moduleName=null;
        atomicMethods=null;
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

    private void printActions(List<ParsingAction> actions)
    {
        for (ParsingAction a: actions)
        {
            if (a instanceof ParsingActionShift)
                System.out.print("s ; ");
            else if (a instanceof ParsingActionReduce)
                System.out.print(((ParsingActionReduce)a).getProduction()
                                   +" ; ");
            else if (a instanceof ParsingActionAccept)
                System.out.print(a.toString());
        }

        System.out.println("");
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

        System.out.print("    "); printActions(actions);

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
        
        System.out.println("  Verifying word "+word+":");
        
        for (List<ParsingAction> actions: actionsSet)
            if (checkThreadWordParse(word,actions) != 0)
                error=true;

        return error ? -1 : 0;
    }
    
    private void checkThread(SootMethod thread)
    {
        ProgramPatternAnalysis programPattern
            =new ProgramPatternAnalysis(thread,moduleName);
        ParsingTable parsingTable;
        TomitaParser parser;

        programPattern.analyze();
        
        parsingTable=new ParsingTable(programPattern.getGrammar());
        parsingTable.buildParsingTable();
        
        parser=new TomitaParser(parsingTable);
        
        System.out.println("Checking thread "
                           +thread.getDeclaringClass().getShortName()+":");

        // XXX Test
        ArrayList<x.cfg.Terminal> word=new ArrayList<x.cfg.Terminal>();
        {
            word.add(new PPTerminal("a"));
            word.add(new PPTerminal("b"));
            word.add(new PPTerminal("c"));
            
            word.add(new x.cfg.EOITerminal());
        }
        
        // XXX for each word
        checkThreadWord(parser,word);
    }
    
    private void runMethodAtomicityAnalysis(Collection<SootMethod> threads)
    {
        atomicMethods=new AtomicMethods(scene.getCallGraph(),threads);
        
        atomicMethods.analyze();
    }
    
    @Override
    protected void internalTransform(String paramString, 
                                     @SuppressWarnings("rawtypes") java.util.Map paramMap) 
    {
        Collection<SootMethod> threads;
        
        scene=Scene.v();
        assert scene.getMainMethod() != null;
        
        threads=getThreads();
        
        runMethodAtomicityAnalysis(threads);
        
        for (SootMethod m: threads)
            checkThread(m);
    }
}
