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

package gluon.grammar;

import java.util.Set;
import java.util.HashSet;
import java.util.Queue;
import java.util.List;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.Collection;

public class CfgOptimizer
{
    private static final boolean DEBUG=true;

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
                                ArrayList<LexicalElement> string)
    {
        Collection<Production> prods=new LinkedList<Production>();

        prods.addAll(grammar.getProductionsContaining(nonterm));

        for (Production p: prods)
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

            if (grammar.getStart().equals(nonterm)
                || nonterm.noRemove())
                continue;

            prodsOf=grammar.getProductionsOf(nonterm);

            if (prodsOf.size() != 1)
                continue;

            prod=prodsOf.iterator().next();

            if (prod.getBody().contains(nonterm))
                continue;

            grammar.removeProduction(prod);

            replace(grammar,nonterm,prod.getBody());

            modified=true;
        }

        return modified;
    }

    private static boolean isDirectLoop(Production prod)
    {
        return prod.bodyLength() == 1
            && prod.getBody().get(0).equals(prod.getHead());
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
            if (isDirectLoop(p))
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

            // dprintln("Grammar size iterating: "+grammarOpt.size());
        } while (modified);

        /* TODO: this might get rid of all the unproductive 
         *       loops in the grammar. maybe we
         *       don't need to do loop detection in the parser
         */

        dprintln("Grammar size end: "+grammarOpt.size());

        dprintln("Grammar: ");
        dprintln(grammarOpt.toString());

        return grammarOpt;
    }
}
