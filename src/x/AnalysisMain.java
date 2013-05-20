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
import x.analysis.atomicMethods.AtomicMethods;

import x.cfg.LexicalElement;
import x.cfg.Terminal;
import x.cfg.parsing.parsingTable.ParsingTable;
import x.cfg.parsing.parsingTable.parsingAction.ParsingActionReduce;
import x.cfg.parsing.tomitaParser.TomitaParser;

public class AnalysisMain
    extends SceneTransformer
{
    private static final boolean DEBUG=true;
    
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
    
    private void checkThreadWordParse(ArrayList<Terminal> word, 
                                      List<ParsingActionReduce> reductions)
    {
        int count=0;
        int wordLen=word.size()-1; // -1 because of $

        dprint("  ");

        for (ParsingActionReduce red: reductions)
            for (LexicalElement e: red.getProduction().getBody())
            {
                if (e instanceof PPTerminal)
                {
                    PPTerminal term=(PPTerminal)e;
                    
                    // System.out.print(" "+term);
                }
                
                dprint(red.getProduction()+" ; ");
            }

        dprintln("");
    }
    
    private void checkThreadWord(TomitaParser parser,
                                 ArrayList<Terminal> word)
    {
        Collection<List<ParsingActionReduce>> reductionsSet=parser.parse(word);
        
        assert reductionsSet != null;
        
        System.out.println("Verifying spec "+word+":");
        
        for (List<ParsingActionReduce> reductions: reductionsSet)
            checkThreadWordParse(word,reductions);
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
        
        System.exit(0);

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
