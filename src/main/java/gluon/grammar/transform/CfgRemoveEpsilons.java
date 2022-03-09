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

import gluon.grammar.Cfg;
import gluon.grammar.NonTerminal;
import gluon.grammar.Production;
import gluon.grammar.Symbol;

import java.util.*;

public class CfgRemoveEpsilons
{
    private static final boolean DEBUG=false;

    private CfgRemoveEpsilons()
    {
        assert false;
    }

    private static void dprintln(String s)
    {
        if (DEBUG)
            System.out.println("CfgOptimizer: "+s);
    }

    private static Collection<ArrayList<Symbol>>
        getCombinations(List<Symbol> original, NonTerminal E)
    {
        Collection<ArrayList<Symbol>> combinations
            =new ArrayList<ArrayList<Symbol>>(8);

        if (original.size() == 0)
            combinations.add(new ArrayList<Symbol>(0));
        else if (original.get(0).equals(E))
        {
            List<Symbol> tail=original.subList(1,original.size());

            for (ArrayList<Symbol> c: getCombinations(tail,E))
            {
                ArrayList<Symbol> Ec;

                combinations.add(c);

                Ec=new ArrayList<Symbol>(c.size()+1);

                Ec.add(E);
                Ec.addAll(c);

                combinations.add(Ec);
            }
        }
        else
        {
            Symbol head=original.get(0);
            List<Symbol> tail=original.subList(1,original.size());

            for (ArrayList<Symbol> c: getCombinations(tail,E))
            {
                ArrayList<Symbol> Hc=new ArrayList<Symbol>(c.size()+1);

                Hc.add(head);
                Hc.addAll(c);

                combinations.add(Hc);
            }
        }

        return combinations;
    }

    /* Remove an epsilon production while preserving the same language.
     *
     * If we have
     *
     *   A → aEbEc
     *   E → ε
     *   E → d
     *
     * will be changed to
     *
     *   A → aEbEc
     *   A → aEbc
     *   A → abEc
     *   A → abc
     *   E → d.
     *
     * If there is no alternatives in E only "A → abc" will be added.
     *
     * Removed is important because we cannot add an epsilon production which
     * was already removed, otherwise we can loop forever.
     */
    private static void removeEpsilonProduction(Cfg grammar, Production prod,
                                                Set<Production> removed)
    {
        NonTerminal E=prod.getHead();
        boolean EHasAlternatives;

        assert prod.bodyLength() == 0;

        grammar.removeProduction(prod);
        dprintln("removed "+prod);
        removed.add(prod);

        EHasAlternatives=grammar.getProductionsOf(E).size() > 0;

        for (Production p: grammar.getProductionsContaining(E))
        {
            NonTerminal A=p.getHead();

            assert p.getBody().contains(E);

            /* p is A → aEbEc */

            grammar.removeProduction(p);
            dprintln(" removed "+p);

            if (EHasAlternatives)
            {
                for (ArrayList<Symbol> symbs: getCombinations(p.getBody(),E))
                {
                    Production newP=new Production(A,symbs);

                    if (!removed.contains(newP)
                        && !newP.isDirectLoop())
                    {
                        grammar.addProduction(newP);
                        dprintln(" added "+newP);
                    }
                }
            }
            else
            {
                Production pStripped=p.clone();

                pStripped.replace(E,new ArrayList<Symbol>(0));

                if (!removed.contains(pStripped)
                    && !pStripped.isDirectLoop())
                {
                    grammar.addProduction(pStripped);
                    dprintln(" added "+pStripped);
                }
            }
        }
    }

    private static Production getEpsilonProduction(Cfg grammar)
    {
        for (Production p: grammar.getProductions())
            if (p.bodyLength() == 0)
                return p;

        return null;
    }

    /* Warning: If grammar generates the empty word removeEpsilons(grammar)
     *          will *not* have the empty word.
     */
    public static Cfg removeEpsilons(Cfg grammar)
    {
        Cfg grammarClean=grammar.clone();
        Production prod;
        Set<Production> removed=new HashSet<Production>();

        while ((prod=getEpsilonProduction(grammarClean)) != null)
            removeEpsilonProduction(grammarClean,prod,removed);

        return grammarClean;
    }
}
