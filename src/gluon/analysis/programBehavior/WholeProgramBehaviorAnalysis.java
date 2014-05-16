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

package gluon.analysis.programBehavior;

import gluon.grammar.Production;
import gluon.grammar.NonTerminal;
import gluon.grammar.transform.CfgSubwords;
import gluon.grammar.transform.CfgOptimizer;
import gluon.grammar.transform.CfgRemoveEpsilons;

import java.util.Set;
import java.util.HashSet;
import java.util.Queue;
import java.util.LinkedList;

import soot.SootClass;
import soot.SootMethod;
import soot.Unit;

import soot.jimple.spark.pag.AllocNode;

public class WholeProgramBehaviorAnalysis
    extends BehaviorAnalysis
{
    private SootMethod entryMethod;

    private Queue<SootMethod> methodQueue; /* queue of methods to analyse */
    private Set<SootMethod> enqueuedMethods;

    public WholeProgramBehaviorAnalysis(SootMethod thread, SootClass modClass,
                                        AllocNode aSite)
    {
        super(modClass,aSite);

        entryMethod=thread;

        methodQueue=null;
        enqueuedMethods=null;
    }

    private void analyzeReachableMethods(SootMethod entryMethod)
    {
        super.visited=new HashSet<Unit>();

        methodQueue=new LinkedList<SootMethod>();
        enqueuedMethods=new HashSet<SootMethod>();

        methodQueue.add(entryMethod);
        enqueuedMethods.add(entryMethod);

        while (methodQueue.size() > 0)
        {
            SootMethod method=methodQueue.poll();

            super.analyzeMethod(method);
        }

        enqueuedMethods=null;
        methodQueue=null;

        super.visited=null;
    }

    @Override
    protected void foundMethodCall(SootMethod method)
    {
        if (!enqueuedMethods.contains(method)
            && method.hasActiveBody())
        {
            methodQueue.add(method);
            enqueuedMethods.add(method);
        }
    }

    @Override
    protected boolean ignoreMethodCall(SootMethod method)
    {
        /* We are doing whole program analysis so every call is taken
         * into account.
         */
        return false;
    }

    @Override
    public void analyze()
    {
        analyzeReachableMethods(entryMethod);

        super.grammar.setStart(new PPNonTerminal(super.alias(entryMethod),entryMethod));

        gluon.profiling.Timer.start("analysis-behavior-grammar-rm-epsilon");
        super.grammar=CfgRemoveEpsilons.removeEpsilons(super.grammar);
        gluon.profiling.Timer.stop("analysis-behavior-grammar-rm-epsilon");

        gluon.profiling.Timer.start("analysis-behavior-grammar-opt");
        super.grammar=CfgOptimizer.optimize(super.grammar);
        gluon.profiling.Timer.stop("analysis-behavior-grammar-opt");

        super.addNewStart();

        super.dprintln("Grammar: "+super.grammar);

        assert super.grammar.hasUniqueStart();
    }
}
