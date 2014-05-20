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
import java.util.Stack;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.ArrayList;

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

class ParserConfiguration
{
    /* We need this so we don't lose reduction history due to stack pops
     */
    private ParserConfiguration parent;

    private ParsingStack stack;

    private ParsingAction action;

    public NonTerminal lca;

    public int pos;

    public ParserConfiguration(ParserConfiguration parentConfig)
    {
        parent=parentConfig;
        action=null;

        if (parentConfig != null)
        {
            stack=parentConfig.stack.clone();
            lca=parentConfig.lca;
            pos=parentConfig.pos;
        }
        else
        {
            stack=new ParsingStack();
            lca=null;
            pos=0;
        }
    }

    public ParserConfiguration()
    {
        this(null);
    }

    /* Indicates if this is the first configuration to reach the LCA in this
     * parsing branch.
     */
    public boolean firstToReachedLCA()
    {
        return lca != null && parent.lca == null;
    }

    public void setAction(ParsingAction a)
    {
        action=a;
    }

    public List<ParsingAction> getActionList()
    {
        LinkedList<ParsingAction> alist
            =new LinkedList<ParsingAction>();

        for (ParserConfiguration pc=this; pc != null; pc=pc.parent)
            alist.addFirst(pc.action);

        return alist;
    }

    public ParsingStack getStack()
    {
        return stack;
    }

    public boolean isLoop(ParserConfiguration conf)
    {
        NonTerminal redHead;
        int genTerminals;

        if (!(conf.action instanceof ParsingActionReduce))
            return false;

        redHead=((ParsingActionReduce)conf.action).getProduction().getHead();
        genTerminals=conf.getTerminalNum();

        for (ParserConfiguration pc=this; pc != null; pc=pc.parent)
        {
            ParsingActionReduce ancRed;
            NonTerminal ancRedHead;
            int ancGenTerminals;

            /* This means that we encontered a shift which is productive,
             * therefore we can stop now.
             */
            if (!(pc.action instanceof ParsingActionReduce))
            {
                assert pc.action instanceof ParsingActionShift;
                return false;
            }

            ancGenTerminals=pc.getTerminalNum();

            if (ancGenTerminals < genTerminals)
                return false;

            ancRed=(ParsingActionReduce)pc.action;
            ancRedHead=ancRed.getProduction().getHead();

            if (redHead.equals(ancRedHead))
                return true;
        }

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

    public ParserConfiguration clone()
    {
        ParserConfiguration clone=new ParserConfiguration();

        clone.parent=parent;
        clone.action=action;
        clone.stack=stack.clone();
        clone.lca=lca;
        clone.pos=pos;

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

    private Stack<ParserConfiguration> parseLifo;
    protected ParserCallback parserCB;

    public Parser(ParsingTable t)
    {
        table=t;
        parseLifo=null;
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
        ParserConfiguration parserConf=newParserConfiguration(parent);

        parserConf.getStack().push(new ParsingStackNode(shift.getState(),1));
        parserConf.pos++;

        parserConf.setAction(shift);

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
        ParserConfiguration parserConf=newParserConfiguration(parent);
        Production p=reduction.getProduction();
        int s;
        int genTerminals;

        genTerminals=stackPop(parserConf,p.bodyLength());

        s=parserConf.getState();

        parserConf.getStack().push(new ParsingStackNode(table.goTo(s,p.getHead()),
                                                        genTerminals));

        parserConf.setAction(reduction);

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
        ParserConfiguration parserConf=newParserConfiguration(parent);

        dprintln(parserConf.hashCode()+": accept");

        parserConf.setAction(accept);

        acceptedDeliver(parserConf);

        gluon.profiling.Profiling.inc("parse-branches");

        return new ArrayList<ParserConfiguration>(0);
    }

    protected ParserConfiguration newParserConfiguration(ParserConfiguration parent)
    {
        return new ParserConfiguration(parent);
    }

    private void parseSingleStep(ParserConfiguration parserConf,
                                 List<Terminal> input)
        throws ParserAbortedException
    {
        int s;
        Terminal t;
        Collection<ParsingAction> actions;

        assert parserConf.pos < input.size();

        s=parserConf.getState();
        t=input.get(parserConf.pos);
        actions=table.actions(s,t);

        if (actions.size() == 0)
        {
            dprintln(parserConf.hashCode()+": error: actions("+s+","+t+")=∅");

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

                    branch.lca=red.getProduction().getHead();
                }

                parseLifo.add(branch);
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

        parseLifo=new Stack<ParserConfiguration>();
        parserCB=pcb;

        for (ParserConfiguration initialConfig: getInitialConfigurations(input))
            parseLifo.add(initialConfig);

        while (parseLifo.size() > 0)
        {
            ParserConfiguration parserConf=parseLifo.pop();

            counter=(counter+1)%500000;

            if (counter == 0 && parserCB.shouldAbort())
                throw new ParserAbortedException();

            /* If this is the *first* configuration of this branch to have an
             * LCA this is the best place to try to prune it.
             *
             * This *should not* be done before adding the configuration on the
             * lifo.
             */
            if (parserConf.firstToReachedLCA()
                && parserCB.pruneOnLCA(parserConf.getActionList(),
                                       parserConf.lca))
            {
                gluon.profiling.Profiling.inc("parse-branches");
                continue;
            }

            parseSingleStep(parserConf,input);
        }

        /* free memory */
        parseLifo=null;
        parserCB=null;

        gluon.profiling.Timer.stop("parsing");
    }
}
