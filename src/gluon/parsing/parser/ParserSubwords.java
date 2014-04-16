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

package gluon.parsing.parser;

import java.util.Queue;
import java.util.List;
import java.util.LinkedList;
import java.util.Set;
import java.util.HashSet;

import gluon.parsing.parsingTable.ParsingTable;
import gluon.grammar.Cfg;
import gluon.grammar.Production;
import gluon.grammar.Symbol;
import gluon.grammar.NonTerminal;
import gluon.grammar.Terminal;

/* Subword parser as described in "Substring parsing for arbitrary context-free
 * grammars", J Rekers, W Koorn, 1991.
 */
public class ParserSubwords
{
    public ParserSubwords(ParsingTable t)
    {
        /*
        table=t;
        parseLifo=null;
        acceptedLCA=null;
        */
    }

    private static boolean addNonTerminalsToQueue(Production prod,
                                                  Queue<NonTerminal> queue,
                                                  Set<NonTerminal> enqueued,
                                                  Set<NonTerminal> terminalGenerators)
    {
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

    private static boolean productionGenerateTerminal(Cfg grammar,
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
    
    /* For the subword parser to be able to parse the grammar every production
     * must generate at least one non-empty word.
     */
    public static boolean isParsable(Cfg grammar)
    {
        Set<NonTerminal> terminalGenerators=new HashSet<NonTerminal>();
        boolean emptyGrammar=true;
        boolean fail=false;

        Production fprod=null;

        for (Production p: grammar.getProductions())
            if (productionGenerateTerminal(grammar,p,terminalGenerators))
                emptyGrammar=false;
            else
            {
                fail=true;
                fprod=p;
            }

        /* It's always ok if the grammar is empty */
        if (emptyGrammar)
            return true;

        return !fail;
    }
}
