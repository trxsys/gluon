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



    /* TODO */
    private Collection<ParsingAction>
        getSuccessorActions(ParserConfiguration parserConf,
                            List<Terminal> input)
    {
        final EOITerminal EOI=new EOITerminal();
        final ParsingActionAccept ACCEPT=new ParsingActionAccept();
        Collection<ParsingAction> actions;
        int state=parserConf.getState();
        Collection<Item> items;

        items=super.table.getStateItems(state);

        actions=new ArrayList<ParsingAction>(items.size()+1);

        for (Item item: items)
        {
            Production partialProd=item.getPartialProduction();
            ParsingActionReduce red=new ParsingActionReduce(partialProd);

            actions.add(red);
        }

        if (super.table.actions(state,EOI).contains(ACCEPT))
        {
            assert items.size() == 0 : "When we find an action there should "
                +"not be any possible alternative";

            actions.add(new ParsingActionAccept());
        }

        return actions;
    }




    /* Apply the reductions until we reach the start nonterminal.
     */
    private void
        completeRemainingReductions(ParserConfiguration parent, List<Terminal> input)
        throws ParserAbortedException
    {
        Stack<ParserConfiguration> parseLifo;
        int counter=0; /* For calling pcb.shouldStop() */

        gluon.profiling.Timer.start("parsing-completeRR");

        parseLifo=new Stack<ParserConfiguration>();
        parseLifo.add(parent);

        while (parseLifo.size() > 0)
        {
            ParserConfiguration parserConf=parseLifo.pop();

            counter=(counter+1)%500000;

            if (counter == 0 && parserCB.shouldAbort())
            {
                gluon.profiling.Timer.stop("parsing-completeRR");
                gluon.profiling.Timer.stop("parsing");
                throw new ParserAbortedException();
            }

            for (ParsingAction action: getSuccessorActions(parserConf,input))
            {
                Collection<ParserConfiguration> branches=null;

                if (action instanceof ParsingActionShift)
                    branches=shift(parserConf,(ParsingActionShift)action,input);
                else if (action instanceof ParsingActionReduce)
                    branches=reduce(parserConf,(ParsingActionReduce)action,input);
                else if (action instanceof ParsingActionAccept)
                    branches=accept(parserConf,(ParsingActionAccept)action,input);
                else
                    assert false;

                for (ParserConfiguration branch: branches)
                {
                    int inputTerminals;

                    if (parserConf.isLoop(branch))
                    {
                        gluon.profiling.Profiling.inc("parse-branches");
                        continue;
                    }

                    inputTerminals=input.size()
                        -(input.get(input.size()-1) instanceof EOITerminal ? 1 : 0);

                    if (branch.getTerminalNum() == inputTerminals
                        && branch.lca == null
                        && action instanceof ParsingActionReduce)
                    {
                        ParsingActionReduce red=(ParsingActionReduce)action;
                        boolean cont;

                        branch.lca=red.getProduction().getHead();

                        gluon.profiling.Timer.stop("parsing");
                        gluon.profiling.Timer.stop("parsing-completeRR");
                        cont=parserCB.onLCA(branch.getActionList(),branch.lca);
                        gluon.profiling.Timer.start("parsing-completeRR");
                        gluon.profiling.Timer.start("parsing");

                        if (!cont)
                        {
                             gluon.profiling.Profiling.inc("parse-branches");
                             continue;
                        }
                    }

                    parseLifo.add(branch);
                }
            }
        }

        gluon.profiling.Timer.stop("parsing-completeRR");
    }

    @Override
    protected Collection<ParserConfiguration> shift(ParserConfiguration parent,
                                                    ParsingActionShift shift,
                                                    List<Terminal> input)
        throws ParserAbortedException
    {
        ParserConfiguration parserConf;

        parserConf=super.shift(parent,shift,input).iterator().next();

        if (parserConf.pos >= input.size())
        {
            completeRemainingReductions(parserConf,input);

            return new ArrayList<ParserConfiguration>();
        }

        return Collections.singleton(parserConf);
    }

    private ParsingActionReduce getSufixRedution(Production p, int takeOnly)
    {
        Production sufixProd;

        sufixProd=new Production(p.getHead(),
                                 p.getBody().subList(p.bodyLength()-takeOnly,
                                                     p.bodyLength()));

        assert sufixProd.bodyLength() == takeOnly;

        return new ParsingActionReduce(sufixProd);
    }

    private Collection<ParserConfiguration>
        reduceWithReachedByJump(ParserConfiguration parent,
                                ParsingActionReduce reduction)
    {
            ParserConfiguration parserConf;
            Production p=reduction.getProduction();
            Collection<ParserConfiguration> configs;
            int genTerminals;

            configs=new LinkedList<ParserConfiguration>();

            parserConf=super.newParserConfiguration(parent);
            genTerminals=super.stackPop(parserConf,p.bodyLength());

            parserConf.setAction(reduction);

            for (int s: super.table.statesReachedBy(p.getHead()))
            {
                ParserConfiguration succParserConfig=parserConf.clone();

                succParserConfig.getStack().push(new ParsingStackNode(s,genTerminals));

                configs.add(succParserConfig);
            }

            return configs;
    }

    /* The algorithm implemented here is from "Substring parsing for arbitrary
     * context-free grammars" by Jan Rekers and Wilco Koorn.
     */
    @Override
    protected Collection<ParserConfiguration> reduce(ParserConfiguration parent,
                                                     ParsingActionReduce reduction,
                                                     List<Terminal> input)
        throws ParserAbortedException
    {
        Collection<ParserConfiguration> configs;
        Production p=reduction.getProduction();
        int stackSize=parent.getStack().size();

        if (stackSize > p.bodyLength())
            configs=super.reduce(parent,reduction,input);
        else if (stackSize < p.bodyLength())
        {
            ParsingActionReduce sufixReduction;

            sufixReduction=getSufixRedution(p,stackSize);

            configs=reduceWithReachedByJump(parent,sufixReduction);
        }
        else
        {
            assert stackSize == p.bodyLength();

            configs=reduceWithReachedByJump(parent,reduction);
        }

        return configs;
    }

    @Override
    protected Collection<ParserConfiguration>
        getInitialConfigurations(List<Terminal> input)
        throws ParserAbortedException
    {
        Collection<ParserConfiguration> configurations;

        configurations=new ArrayList<ParserConfiguration>(128);

        for (int s: super.table.statesReachedBy(input.get(0)))
        {
            ParserConfiguration initConfig;
            ParsingActionShift shift=new ParsingActionShift(s);

            initConfig=shift(null,shift,input).iterator().next();

            configurations.add(initConfig);
        }

        return configurations;
    }

    @Override
    public void parse(List<Terminal> input, ParserCallback pcb)
        throws ParserAbortedException
    {
        assert input.size() > 0;
        assert !(input.get(input.size()-1) instanceof EOITerminal)
            : "input should not end with $";

        super.parse(input,pcb);
    }
}
