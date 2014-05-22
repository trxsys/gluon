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

import gluon.grammar.Cfg;
import gluon.grammar.Production;
import gluon.grammar.transform.CfgSubwords;
import gluon.grammar.transform.CfgOptimizer;
import gluon.grammar.transform.CfgRemoveEpsilons;

import soot.SootMethod;
import soot.SootClass;
import soot.Unit;

import soot.jimple.spark.pag.AllocNode;

import java.util.HashSet;

public class ClassBehaviorAnalysis
    extends BehaviorAnalysis
{
    private SootClass classA;

    public ClassBehaviorAnalysis(SootClass c, SootClass modClass, AllocNode aSite)
    {
        super(modClass,aSite);
        classA=c;
    }

    /* For conservative points-to analisys */
    public ClassBehaviorAnalysis(SootClass c, SootClass modClass)
    {
        super(modClass);
        classA=c;
    }

    @Override
    protected void foundMethodCall(SootMethod method)
    {
    }

    @Override
    protected boolean ignoreMethodCall(SootMethod method)
    {
        /* We are doing class-scope analysis so only calls inside the same class
         * are taken into account.
         */
        return !method.getDeclaringClass().equals(classA);
    }

    public static Cfg emptyGrammar()
    {
        Cfg grammar=new Cfg();
        Production prod=new Production(new PBNonTerminal("S",null));

        grammar.addProduction(prod);

        grammar.setStart(new PBNonTerminal("S",null));

        return grammar;
    }

    @Override
    public void analyze()
    {
        gluon.profiling.Timer.start("analysis-behavior");

        assert classA.getMethodCount() > 0;

        super.visited=new HashSet<Unit>();

        for (SootMethod m: classA.getMethods())
            if (m.hasActiveBody())
            {
                PBNonTerminal nonterm;
                Production production;

                nonterm=super.analyzeMethod(m);

                production=new Production(new PBNonTerminal("S'",null));

                production.appendToBody(nonterm);

                super.grammar.addProduction(production);
            }

        super.visited=null;

        if (grammar.size() == 0)
        {
            super.grammar=emptyGrammar();
            return;
        }

        super.grammar.setStart(new PBNonTerminal("S'",null));

        gluon.profiling.Timer.start("grammar-rm-epsilon");
        super.grammar=CfgRemoveEpsilons.removeEpsilons(super.grammar);
        gluon.profiling.Timer.stop("grammar-rm-epsilon");

        gluon.profiling.Timer.start("grammar-opt");
        super.grammar=CfgOptimizer.optimize(super.grammar);
        gluon.profiling.Timer.stop("grammar-opt");

        super.addNewStart();

        super.dprintln("Grammar: "+super.grammar);

        assert super.grammar.hasUniqueStart();

        gluon.profiling.Timer.stop("analysis-behavior");
    }
}
