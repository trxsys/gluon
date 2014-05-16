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
import soot.Unit;

import soot.jimple.spark.pag.AllocNode;

import gluon.analysis.thread.ThreadAnalysis;
import gluon.analysis.programBehavior.BehaviorAnalysis;
import gluon.analysis.programBehavior.WholeProgramBehaviorAnalysis;
import gluon.analysis.programBehavior.ClassBehaviorAnalysis;
import gluon.analysis.programBehavior.BehaviorAnalysis;
import gluon.analysis.programBehavior.PPTerminal;
import gluon.analysis.programBehavior.PPNonTerminal;
import gluon.analysis.atomicity.AtomicityAnalysis;
import gluon.analysis.valueEquivalence.ValueEquivAnalysis;
import gluon.analysis.valueEquivalence.ValueM;
import gluon.analysis.monitor.MonitorAnalysis;
import gluon.analysis.pointsTo.PointsToInformation;

import gluon.grammar.Cfg;
import gluon.grammar.Symbol;
import gluon.grammar.Terminal;
import gluon.grammar.NonTerminal;
import gluon.grammar.Production;

import gluon.parsing.parsingTable.ParsingTable;
import gluon.parsing.parsingTable.parsingAction.*;
import gluon.parsing.parser.Parser;
import gluon.parsing.parser.ParserSubwords;
import gluon.parsing.parser.ParserCallback;
import gluon.parsing.parser.ParserAbortedException;
import gluon.parsing.parseTree.ParseTree;

import gluon.contract.Contract;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.Map;

import java.util.LinkedList;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;

public class AnalysisMain
    extends SceneTransformer
{
    private static final boolean DEBUG=false;

    private static final AnalysisMain instance=new AnalysisMain();

    private Scene scene;
    private String moduleName; /* Name of the module to analyze */
    private SootClass module;  /* Class of the module to analyze */
    private AtomicityAnalysis atomicityAnalysis;
    private Contract contract;

    private String contractRaw;

    class ParserCallbackCheckWord
        implements ParserCallback
    {
        private List<Terminal> word;
        private Set<WordInstance> reported;
        private ValueEquivAnalysis vEquiv;
        private long startTime;

        public ParserCallbackCheckWord(List<Terminal> word,
                                       Set<WordInstance> reported,
                                       ValueEquivAnalysis vEquiv,
                                       long startTime)
        {
            this.word=word;
            this.reported=reported;
            this.vEquiv=vEquiv;
            this.startTime=startTime;
        }

        public boolean shouldAbort()
        {
            long now;

            if (Main.TIMEOUT == 0)
                return false;

            now=System.currentTimeMillis()/1000;

            return now-startTime > Main.TIMEOUT;
        }

        public void accepted(List<ParsingAction> actions,
                             NonTerminal lca)
        {
            int ret;
            WordInstance wordInst;

            wordInst=new WordInstance((PPNonTerminal)lca,word,actions);

            ret=checkWordInstance(wordInst,reported,vEquiv);

            if (ret <= 0)
                System.out.println();
        }
    };

    private AnalysisMain()
    {
        scene=null;
        moduleName=null;
        module=null;
        atomicityAnalysis=null;
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

    private int checkWordInstance(WordInstance wordInst,
                                  Set<WordInstance> reported,
                                  ValueEquivAnalysis vEquiv)
    {
        SootMethod lcaMethod;
        boolean atomic;

        gluon.profiling.Timer.start("check-word-instance");

        assert wordInst.assertLCASanityCheck();

        if (reported.contains(wordInst))
            return 1;

        if (!wordInst.argumentsMatch(vEquiv))
        {
            gluon.profiling.Profiling.inc("discarded-trees-args-not-match");
            gluon.profiling.Timer.stop("check-word-instance");
            return 1;
        }

        gluon.profiling.Profiling.inc("parse-trees");

        reported.add(wordInst);

        atomic=atomicityAnalysis.isAtomic(wordInst);

        dprintln("      Lowest common ancestor: "+wordInst.getLCA());

        lcaMethod=wordInst.getLCAMethod();

        System.out.println("      Method: "+lcaMethod.getDeclaringClass().getShortName()
                           +"."+lcaMethod.getName()+"()");

        System.out.print("      Calls Location:");

        for (PPTerminal t: wordInst.getParsingTerminals())
        {
            int linenum=t.getLineNumber();
            String source=t.getSourceFile();

            System.out.print(" "+source+":"+(linenum > 0 ? ""+linenum : "?"));
        }

        System.out.println();

        System.out.println("      Atomic: "+(atomic ? "YES" : "NO"));

        gluon.profiling.Timer.stop("check-word-instance");

        return atomic ? 0 : -1;
    }

    private void checkThreadWord(SootMethod thread,
                                 List<Terminal> word,
                                 ValueEquivAnalysis vEquiv)
        throws ParserAbortedException
    {
        Set<WordInstance> reported=new HashSet<WordInstance>();
        long startTime=System.currentTimeMillis()/1000;
        Collection<AllocNode> moduleAllocSites;

        System.out.println("  Verifying word "+WordInstance.wordStr(word)+":");
        System.out.println();

        moduleAllocSites=PointsToInformation.getModuleAllocationSites(module);

        /* Represents a static module */
        moduleAllocSites.add(null);

        for (soot.jimple.spark.pag.AllocNode as: moduleAllocSites)
        {
            Parser parser;
            BehaviorAnalysis ba=new WholeProgramBehaviorAnalysis(thread,module,as);
            ParserCallback pcb;

            if (Main.ATOMICITY_SYNCH)
            {
                MonitorAnalysis monAnalysis=new MonitorAnalysis(scene);

                monAnalysis.analyze();
                ba.setSynchMode(monAnalysis);
            }

            parser=makeParser(ba);

            dprintln("Created parser for thread "+thread.getName()
                     +"(), allocation site "+as+".");


            pcb=new ParserCallbackCheckWord(word,reported,vEquiv,startTime);

            parser.parse(word,pcb);
        }

        if (reported.size() == 0)
        {
            System.out.println("    No occurrences");
            System.out.println();
        }
    }

    private Parser makeParser(BehaviorAnalysis programBehavior)
    {
        ParsingTable parsingTable;
        Cfg grammar;

        gluon.profiling.Profiling.inc("alloc-sites-total");

        gluon.profiling.Timer.start("analysis-behavior");
        programBehavior.analyze();
        gluon.profiling.Timer.stop("analysis-behavior");

        grammar=programBehavior.getGrammar();

        gluon.profiling.Profiling.inc("grammar-productions-total",
                                      grammar.size());

        assert gluon.parsing.parser.ParserSubwords.isParsable(grammar);

        parsingTable=new ParsingTable(grammar);

        gluon.profiling.Timer.start("build-parsing-table");
        parsingTable.buildParsingTable();
        gluon.profiling.Timer.stop("build-parsing-table");

        gluon.profiling.Profiling.inc("parsing-table-state-number-total",
                                      parsingTable.numberOfStates());

        return new ParserSubwords(parsingTable);
    }

    private void checkThread(SootMethod thread)
    {
        ValueEquivAnalysis vEquiv=new ValueEquivAnalysis();

        System.out.println("Checking thread "
                           +thread.getDeclaringClass().getShortName()
                           +"."+thread.getName()+"():");
        System.out.println();

        for (List<Terminal> word: contract.getWords())
            try
            {
                checkThreadWord(thread,word,vEquiv);
            }
            catch (ParserAbortedException _)
            {
                System.out.println("    *** Timeout ***");
                System.out.println();
            }
    }

    private void initAtomicityAnalysis(Collection<SootMethod> threads)
    {
        atomicityAnalysis=new AtomicityAnalysis();

        if (Main.ATOMICITY_SYNCH)
            atomicityAnalysis.setSynchMode();
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

    private void checkClassWordConservativePointsTo(SootClass c,
                                                    List<Terminal> word,
                                                    ValueEquivAnalysis vEquiv)
        throws ParserAbortedException
    {
        Set<WordInstance> reported=new HashSet<WordInstance>();
        long startTime=System.currentTimeMillis()/1000;
        Parser parser;
        BehaviorAnalysis ba=new ClassBehaviorAnalysis(c,module);
        ParserCallback pcb;

        System.out.println("  Verifying word "+WordInstance.wordStr(word)+":");
        System.out.println();

        if (Main.ATOMICITY_SYNCH)
        {
            MonitorAnalysis monAnalysis=new MonitorAnalysis(scene);

            monAnalysis.analyze();
            ba.setSynchMode(monAnalysis);
        }

        parser=makeParser(ba);

        pcb=new ParserCallbackCheckWord(word,reported,vEquiv,startTime);

        parser.parse(word,pcb);

        if (reported.size() == 0)
        {
            System.out.println("    No occurrences");
            System.out.println();
        }
    }

    private void checkClassWordRegularPointsTo(SootClass c,
                                               List<Terminal> word,
                                               ValueEquivAnalysis vEquiv)
        throws ParserAbortedException
    {
        Set<WordInstance> reported=new HashSet<WordInstance>();
        long startTime=System.currentTimeMillis()/1000;
        Collection<AllocNode> moduleAllocSites;

        System.out.println("  Verifying word "+WordInstance.wordStr(word)+":");
        System.out.println();

        moduleAllocSites=PointsToInformation.getModuleAllocationSites(module);

        /* Represents a static module */
        moduleAllocSites.add(null);

        for (soot.jimple.spark.pag.AllocNode as: moduleAllocSites)
        {
            Parser parser;
            BehaviorAnalysis ba=new ClassBehaviorAnalysis(c,module,as);
            ParserCallback pcb;

            if (Main.ATOMICITY_SYNCH)
            {
                MonitorAnalysis monAnalysis=new MonitorAnalysis(scene);

                monAnalysis.analyze();
                ba.setSynchMode(monAnalysis);
            }

            parser=makeParser(ba);

            dprintln("Created parser for class "+c.getName()
                     +", allocation site "+as+".");

            pcb=new ParserCallbackCheckWord(word,reported,vEquiv,startTime);

            parser.parse(word,pcb);
        }

        if (reported.size() == 0)
        {
            System.out.println("    No occurrences");
            System.out.println();
        }
    }

    private void checkClassWord(SootClass c, List<Terminal> word,
                                ValueEquivAnalysis vEquiv)
        throws ParserAbortedException
    {
        if (Main.CONSERVATIVE_POINTS_TO)
            checkClassWordConservativePointsTo(c,word,vEquiv);
        else
            checkClassWordRegularPointsTo(c,word,vEquiv);
    }

    private void checkClass(SootClass c)
    {
        ValueEquivAnalysis vEquiv=new ValueEquivAnalysis();

        if (c.isJavaLibraryClass() && !gluon.Main.WITH_JAVA_LIB)
            return;

        if (c.getMethodCount() == 0)
            return;

        System.out.println("Checking class "
                           +c.getShortName()+":");
        System.out.println();

        for (List<Terminal> word: contract.getWords())
            try
            {
                checkClassWord(c,word,vEquiv);
            }
            catch (ParserAbortedException _)
            {
                System.out.println("    *** Timeout ***");
                System.out.println();
            }
    }

    private void runAnalysis()
    {
        Collection<SootMethod> threads;

        scene=Scene.v();
        assert scene.getMainMethod() != null;

        gluon.profiling.Timer.stop("soot-init");

        dprintln("Started MainAnalysis.");

        module=getModuleClass();

        if (module == null)
            Main.fatal(moduleName+": module's class not found");

        /* If the contract was not passed by the command line then extract it
         * from the module's annotation @Contract.
         */
        if (contractRaw != null)
            contract=new Contract(module,contractRaw);
        else
        {
            contract=new Contract(module);
            contract.loadAnnotatedContract();
        }

        if (contract.clauseNum() == 0)
            Main.fatal("empty contract");

        dprintln("Loaded Contract.");

        gluon.profiling.Timer.start("analysis-threads");
        threads=getThreads();
        gluon.profiling.Timer.stop("analysis-threads");

        dprintln("Obtained "+threads.size()+" thread entry points.");

        gluon.profiling.Profiling.set("threads",threads.size());

        initAtomicityAnalysis(threads);

        if (Main.CLASS_SCOPE)
            for (SootClass c: scene.getClasses())
                checkClass(c);
        else
            for (SootMethod m: threads)
                checkThread(m);
    }

    @Override
    protected void internalTransform(String paramString,
                                     @SuppressWarnings("rawtypes") java.util.Map paramMap)
    {
        runAnalysis();
    }
}
