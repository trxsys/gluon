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
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.Iterator;

import java.util.Collections;

import gluon.grammar.Production;
import gluon.grammar.Terminal;
import gluon.grammar.NonTerminal;
import gluon.grammar.EOITerminal;

import gluon.parsing.parsingTable.ParsingTable;
import gluon.parsing.parsingTable.parsingAction.*;

class ParsingStackNode
{
    public final int state;
    public final int generatedTerminals;

    public ParsingStackNode parent;

    public ParsingStackNode(int s, int t)
    {
        state=s;
        generatedTerminals=t;
        parent=null;
    }
}

class ParsingStack
{
    private ParsingStackNode top;
    private int size;

    public ParsingStack()
    {
        size=0;
        top=null;
    }

    public int size()
    {
        return size;
    }

    public ParsingStackNode peek()
    {
        assert top != null;

        return top;
    }

    public void push(ParsingStackNode node)
    {
        assert node.parent == null;

        node.parent=top;
        top=node;

        size++;
    }

    public ParsingStackNode pop()
    {
        ParsingStackNode node=top;

        assert top != null;

        top=top.parent;

        size--;

        return top;
    }

    public ParsingStack clone()
    {
        ParsingStack clone=new ParsingStack();

        clone.top=top;
        clone.size=size;

        return clone;
    }
}

/* This class represents the top state of the parser configuration, used to
 * merge parser configurations when they reach a mergable state.
 */
class ParserConfigurationTopState
{
    private int state;
    private int pos;

    public ParserConfigurationTopState(int s, int p)
    {
        state=s;
        pos=p;
    }

    @Override
    public boolean equals(Object o)
    {
        ParserConfigurationTopState other;

        assert o instanceof ParserConfigurationTopState;

        other=(ParserConfigurationTopState)o;

        return other.state == state && other.pos == pos;
    }

    @Override
    public int hashCode()
    {
        return state^(92821*pos);
    }
}

class ParserConfiguration
{
    private class ParserHistory
    {
        public final ParsingAction action;
        public final int generatedTerminals;

        public ParserHistory(ParsingAction a, int t)
        {
            action=a;
            generatedTerminals=t;
        }
    }

    private ParsingStack stack;

    private LinkedList<ParserHistory> history;

    public NonTerminal lca;

    public int pos;

    /* Bitmap bloom filter used to improve isLoop() performance.
     *
     * This filter represents the set of nonterminals reduced since the last
     * shift.  For instance if we have the following actions:
     *
     *    < ..., shift, red[A → B] >
     *
     * then bloomFilter.mayContain(A).  We use this to quickly return false
     * in isLoop() if no reduction is made [since the last shift] to a given
     * nonterminal.
     *
     * setAction() maintains this bloom filter.
     */
    private static final int RED_BF_SIZE=61; /* a prime number */
    private long reductionsBloomFilter;

    public ParserConfiguration()
    {
        history=new LinkedList<ParserHistory>();

        stack=new ParsingStack();
        lca=null;
        pos=0;
        reductionsBloomFilter=0;
    }

    private void redBFClear()
    {
        reductionsBloomFilter=0;
    }

    private void redBFAdd(NonTerminal nonterm)
    {
        int bit=nonterm.hashCode()%RED_BF_SIZE;

        reductionsBloomFilter=reductionsBloomFilter|(1L<<bit);
    }

    private boolean redBFmayContain(NonTerminal nonterm)
    {
        int bit=nonterm.hashCode()%RED_BF_SIZE;

        return (reductionsBloomFilter&(1L<<bit)) != 0;
    }

    /* This should always be called *after* the stack is changed as a result of
     * this action.
     */
    public void addAction(ParsingAction action)
    {
        if (action instanceof ParsingActionShift)
            redBFClear();
        else if (action instanceof ParsingActionReduce)
            redBFAdd(((ParsingActionReduce)action).getProduction().getHead());

        history.add(new ParserHistory(action,getTerminalNum()));
    }

    public ParsingAction getLastAction()
    {
        return history.getLast().action;
    }

    public List<ParsingAction> getActionList()
    {
        List<ParsingAction> actions=new LinkedList<ParsingAction>();

        for (ParserHistory ph: history)
            actions.add(ph.action);

        return actions;
    }

    public ParsingStack getStack()
    {
        return stack;
    }

    public boolean isLoop(ParserConfiguration conf)
    {
        NonTerminal redHead;
        int genTerminals;

        if (!(conf.getLastAction() instanceof ParsingActionReduce))
            return false;

        gluon.profiling.Timer.start(".isLoop");

        redHead=((ParsingActionReduce)conf.getLastAction()).getProduction()
                                                           .getHead();
        genTerminals=conf.getTerminalNum();

        if (!redBFmayContain(redHead))
        {
            gluon.profiling.Timer.stop(".isLoop");
            return false;
        }

        for (Iterator<ParserHistory> it=history.descendingIterator();
             it.hasNext(); )
        {
            ParserHistory ph=it.next();
            ParsingAction action=ph.action;
            ParsingActionReduce ancRed;
            NonTerminal ancRedHead;
            int ancGenTerminals;

            /* This means that we encontered a shift which is productive,
             * therefore we can stop now.
             */
            if (action instanceof ParsingActionShift)
            {
                gluon.profiling.Timer.stop(".isLoop");
                return false;
            }

            assert action instanceof ParsingActionReduce;

            ancGenTerminals=ph.generatedTerminals;

            if (ancGenTerminals < genTerminals)
            {
                gluon.profiling.Timer.stop(".isLoop");
                return false;
            }

            ancRed=(ParsingActionReduce)action;
            ancRedHead=ancRed.getProduction().getHead();

            if (redHead.equals(ancRedHead))
            {
                gluon.profiling.Timer.stop(".isLoop");
                return true;
            }
        }

        gluon.profiling.Timer.stop(".isLoop");

        return false;
    }

    public int getTerminalNum()
    {
        return stack.peek().generatedTerminals;
    }

    public int getState()
    {
        return stack.peek().state;
    }

    public Collection<ParsingAction> getSuccessorActions(ParsingTable table,
                                                         List<Terminal> input)
    {
        Terminal term;
        int state;

        assert pos < input.size();

        state=getState();
        term=input.get(pos);

        return table.actions(state,term);
    }

    public ParserConfigurationTopState getTopState()
    {
        return new ParserConfigurationTopState(getState(),pos);
    }

    protected void copy(ParserConfiguration src)
    {
        stack=src.stack.clone();

        history=new LinkedList<ParserHistory>();
        history.addAll(src.history);

        lca=src.lca;
        pos=src.pos;
        reductionsBloomFilter=src.reductionsBloomFilter;
    }

    public ParserConfiguration clone()
    {
        ParserConfiguration clone=new ParserConfiguration();

        clone.copy(this);

        return clone;
    }
}

/* This is a partial implementation of the tomita parser. We do not merge
 * configuration states as described in the full tomita implementation.
 *
 * This parser also detects and prune branches with unproductive loops in the
 * grammar.
 */
public abstract class Parser
{
    private static final boolean DEBUG=false;

    protected final ParsingTable table;

    private Queue<ParserConfiguration> parseQueue;
    protected ParserCallback parserCB;

    public Parser(ParsingTable t)
    {
        table=t;
        parseQueue=null;
    }

    protected void dprintln(String s)
    {
        if (DEBUG)
            System.out.println(this.getClass().getSimpleName()+": "+s);
    }

    protected Collection<ParserConfiguration> shift(ParserConfiguration parent,
                                                    ParsingActionShift shift,
                                                    List<Terminal> input)
        throws ParserAbortedException
    {
        ParserConfiguration parserConf;

        parserConf=parent == null ? new ParserConfiguration()
                                  : parent.clone();

        parserConf.getStack().push(new ParsingStackNode(shift.getState(),1));
        parserConf.pos++;

        parserConf.addAction(shift);

        dprintln(parserConf.hashCode()+": shift "+shift.getState());

        return Collections.singleton(parserConf);
    }

    /* Pops n elements from the stack and returns the number of terminals
     * generated by the poped elements.
     */
    protected int stackPop(ParserConfiguration parserConf, int n)
    {
        int genTerminals=0;

        for (int i=0; i < n; i++)
        {
            genTerminals+=parserConf.getTerminalNum();
            parserConf.getStack().pop();
        }

        return genTerminals;
    }

    protected Collection<ParserConfiguration> reduce(ParserConfiguration parent,
                                                     ParsingActionReduce reduction,
                                                     List<Terminal> input)
        throws ParserAbortedException
    {
        ParserConfiguration parserConf=parent.clone();
        Production p=reduction.getProduction();
        int s;
        int genTerminals;

        genTerminals=stackPop(parserConf,p.bodyLength());

        s=parserConf.getState();

        parserConf.getStack().push(new ParsingStackNode(table.goTo(s,p.getHead()),
                                                        genTerminals));

        parserConf.addAction(reduction);

        dprintln(parserConf.hashCode()+": reduce "+p);

        return Collections.singleton(parserConf);
    }

    private void acceptedDeliver(ParserConfiguration parserConf)
    {
        NonTerminal lca=parserConf.lca;

        assert parserConf.lca != null;

        gluon.profiling.Timer.stop("parsing");

        parserCB.accepted(parserConf.getActionList(),lca);

        gluon.profiling.Timer.start("parsing");
    }

    protected Collection<ParserConfiguration> accept(ParserConfiguration parent,
                                                     ParsingActionAccept accept,
                                                     List<Terminal> input)
        throws ParserAbortedException
    {
        ParserConfiguration parserConf=parent.clone();

        dprintln(parserConf.hashCode()+": accept");

        parserConf.addAction(accept);

        acceptedDeliver(parserConf);

        gluon.profiling.Profiling.inc("parse-branches");

        return new ArrayList<ParserConfiguration>(0);
    }

    private void parseSingleStep(ParserConfiguration parserConf,
                                 List<Terminal> input)
        throws ParserAbortedException
    {
        Collection<ParsingAction> actions;

        actions=parserConf.getSuccessorActions(table,input);

        if (actions.size() == 0)
        {
            dprintln(parserConf.hashCode()+": error: actions=∅");

            gluon.profiling.Profiling.inc("parse-branches");

            return;
        }

        for (ParsingAction action: actions)
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

                /* Check if we have a lca */
                if (branch.getTerminalNum() == inputTerminals
                    && parserConf.lca == null
                    && action instanceof ParsingActionReduce)
                {
                    ParsingActionReduce red=(ParsingActionReduce)action;
                    boolean cont;

                    branch.lca=red.getProduction().getHead();

                    gluon.profiling.Timer.stop("parsing");
                    cont=parserCB.onLCA(branch.getActionList(),branch.lca);
                    gluon.profiling.Timer.start("parsing");

                    if (!cont)
                    {
                        gluon.profiling.Profiling.inc("parse-branches");
                        continue;
                    }
                }

                parseQueue.add(branch);
            }
        }
    }

    protected abstract Collection<ParserConfiguration>
        getInitialConfigurations(List<Terminal> input)
        throws ParserAbortedException;

    /* Argument input should be an ArrayList for performance reasons. */
    public void parse(List<Terminal> input, ParserCallback pcb)
        throws ParserAbortedException
    {
        int counter=0; /* For calling pcb.shouldStop() */

        gluon.profiling.Timer.start("parsing");

        parseQueue=new LinkedList<ParserConfiguration>();
        parserCB=pcb;

        for (ParserConfiguration initialConfig: getInitialConfigurations(input))
            parseQueue.add(initialConfig);

        while (parseQueue.size() > 0)
        {
            ParserConfiguration parserConf=parseQueue.remove();

            counter=(counter+1)%500000;

            if (counter == 0 && parserCB.shouldAbort())
            {
                gluon.profiling.Timer.stop("parsing");
                throw new ParserAbortedException();
            }

            parseSingleStep(parserConf,input);
        }

        /* free memory */
        parseQueue=null;
        parserCB=null;

        gluon.profiling.Timer.stop("parsing");
    }
}
