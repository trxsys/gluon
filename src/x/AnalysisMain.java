package x;

import java.util.Collection;

import soot.Scene;
import soot.SceneTransformer;
import soot.SootMethod;

import x.analysis.programPattern.ProgramPatternAnalysis;
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
        Collection<SootMethod> threads;

        ta.analyze();
        // TODO filter out thread spawned inside our target module
        
        threads=ta.getThreadsEntryMethod();

        return threads;
    }

    @Override
    protected void internalTransform(String paramString, 
       @SuppressWarnings("rawtypes") java.util.Map paramMap) 
    {
        scene=Scene.v();

        assert scene.getMainMethod() != null;

        if (true) // DEBUG
        for (SootMethod m: getThreads())
        {
            ProgramPatternAnalysis programPattern
                =new ProgramPatternAnalysis(m,moduleName);
        
            programPattern.analyze();

            new x.analysis.programPattern.ParsingTable(programPattern.getGrammar())
            .buildParsingTable();
            break;
        }
        else
        new x.analysis.programPattern.ParsingTable(null).buildParsingTable();
    }
}
