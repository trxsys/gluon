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

import x.cfg.LexicalElement;
import x.cfg.Terminal;
import x.cfg.parsing.parsingTable.ParsingTable;
import x.cfg.parsing.parsingTable.parsingAction.ParsingActionReduce;
import x.cfg.parsing.tomitaParser.TomitaParser;

public class AnalysisMain
    extends SceneTransformer
{
    private static final AnalysisMain instance = new AnalysisMain();

    private Scene scene;
    private String moduleName; // module to analyze

    private AnalysisMain() 
    {
        scene=null;
        moduleName=null;
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

    private void checkThreadWord(TomitaParser parser,
                                 ArrayList<Terminal> word)
    {
        List<ParsingActionReduce> reductions=parser.parse(word);
        
        if (reductions == null)
            return; // everything ok

        System.out.print("Here we go:");

        for (ParsingActionReduce red: reductions)
            for (LexicalElement e: red.getProduction().getBody())
                if (e instanceof PPTerminal)
                    System.out.print(" "+e);

        System.out.println();
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

    @Override
    protected void internalTransform(String paramString, 
       @SuppressWarnings("rawtypes") java.util.Map paramMap) 
    {
        scene=Scene.v();

        assert scene.getMainMethod() != null;

        for (SootMethod m: getThreads())
            checkThread(m);
    }
}
