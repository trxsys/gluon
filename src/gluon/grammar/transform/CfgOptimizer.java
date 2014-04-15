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

package gluon.grammar.transform;

import java.util.Set;
import java.util.HashSet;
import java.util.Queue;
import java.util.List;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.Collection;

import gluon.grammar.Cfg;
import gluon.grammar.Production;
import gluon.grammar.NonTerminal;
import gluon.grammar.Symbol;

public class CfgOptimizer
{
    private static final boolean DEBUG=false;

    private CfgOptimizer()
    {
        assert false;
    }

    private static void dprintln(String s)
    {
        if (DEBUG)
            System.out.println("CfgOptimizer: "+s);
    }

    private static void replace(Cfg grammar, NonTerminal nonterm,
                                ArrayList<Symbol> string)
    {
        for (Production p: grammar.getProductionsContaining(nonterm))
        {
            grammar.removeProduction(p);

            p.replace(nonterm,string);

            grammar.addProduction(p);
        }
    }

    /* If there exists
     *
     *   B → X  (where X may be multiple symbols, and X does not contain B)
     *   B ↛ Y  (where Y ≠ X)
     *
     * remove B → X and replace every B with X.
     *
     * Returns true if the grammar was modified.
     */
    private static boolean removeDirectReductions(Cfg grammar)
    {
        boolean modified=false;

        for (NonTerminal nonterm: grammar.getNonTerminals())
        {
            Collection<Production> prodsOf;
            Production prod;

            if (nonterm.equals(grammar.getStart()))
                continue;

            prodsOf=grammar.getProductionsOf(nonterm);

            if (prodsOf.size() != 1)
                continue;

            prod=prodsOf.iterator().next();

            if (prod.getHead().noRemove())
                continue;

            /* Contains a loop (A → αAβ) */
            if (prod.getBody().contains(nonterm))
                continue;

            grammar.removeProduction(prod);

            replace(grammar,nonterm,prod.getBody());

            modified=true;
        }

        return modified;
    }

    /* Remove production of the from
     *
     *   A → A
     *
     * Returns true if the grammar was modified.
     */
    private static boolean removeDirectLoops(Cfg grammar)
    {
        boolean modified=false;

        for (Production p: grammar.getProductions())
            if (p.isDirectLoop())
                if (grammar.removeProduction(p))
                    modified=true;

        return modified;
    }

    public static Cfg optimize(Cfg grammar)
    {
        Cfg grammarOpt=grammar.clone();
        boolean modified;

        dprintln("Grammar size start: "+grammarOpt.size());

        do
        {
            modified=removeDirectReductions(grammarOpt);

            if (removeDirectLoops(grammarOpt))
                modified=true;

            dprintln("Grammar size iterating: "+grammarOpt.size());
        } while (modified);

        dprintln("Grammar size end: "+grammarOpt.size());

        return grammarOpt;
    }
}
