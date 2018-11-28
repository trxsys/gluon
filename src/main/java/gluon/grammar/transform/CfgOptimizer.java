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

import gluon.grammar.*;

import java.util.*;

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


    private static boolean addNonTerminalsToQueue(Production prod,
                                                  Queue<NonTerminal> queue,
                                                  Set<NonTerminal> enqueued,
                                                  Set<NonTerminal> terminalGenerators)
    {
        /* Generates the empty word */
        if (prod.getBody().size() == 0)
        {
            terminalGenerators.add(prod.getHead());
            return true;
        }

        for (Symbol s: prod.getBody())
        {
            NonTerminal nonterm;

            if (s instanceof Terminal)
            {
                terminalGenerators.add(prod.getHead());
                return true;
            }

            assert s instanceof NonTerminal;

            nonterm=(NonTerminal)s;

            if (terminalGenerators.contains(s))
                return true;

            if (!enqueued.contains(nonterm))
            {
                queue.add(nonterm);
                enqueued.add(nonterm);
            }
        }

        return false;
    }

    private static boolean productionGenerateWord(Cfg grammar,
                                                  Production prod,
                                                  Set<NonTerminal> terminalGenerators)
    {
        Queue<NonTerminal> queue=new LinkedList<NonTerminal>();
        Set<NonTerminal> enqueued=new HashSet<NonTerminal>();

        if (addNonTerminalsToQueue(prod,queue,enqueued,terminalGenerators))
            return true;

        while (queue.size() > 0)
        {
            NonTerminal nonterm=queue.poll();

            for (Production p: grammar.getProductionsOf(nonterm))
                if (addNonTerminalsToQueue(p,queue,enqueued,terminalGenerators))
                    return true;
        }

        return false;
    }

    private static boolean removeImproductiveProductions(Cfg grammar)
    {
        List<Production> toRemove=new LinkedList<Production>();
        Set<NonTerminal> terminalGenerators=new HashSet<NonTerminal>();

        for (Production p: grammar.getProductions())
            if (!productionGenerateWord(grammar,p,terminalGenerators))
                toRemove.add(p);

        for (Production p: toRemove)
            grammar.removeProduction(p);

        return toRemove.size() > 0;
    }

    public static Cfg optimize(Cfg grammar)
    {
        Cfg grammarOpt=grammar.clone();
        boolean modified;

        dprintln("Grammar size start: "+grammarOpt.size());

        do
        {
            modified=removeImproductiveProductions(grammarOpt);

            if (removeDirectReductions(grammarOpt))
                modified=true;

            if (removeDirectLoops(grammarOpt))
                modified=true;

            dprintln("Grammar size iterating: "+grammarOpt.size());
        } while (modified);

        dprintln("Grammar size end: "+grammarOpt.size());

        return grammarOpt;
    }
}
