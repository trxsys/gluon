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

public class CfgOptimizer
{
    private CfgOptimizer()
    {
        assert false;
    }

    private static Set<NonTerminal> getReachable(Cfg grammar)
    {
        Set<NonTerminal> enqueued=new HashSet<NonTerminal>();
        Queue<NonTerminal> queue=new LinkedList<NonTerminal>();

        queue.add(grammar.getStart());
        enqueued.add(grammar.getStart());
        
        while (queue.size() > 0)
        {
            NonTerminal u=queue.poll();

            for (Production p: grammar.getProductionsOf(u))
                for (LexicalElement ve: p.getBody())
                    if (ve instanceof NonTerminal)
                    {
                        NonTerminal v=(NonTerminal)ve;

                        if (!enqueued.contains(v))
                        {
                            queue.add(v);
                            enqueued.add(v);
                        }
                    }
        }

        return enqueued;
    }

    /* Returns true if the grammar was changed */
    private static boolean removeUnreachable(Cfg grammar)
    {
        Set<NonTerminal> reachable=getReachable(grammar);
        List<Production> toRemove=new LinkedList<Production>();

        for (Production p: grammar.getProductions())
            if (!reachable.contains(p.getHead()))
                toRemove.add(p);

        for (Production p: toRemove)
            grammar.removeProduction(p);

        return toRemove.size() > 0;
    }

    /* Returns true if the grammar was changed */
    private static boolean removeLoops(Cfg grammar)
    {
        List<Production> toRemove=new LinkedList<Production>();

        for (Production p: grammar.getProductions())
            if (p.bodyLength() == 1
                && p.getBody().get(0).equals(p.getHead()))
                toRemove.add(p);

        for (Production p: toRemove)
            grammar.removeProduction(p);

        return toRemove.size() > 0;
    }

    public static Cfg optimize(Cfg grammar)
    {
        Cfg grammarOpt=grammar.clone();
        boolean changed;

        do
        {
            changed=false;

            /* "changed" should be at the right side of the "or" to force the
             * evaluation of the function call.
             */
            // removeFoo();
            changed=removeLoops(grammar) || changed;
            changed=removeUnreachable(grammar) || changed;
        } while (changed);

        return grammarOpt;
    }
}
