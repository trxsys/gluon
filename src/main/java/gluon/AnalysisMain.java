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

import gluon.analysis.atomicity.AtomicityAnalysis;
import gluon.analysis.monitor.MonitorAnalysis;
import gluon.analysis.pointsTo.PointsToInformation;
import gluon.analysis.programBehavior.*;
import gluon.analysis.thread.ThreadAnalysis;
import gluon.analysis.valueEquivalence.ValueEquivAnalysis;
import gluon.contract.Contract;
import gluon.grammar.Cfg;
import gluon.grammar.NonTerminal;
import gluon.grammar.Terminal;
import gluon.parsing.parser.Parser;
import gluon.parsing.parser.ParserAbortedException;
import gluon.parsing.parser.ParserCallback;
import gluon.parsing.parser.ParserSubwords;
import gluon.parsing.parsingTable.ParsingTable;
import gluon.parsing.parsingTable.parsingAction.ParsingAction;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootClass;
import soot.SootMethod;
import soot.jimple.spark.pag.AllocNode;

import java.util.*;

public class AnalysisMain
    extends SceneTransformer
{
    private static final boolean DEBUG=false;

    private static final AnalysisMain instance=new AnalysisMain();

    private Scene scene;
    private String moduleName; /* Name of the module to analyze */
    private SootClass module;  /* Class of the module to analyze */
    private Contract contract;

    private String contractRaw;

    /* TODO: we should have a class called AnalysisContext */
    class ParserCallbackCheckWord
        implements ParserCallback
    {
        private List<Terminal> word;
        private Set<WordInstance> verifiedWords;
        private AtomicityAnalysis atomicityAnalysis;
        private ValueEquivAnalysis vEquiv;
        private long startTime;

        private int reported;

        public ParserCallbackCheckWord(List<Terminal> word,
                                       Set<WordInstance> verifiedWords,
                                       AtomicityAnalysis atomicityAnalysis,
                                       ValueEquivAnalysis vEquiv,
                                       long startTime)
        {
            this.word=word;
            this.verifiedWords=verifiedWords;
            this.atomicityAnalysis=atomicityAnalysis;
            this.vEquiv=vEquiv;
            this.startTime=startTime;

            this.reported=0;
        }

        public int getReported()
        {
            return reported;
        }

        public boolean onLCA(List<ParsingAction> actions, NonTerminal lca)
        {
            WordInstance wordInst;
            int ret;

            gluon.profiling.Timer.start(".onLCA");

            wordInst=new WordInstance((PBNonTerminal)lca,word,actions);

            if (verifiedWords.contains(wordInst))
            {
                gluon.profiling.Timer.stop(".onLCA");
                return false;
            }

            ret=checkWordInstance(wordInst,atomicityAnalysis,vEquiv);

            verifiedWords.add(wordInst);

            if (ret <= 0)
                reported++;

            if (ret <= 0)
                System.out.println();

            gluon.profiling.Timer.stop(".onLCA");

            return false;
        }

        public boolean shouldAbort()
        {
            long now;

            if (Main.TIMEOUT == 0)
                return false;

            now=System.currentTimeMillis()/1000;

            return now-startTime > Main.TIMEOUT;
        }

        public void accepted(List<ParsingAction> actions, NonTerminal lca)
        {
            assert false : "We should be prunning everything on LCA";
        }
    };

    private AnalysisMain()
    {
        scene=null;
        moduleName=null;
        module=null;
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

    /* Returns > 0 if the word is ignored;
     *         = 0 if the word is atomic;
     *         < 0 if the word is a contract violation.
     */
    private int checkWordInstance(WordInstance wordInst,
                                  AtomicityAnalysis atomicity,
                                  ValueEquivAnalysis vEquiv)
    {
        SootMethod lcaMethod;
        boolean atomic;

        gluon.profiling.Timer.start("check-word-instance");

        if (!wordInst.argumentsMatch(vEquiv))
        {
            gluon.profiling.Profiling.inc("discarded-trees-args-not-match");
            gluon.profiling.Timer.stop("check-word-instance");
            return 1;
        }

        gluon.profiling.Profiling.inc("parse-trees");

        atomic=atomicity.isAtomic(wordInst);

        dprintln("      Lowest common ancestor: "+wordInst.getLCA());

        lcaMethod=wordInst.getLCAMethod();

        System.out.println("      Method: "+lcaMethod.getDeclaringClass().getShortName()
                +"."+lcaMethod.getName()+"()");

        System.out.print("      Calls Location:");

        for (PBTerminal t: wordInst.getParsingTerminals())
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

    private static List<String> wordToStrings(List<Terminal> word)
    {
        List<String> strings=new ArrayList<String>(word.size());

        for (int i=0; i < word.size(); i++)
            strings.add(word.get(i).getName());

        return strings;
    }

    private void checkThreadWord(SootMethod thread,
                                 List<Terminal> word,
                                 ValueEquivAnalysis vEquiv)
        throws ParserAbortedException
    {
        Set<WordInstance> verifiedWords=new HashSet<WordInstance>();
        long startTime=System.currentTimeMillis()/1000;
        Collection<AllocNode> moduleAllocSites;
        int reported=0;

        System.out.println("  Verifying clause "+WordInstance.wordStr(word)+":");
        System.out.println();

        moduleAllocSites=PointsToInformation.getModuleAllocationSites(module);

        /* Represents a static module */
        moduleAllocSites.add(null);

        for (soot.jimple.spark.pag.AllocNode as: moduleAllocSites)
        {
            Parser parser;
            Cfg grammar;
            BehaviorAnalysis ba;
            AtomicityAnalysis aa;
            ParserCallbackCheckWord pcb;

            gluon.profiling.Profiling.inc("alloc-sites");

            ba=new WholeProgramBehaviorAnalysis(thread,module,as,wordToStrings(word));

            if (Main.ATOMICITY_SYNCH)
            {
                MonitorAnalysis monAnalysis=new MonitorAnalysis(scene);

                monAnalysis.analyze();
                ba.setSynchMode(monAnalysis);
            }

            ba.analyze();

            grammar=ba.getGrammar();

            aa=new AtomicityAnalysis(grammar);

            aa.analyze();

            parser=makeParser(grammar);

            dprintln("Created parser for thread "+thread.getName()
                    +"(), allocation site "+as+".");

            pcb=new ParserCallbackCheckWord(word,verifiedWords,aa,vEquiv,startTime);

            parser.parse(word,pcb);

            reported+=pcb.getReported();
        }

        if (reported == 0)
        {
            System.out.println("    No occurrences");
            System.out.println();
        }
    }

    private Parser makeParser(Cfg grammar)
    {
        ParsingTable parsingTable;

        gluon.profiling.Profiling.inc("grammar-productions",grammar.size());

        assert gluon.parsing.parser.ParserSubwords.isParsable(grammar);

        parsingTable=new ParsingTable(grammar);

        parsingTable.buildParsingTable();

        gluon.profiling.Profiling.inc("parsing-table-states",
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
            catch (ParserAbortedException e)
            {
                System.out.println("    *** Timeout ***");
                System.out.println();
            }
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
        Set<WordInstance> verifiedWords=new HashSet<WordInstance>();
        long startTime=System.currentTimeMillis()/1000;
        Parser parser;
        Cfg grammar;
        BehaviorAnalysis ba;
        AtomicityAnalysis aa;
        ParserCallbackCheckWord pcb;

        System.out.println("  Verifying clause "+WordInstance.wordStr(word)+":");
        System.out.println();

        ba=new ClassBehaviorAnalysis(c,module,wordToStrings(word));

        if (Main.ATOMICITY_SYNCH)
        {
            MonitorAnalysis monAnalysis=new MonitorAnalysis(scene);

            monAnalysis.analyze();
            ba.setSynchMode(monAnalysis);
        }

        ba.analyze();

        grammar=ba.getGrammar();

        aa=new AtomicityAnalysis(grammar);

        aa.analyze();

        parser=makeParser(grammar);

        pcb=new ParserCallbackCheckWord(word,verifiedWords,aa,vEquiv,startTime);

        parser.parse(word,pcb);

        if (pcb.getReported() == 0)
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
        Set<WordInstance> verifiedWords=new HashSet<WordInstance>();
        long startTime=System.currentTimeMillis()/1000;
        Collection<AllocNode> moduleAllocSites;
        int reported=0;

        System.out.println("  Verifying clause "+WordInstance.wordStr(word)+":");
        System.out.println();

        moduleAllocSites=PointsToInformation.getModuleAllocationSites(module);

        /* Represents a static module */
        moduleAllocSites.add(null);

        for (soot.jimple.spark.pag.AllocNode as: moduleAllocSites)
        {
            Parser parser;
            Cfg grammar;
            BehaviorAnalysis ba;
            AtomicityAnalysis aa;
            ParserCallbackCheckWord pcb;

            gluon.profiling.Profiling.inc("alloc-sites");

            ba=new ClassBehaviorAnalysis(c,module,as,wordToStrings(word));

            if (Main.ATOMICITY_SYNCH)
            {
                MonitorAnalysis monAnalysis=new MonitorAnalysis(scene);

                monAnalysis.analyze();
                ba.setSynchMode(monAnalysis);
            }

            ba.analyze();

            grammar=ba.getGrammar();

            aa=new AtomicityAnalysis(grammar);

            aa.analyze();

            parser=makeParser(grammar);

            dprintln("Created parser for class "+c.getName()
                    +", allocation site "+as+".");

            pcb=new ParserCallbackCheckWord(word,verifiedWords,aa,vEquiv,startTime);

            parser.parse(word,pcb);

            reported+=pcb.getReported();
        }

        if (reported == 0)
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
            catch (ParserAbortedException e)
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

        threads=getThreads();

        dprintln("Obtained "+threads.size()+" thread entry points.");

        gluon.profiling.Profiling.set("threads",threads.size());

        if (Main.CLASS_SCOPE)
        {
            for (SootClass c: scene.getClasses())
                if (!module.equals(c))
                    checkClass(c);
        }
        else
        {
            // TODO Since Java 8 the language also contains lambda functions that can be used as entrypoints for thread.
            //      Are they also represented as a `SootMethod`?
            for (SootMethod m: threads)
                checkThread(m);
        }
    }

    @Override
    protected void internalTransform(String paramString,
                                     @SuppressWarnings("rawtypes") java.util.Map paramMap)
    {
        runAnalysis();
    }
}
