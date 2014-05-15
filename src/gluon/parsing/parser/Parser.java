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

/* TODO: restructure stack to make it simpler? */
class ParserStackNode {
    public int state;
    public int generateTerminals;

    public ParserStackNode(int s, int t)
    {
        state=s;
        generateTerminals=t;
    }
}

class ParserConfiguration
{
    /* We need this so we don't lose reduction history due to stack pops
     */
    private ParserConfiguration parentComplete;

    private ParserConfiguration parent;

    private ParserStackNode stackTop;

    private ParsingAction action;

    public NonTerminal lca;

    public int pos;

    public ParserConfiguration(ParserConfiguration parentConfig)
    {
        parentComplete=parentConfig;
        parent=parentConfig;
        stackTop=null;
        pos=parentConfig != null ? parentConfig.pos : 0;
        action=null;
        lca=parentConfig != null ? parentConfig.lca : null;
    }

    public ParserConfiguration()
    {
        this(null);
    }

    public void setAction(ParsingAction a)
    {
        action=a;
    }

    public List<ParsingAction> getActionList()
    {
        LinkedList<ParsingAction> alist
            =new LinkedList<ParsingAction>();

        for (ParserConfiguration pc=this; pc != null; pc=pc.parentComplete)
            alist.addFirst(pc.action);

        return alist;
    }

    /* TODO: optimize this so that we don't need to traverse the stack */
    public int stackSize()
    {
        int size=0;

        for (ParserConfiguration pc=this; pc != null; pc=pc.parent)
            size++;

        return size;
    }

    public ParserStackNode stackPeek()
    {
        return stackTop != null ? stackTop : parent.stackPeek();
    }

    public void stackPush(ParserStackNode t)
    {
        assert stackTop == null;

        stackTop=t;
    }

    public void stackPop()
    {
        if (stackTop != null)
            stackTop=null;
        else
        {
            assert parent != null;
            assert parent.stackTop != null;
            parent=parent.parent;
        }
    }

    public boolean isLoop(ParserConfiguration conf)
    {
        NonTerminal redHead;
        int loops=0;
        int genTerminals=conf.stackPeek().generateTerminals;

        if (!(conf.action instanceof ParsingActionReduce))
            return false;

        redHead=((ParsingActionReduce)conf.action).getProduction().getHead();

        for (ParserConfiguration pc=this; pc != null; pc=pc.parentComplete)
        {
            ParsingActionReduce ancRed;
            NonTerminal ancRedHead;
            int ancGenTerminals;

            if (!(pc.action instanceof ParsingActionReduce))
                return false;

            ancRed=(ParsingActionReduce)pc.action;
            ancRedHead=ancRed.getProduction().getHead();
            ancGenTerminals=pc.stackPeek().generateTerminals;

            if (ancGenTerminals < genTerminals)
                return false;

            if (redHead.equals(ancRedHead))
                return true;
        }

        return false;
    }

    public int getTerminalNum()
    {
        return stackPeek().generateTerminals;
    }

    public ParserConfiguration clone()
    {
        ParserConfiguration clone=new ParserConfiguration();

        clone.parentComplete=parentComplete;
        clone.parent=parent;
        clone.stackTop=stackTop;
        clone.action=action;
        clone.lca=lca;
        clone.pos=pos;

        return clone;
    }
}

/* This is a partially a implementation of the tomita parser. We do not merge
 * configuration states as described in the full tomita implementation.
 *
 * This parser also detects and prune branches with unproductive loops in the
 * grammar.
 * A more aggressive pruning is done so that no two parsing trees with the same
 * lowest common ancestor are explored (PRUNE_BY_REPEATED_LCA).
 * Unfortunatly this cannot be done with contract with arguments since we may
 * prune LCA that would fail to match the arguments unification thus preventing
 * other LCA from being reported.
 */
public abstract class Parser
{
    private static final boolean DEBUG=false;

    private static final boolean PRUNE_BY_REPEATED_LCA=false;

    protected final ParsingTable table;

    private Stack<ParserConfiguration> parseLifo;
    private Set<NonTerminal> acceptedLCA;
    protected ParserCallback parserCB;

    public Parser(ParsingTable t)
    {
        table=t;
        parseLifo=null;
        acceptedLCA=null;
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

        parserConf.stackPush(new ParserStackNode(shift.getState(),1));
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
            genTerminals+=parserConf.stackPeek().generateTerminals;
            parserConf.stackPop();
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

        s=parserConf.stackPeek().state;

        parserConf.stackPush(new ParserStackNode(table.goTo(s,p.getHead()),
                                                 genTerminals));

        parserConf.setAction(reduction);

        dprintln(parserConf.hashCode()+": reduce "+p);

        return Collections.singleton(parserConf);
    }

    private void acceptedDeliver(ParserConfiguration parserConf)
    {
        NonTerminal lca=parserConf.lca;

        assert parserConf.lca != null;

        parserCB.accepted(parserConf.getActionList(),lca);

        if (PRUNE_BY_REPEATED_LCA)
            acceptedLCA.add(lca);
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

        gluon.profiling.Profiling.inc("final:parse-branches");

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

        s=parserConf.stackPeek().state;
        t=input.get(parserConf.pos);
        actions=table.actions(s,t);

        if (actions.size() == 0)
        {
            dprintln(parserConf.hashCode()+": error: actions("+s+","+t+")=∅");

            gluon.profiling.Profiling.inc("final:parse-branches");

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
                boolean prune=false;
                int inputTerminals;

                inputTerminals=input.size()
                    -(input.get(input.size()-1) instanceof EOITerminal ? 1 : 0);

                /* Check if we have a lca */
                if (branch.getTerminalNum() == inputTerminals
                    && parserConf.lca == null
                    && action instanceof ParsingActionReduce)
                {
                    ParsingActionReduce red=(ParsingActionReduce)action;

                    branch.lca=red.getProduction().getHead();

                    if (PRUNE_BY_REPEATED_LCA
                        && acceptedLCA.contains(branch.lca))
                        prune=true;
                }

                if (!prune && !parserConf.isLoop(branch))
                    parseLifo.add(branch);
                else
                    gluon.profiling.Profiling.inc("final:parse-branches");
            }
        }
    }

    protected abstract Collection<ParserConfiguration>
        getInitialConfigurations(List<Terminal> input);

    /* Argument input should be an ArrayList for performance reasons. */
    public void parse(List<Terminal> input, ParserCallback pcb)
        throws ParserAbortedException
    {
        int counter=0; /* For calling pcb.shouldStop() */

        parseLifo=new Stack<ParserConfiguration>();
        acceptedLCA=new HashSet<NonTerminal>();
        parserCB=pcb;

        for (ParserConfiguration initialConfig: getInitialConfigurations(input))
            parseLifo.add(initialConfig);

        while (parseLifo.size() > 0)
        {
            ParserConfiguration parserConf=parseLifo.pop();

            counter=(counter+1)%500000;

            if (counter == 0 && parserCB.shouldAbort())
                throw new ParserAbortedException();

            parseSingleStep(parserConf,input);
        }

        /* free memory */
        parseLifo=null;
        acceptedLCA=null;
        parserCB=null;
    }
}
