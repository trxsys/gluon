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

import java.util.Collection;
import java.util.Queue;
import java.util.List;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Stack;

import java.util.Collections;

import gluon.parsing.parsingTable.ParsingTable;
import gluon.grammar.Cfg;
import gluon.grammar.Production;
import gluon.grammar.Symbol;
import gluon.grammar.NonTerminal;
import gluon.grammar.Terminal;
import gluon.grammar.EOITerminal;

import gluon.parsing.parsingTable.ParsingTable;
import gluon.parsing.parsingTable.Item;
import gluon.parsing.parsingTable.parsingAction.*;

/* Subword parser as described in "Substring parsing for arbitrary context-free
 * grammars", J Rekers, W Koorn, 1991.
 */
public class ParserSubwords
    extends Parser
{
    public ParserSubwords(ParsingTable t)
    {
        super(t);
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

    /* Apply the reductions until we reach the start nonterminal.
     */
    private Collection<ParserConfiguration>
        completeRemainingReductions(ParserConfiguration parent, List<Terminal> input)
    {
        final EOITerminal EOI=new EOITerminal();
        final ParsingActionAccept ACCEPT=new ParsingActionAccept();
        Stack<ParserConfiguration> parseLifo;
        Collection<ParserConfiguration> accepts;

        gluon.profiling.Timer.start("final:total-parsing-completeRR");

        accepts=new LinkedList<ParserConfiguration>();

        parseLifo=new Stack<ParserConfiguration>();
        parseLifo.add(parent);

        while (parseLifo.size() > 0)
        {
            ParserConfiguration parserConf=parseLifo.pop();
            int state=parserConf.stackPeek().state;

            // TODO maybe timeout here as well?

            if (super.table.actions(state,EOI).contains(ACCEPT))
            {
                Collection<ParserConfiguration> a;

                a=super.accept(parserConf,ACCEPT,input);

                assert a.size() == 1;

                accepts.addAll(a);

                continue;
            }

            for (Item item: super.table.getStateItems(state))
            {
                Production partialProd=item.getPartialProduction();
                ParsingActionReduce red=new ParsingActionReduce(partialProd);
                Collection<ParserConfiguration> parserConfs;

                parserConfs=this.reduce(parserConf,red,input);

                for (ParserConfiguration conf: parserConfs)
                {
                    if (parserConf.isLoop(conf))
                        continue;

                    if (conf.getTerminalNum() == input.size()
                        && conf.lca == null)
                        conf.lca=red.getProduction().getHead();

                    parseLifo.add(conf);
                }
            }
        }

        gluon.profiling.Timer.stop("final:total-parsing-completeRR");

        return accepts;
    }

    @Override
    protected Collection<ParserConfiguration> shift(ParserConfiguration parent,
                                                    ParsingActionShift shift,
                                                    List<Terminal> input)
    {
        ParserConfiguration parserConf;

        parserConf=super.shift(parent,shift,input).iterator().next();

        if (parserConf.pos >= input.size())
            return completeRemainingReductions(parserConf,input);

        return Collections.singleton(parserConf);
    }

    /* The algorithm implemented here is from "Substring parsing for arbitrary
     * context-free grammars" by Jan Rekers and Wilco Koorn.
     */
    @Override
    protected Collection<ParserConfiguration> reduce(ParserConfiguration parent,
                                                     ParsingActionReduce reduction,
                                                     List<Terminal> input)
    {
        Collection<ParserConfiguration> configs;
        Production p=reduction.getProduction();
        int stackSize=parent.stackSize();

        configs=new LinkedList<ParserConfiguration>();

        if (stackSize > p.bodyLength())
            configs=super.reduce(parent,reduction,input);
        else if (stackSize < p.bodyLength())
        {
            ParserConfiguration parserConf;
            ParsingActionReduce sufixReduction;
            Production sufixProd;
            int genTerminals;

            /* TODO method to create this new reduction */
            sufixProd=new Production(p.getHead(),
                                     p.getBody().subList(p.bodyLength()-stackSize,
                                                         p.bodyLength()));

            assert sufixProd.bodyLength() == stackSize;

            sufixReduction=new ParsingActionReduce(sufixProd);

            /* TODO refactor this */
            parserConf=super.newParserConfiguration(parent);
            genTerminals=super.stackPop(parserConf,sufixProd.bodyLength());

            parserConf.setAction(sufixReduction);

            for (int s: super.table.statesReachedBy(p.getHead()))
            {
                ParserConfiguration succParserConfig=parserConf.clone();

                succParserConfig.stackPush(new ParserStackNode(s,genTerminals));

                configs.add(succParserConfig);
            }
        }
        else
        {
            ParserConfiguration parserConf;
            int genTerminals;

            assert stackSize == p.bodyLength();

            /* TODO refactor this */
            parserConf=super.newParserConfiguration(parent);
            genTerminals=super.stackPop(parserConf,p.bodyLength());

            parserConf.setAction(reduction);

            for (int s: super.table.statesReachedBy(p.getHead()))
            {
                ParserConfiguration succParserConfig=parserConf.clone();

                succParserConfig.stackPush(new ParserStackNode(s,genTerminals));

                configs.add(succParserConfig);
            }
        }

        return configs;
    }

    @Override
    protected Collection<ParserConfiguration>
        getInitialConfigurations(List<Terminal> input)
    {
        Collection<ParserConfiguration> configurations;

        configurations=new ArrayList<ParserConfiguration>(128);

        for (int s: super.table.statesReachedBy(input.get(0)))
        {
            ParserConfiguration initConfig=new ParserConfiguration();

            /* We have alread read the terminal at input[0], hence the "1" passed
             * to the ParserStackNode contructor.
             */
            initConfig.stackPush(new ParserStackNode(s,1));
            initConfig.pos=1; /* we already "read" the first terminal */

            initConfig.setAction(new ParsingActionShift(s));

            configurations.add(initConfig);
        }

        return configurations;
    }

    @Override
    public int parse(List<Terminal> input, ParserCallback pcb)
        throws ParserAbortedException
    {
        assert input.size() > 0;
        assert !(input.get(input.size()-1) instanceof EOITerminal)
            : "input should not end with $";

        return super.parse(input,pcb);
    }
}
