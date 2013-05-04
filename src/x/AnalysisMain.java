package x;

import java.util.Collection;

import java.util.ArrayList;

import soot.Scene;
import soot.SceneTransformer;
import soot.SootMethod;

import x.analysis.programPattern.ProgramPatternAnalysis;
import x.cfg.parsing.parsingTable.ParsingTable;
import x.analysis.thread.ThreadAnalysis;

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

    @Override
    protected void internalTransform(String paramString, 
       @SuppressWarnings("rawtypes") java.util.Map paramMap) 
    {
        scene=Scene.v();

        assert scene.getMainMethod() != null;

        for (SootMethod m: getThreads())
        {
            ProgramPatternAnalysis programPattern
                =new ProgramPatternAnalysis(m,moduleName);
            ParsingTable parsingTable;

            programPattern.analyze();

            parsingTable=new ParsingTable(programPattern.getGrammar());

            parsingTable.buildParsingTable();
        }
    }
}
