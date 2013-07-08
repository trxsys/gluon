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
import x.cfg.NonTerminal;
import x.cfg.parsing.parsingTable.ParsingTable;
import x.cfg.parsing.parsingTable.parsingAction.*;
import x.cfg.parsing.tomitaParser.TomitaParser;
import x.cfg.parsing.parseTree.ParseTree;

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

    private void debugPrintActions(List<ParsingAction> actions)
    {
        for (ParsingAction a: actions)
            dprint(a+" ; ");

        dprintln("");
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
                                      List<ParsingAction> actions)
    {
        ParseTree ptree=new ParseTree();
        NonTerminal lca;

        dprint("  "); debugPrintActions(actions);

        ptree.buildTree(word,actions);

        lca=ptree.getLCA();

        System.out.println("  LCA: "+lca);
    }
    
    private void checkThreadWord(TomitaParser parser,
                                 ArrayList<Terminal> word)
    {
        Collection<List<ParsingAction>> actionsSet=parser.parse(word);
        
        assert actionsSet != null;
        
        System.out.println("Verifying word "+word+":");
        
        for (List<ParsingAction> actions: actionsSet)
            checkThreadWordParse(word,actions);
    }
    
    private void checkThread(SootMethod thread)
    {
        ProgramPatternAnalysis programPattern
            =new ProgramPatternAnalysis(thread,moduleName);
        ParsingTable parsingTable;
        TomitaParser parser;
        
        if (true)
        {
        // XXX Test
            x.cfg.Cfg grammar=new x.cfg.Cfg();

        soot.SootMethod p=new soot.SootMethod("+",new ArrayList<Object>(),
                                              soot.VoidType.v());
        soot.SootMethod m=new soot.SootMethod("*",new ArrayList<Object>(),
                                              soot.VoidType.v());
        soot.SootMethod i=new soot.SootMethod("id",new ArrayList<Object>(),
                                              soot.VoidType.v());
        soot.SootMethod l=new soot.SootMethod("(",new ArrayList<Object>(),
                                              soot.VoidType.v());
        soot.SootMethod r=new soot.SootMethod(")",new ArrayList<Object>(),
                                              soot.VoidType.v());

        x.analysis.programPattern.PPTerminal plus=new x.analysis.programPattern.PPTerminal(p);
        x.analysis.programPattern.PPTerminal mult=new x.analysis.programPattern.PPTerminal(m);
        x.analysis.programPattern.PPTerminal id=new x.analysis.programPattern.PPTerminal(i);
        x.analysis.programPattern.PPTerminal lpar=new x.analysis.programPattern.PPTerminal(l);
        x.analysis.programPattern.PPTerminal rpar=new x.analysis.programPattern.PPTerminal(r);
         x.cfg.Production e1=new  x.cfg.Production(new x.analysis.programPattern.PPNonTerminal("E"));
         x.cfg.Production e2=new  x.cfg.Production(new x.analysis.programPattern.PPNonTerminal("E"));
         x.cfg.Production e3=new  x.cfg.Production(new x.analysis.programPattern.PPNonTerminal("E"));
         x.cfg.Production e4=new  x.cfg.Production(new x.analysis.programPattern.PPNonTerminal("E"));
         x.cfg.Production e5=new  x.cfg.Production(new x.analysis.programPattern.PPNonTerminal("S"));

        e1.appendToBody(new x.analysis.programPattern.PPNonTerminal("E"));
        e1.appendToBody(plus);
        e1.appendToBody(new x.analysis.programPattern.PPNonTerminal("E"));

        e2.appendToBody(new x.analysis.programPattern.PPNonTerminal("E"));
        e2.appendToBody(mult);
        e2.appendToBody(new x.analysis.programPattern.PPNonTerminal("E"));

        e3.appendToBody(lpar);
        e3.appendToBody(new x.analysis.programPattern.PPNonTerminal("E"));
        e3.appendToBody(rpar);

        e4.appendToBody(id);

        e5.appendToBody(new x.analysis.programPattern.PPNonTerminal("E"));

        grammar.addProduction(e1);
        grammar.addProduction(e2);
        grammar.addProduction(e3);
        grammar.addProduction(e4);

        grammar.addProduction(e5);

        grammar.setStart(new x.analysis.programPattern.PPNonTerminal("S"));


            parsingTable=new ParsingTable(grammar);
        }
        else
        {
            programPattern.analyze();
        
            parsingTable=new ParsingTable(programPattern.getGrammar());
        }

        parsingTable.buildParsingTable();
        
        parser=new TomitaParser(parsingTable);
        
        // XXX Test
        ArrayList<x.cfg.Terminal> word=new ArrayList<x.cfg.Terminal>();
        {
            word.add(new PPTerminal("id"));
            word.add(new PPTerminal("+"));
            word.add(new PPTerminal("id"));
            word.add(new PPTerminal("+"));
            word.add(new PPTerminal("id"));
            
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
