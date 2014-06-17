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
import java.util.Set;
import java.util.Stack;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
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
    public int state;
    public int generatedTerminals;

    /* If generatedTerminalsByNonTerm.get(nonterm) = m then this subtree has a
     * nonterminal that consumed m terminals and no more.
     *
     * This is used to prune unproductive loops.
     */
    private Map<NonTerminal,Integer> terminalsByNonTerm;

    public ParsingStackNode parent;

    public ParsingStackNode()
    {
        state=-1;
        generatedTerminals=0;

        terminalsByNonTerm=new HashMap<NonTerminal,Integer>();

        parent=null;
    }

    public ParsingStackNode(int state, int generatedTerminals)
    {
        this();

        this.state=state;
        this.generatedTerminals=generatedTerminals;
    }

    public int getTerminalsByNonTerm(NonTerminal nonterm)
    {
        Integer terms=terminalsByNonTerm.get(nonterm);

        return terms == null ? 0 : terms;
    }

    public void updateTerminalsByNonTerm(NonTerminal nonterm, int terms)
    {
        int currentTerms=getTerminalsByNonTerm(nonterm);

        if (terms > currentTerms)
            terminalsByNonTerm.put(nonterm,terms);
    }

    public Set<Map.Entry<NonTerminal,Integer>>
        getNonTermTerminals()
    {
        return terminalsByNonTerm.entrySet();
    }

    public ParsingStackNode clone()
    {
        ParsingStackNode clone=new ParsingStackNode();

        clone.state=state;
        clone.generatedTerminals=generatedTerminals;

        /* We don't need to do a deep clone of this map because it will be
         * read-only from now on.
         */
        clone.terminalsByNonTerm=terminalsByNonTerm;

        clone.parent=parent;

        return clone;
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
    private ParsingStack stack;
    private LinkedList<ParsingAction> actions;
    public int pos;

    public NonTerminal lca;

    public ParserConfiguration()
    {
        actions=new LinkedList<ParsingAction>();

        stack=new ParsingStack();
        lca=null;
        pos=0;
    }

    /* This should always be called *after* the stack is changed as a result of
     * this action.
     */
    public void addAction(ParsingAction action)
    {
        actions.add(action);
    }

    public ParsingAction getLastAction()
    {
        return actions.getLast();
    }

    public List<ParsingAction> getActionList()
    {
        return actions;
    }

    public ParsingStack getStack()
    {
        return stack;
    }

    public boolean isLoop(ParserConfiguration conf)
    {
        ParsingAction action=conf.getLastAction();
        Production reduction;
        NonTerminal head;
        ParsingStackNode subTree;

        if (!(action instanceof ParsingActionReduce))
            return false;

        reduction=((ParsingActionReduce)action).getProduction();
        head=reduction.getHead();

        subTree=stack.peek();

        /* This iterates all the subtrees of the reduction made. */
        for (int i=0; i < reduction.bodyLength(); i++)
        {
            /* If this condition is true then there is a subtree that contains
             * /head/, and in that subtree head consumes the same number of
             * terminals as the branch /conf/.  This means that /conf/ contains
             * a loop (since there are two /head/ in the tree) and that loop is
             * inproductive (since from those two points of the tree no extra
             * terminal were consumed).
             */
            if (subTree.getTerminalsByNonTerm(head) == conf.getTerminalNum())
                return true;

            assert subTree.getTerminalsByNonTerm(head) < conf.getTerminalNum();

            subTree=subTree.parent;
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

    protected void copy(ParserConfiguration src)
    {
        stack=src.stack.clone();

        actions=new LinkedList<ParsingAction>();
        actions.addAll(src.actions);

        lca=src.lca;
        pos=src.pos;
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
        ParserConfiguration parserConf;

        parserConf=parent == null ? new ParserConfiguration()
                                  : parent.clone();

        parserConf.getStack().push(new ParsingStackNode(shift.getState(),1));
        parserConf.pos++;

        parserConf.addAction(shift);

        dprintln(parserConf.hashCode()+": shift "+shift.getState());

        return Collections.singleton(parserConf);
    }

    /* Pops n elements from the stack, returns the number of terminals
     * generated by the poped elements, and updates the "terminals by nonterm"
     * of the next stack node.
     */
    protected void stackPop(ParserConfiguration parserConf, int n,
                            ParsingStackNode newStackNode,
                            NonTerminal reductionHead)
    {
        for (int i=0; i < n; i++)
        {
            ParsingStackNode subtree;

            subtree=parserConf.getStack().peek();
            newStackNode.generatedTerminals+=subtree.generatedTerminals;

            parserConf.getStack().pop();

            for (Map.Entry<NonTerminal,Integer> e: subtree.getNonTermTerminals())
            {
                NonTerminal nonterm=e.getKey();
                int terms=e.getValue();

                newStackNode.updateTerminalsByNonTerm(nonterm,terms);
            }
        }

        newStackNode.updateTerminalsByNonTerm(reductionHead,
                                              newStackNode.generatedTerminals);
    }

    protected Collection<ParserConfiguration> reduce(ParserConfiguration parent,
                                                     ParsingActionReduce reduction,
                                                     List<Terminal> input)
        throws ParserAbortedException
    {
        ParserConfiguration parserConf=parent.clone();
        Production p=reduction.getProduction();
        ParsingStackNode newStackNode=new ParsingStackNode();
        int s;

        stackPop(parserConf,p.bodyLength(),newStackNode,p.getHead());

        s=parserConf.getState();

        newStackNode.state=table.goTo(s,p.getHead());

        parserConf.getStack().push(newStackNode);

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
            {
                gluon.profiling.Timer.stop("parsing");
                throw new ParserAbortedException();
            }

            parseSingleStep(parserConf,input);
        }

        /* free memory */
        parseLifo=null;
        parserCB=null;

        gluon.profiling.Timer.stop("parsing");
    }
}
