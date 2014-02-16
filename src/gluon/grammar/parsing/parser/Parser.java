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

package gluon.grammar.parsing.parser;

import java.util.Collection;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import java.util.Stack;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.ArrayList;

import gluon.grammar.Production;
import gluon.grammar.Terminal;
import gluon.grammar.NonTerminal;
import gluon.grammar.EOITerminal;

import gluon.grammar.parsing.parsingTable.ParsingTable;
import gluon.grammar.parsing.parsingTable.parsingAction.*;

enum ParserStatus
{
    RUNNING,
    ACCEPTED,
    ERROR
}

class ParserStackNode {
    public final int state;
    public final int generateTerminals;

    public ParserStackNode(int s, int t)
    {
        state=s;
        generateTerminals=t;
    }
};

class ParserConfiguration
{
    /* We need this so we don't lose reduction history due to stack pops
     */
    private final ParserConfiguration parentComplete;

    private ParserConfiguration parent;

    private ParserStackNode stackTop; 

    private ParsingAction action;

    public NonTerminal lca;

    public int pos;
    public ParserStatus status;

    public ParserConfiguration(ParserConfiguration p)
    {
        parentComplete=p;
        parent=p;
        stackTop=null;
        pos=parent != null ? p.pos : 0;
        action=null;
        lca=p != null ? p.lca : null;

        status=ParserStatus.RUNNING;
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
public class Parser
{
    private static final boolean DEBUG=false;

    private static final boolean PRUNE_BY_REPEATED_LCA=false;

    private final ParsingTable table;
    private Stack<ParserConfiguration> parseLifo;

    private Set<NonTerminal> acceptedLCA;

    public Parser(ParsingTable t)
    {
        table=t;
        parseLifo=null;
        acceptedLCA=null;
    }
    
    private void dprintln(String s)
    {
        if (DEBUG)
            System.out.println(this.getClass().getSimpleName()+": "+s);
    }
    
    private void shift(ParserConfiguration parserConf,
                       ParsingActionShift shift)
    {
        parserConf.stackPush(new ParserStackNode(shift.getState(),1));
        parserConf.pos++;

        parserConf.setAction(shift);

        dprintln(parserConf.hashCode()+": shift "+shift.getState());
    }
    
    private void reduce(ParserConfiguration parserConf,
                        ParsingActionReduce reduction)
    {
        Production p=reduction.getProduction();
        int s;
        int genTerminals=0;

        for (int i=0; i < p.bodyLength(); i++)
        {
            genTerminals+=parserConf.stackPeek().generateTerminals;
            parserConf.stackPop();
        }
        
        s=parserConf.stackPeek().state;
        
        parserConf.stackPush(new ParserStackNode(table.goTo(s,p.getHead()),
                                                 genTerminals));
        
        parserConf.setAction(reduction);
        
        dprintln(parserConf.hashCode()+": reduce "+p);
    }
    
    private void accept(ParserConfiguration parserConf)
    {
        parserConf.status=ParserStatus.ACCEPTED;
        dprintln(parserConf.hashCode()+": accept");
    }
    
    private ParserConfiguration[] initBranches(ParserConfiguration parserConf,
                                               int n)
    {
        ParserConfiguration[] branches=new ParserConfiguration[n];

        for (int i=0; i < n; i++)
            branches[i]=new ParserConfiguration(parserConf);

        return branches;
    }

    private void parseSingleParser(ParserConfiguration parserConf, 
                                   List<Terminal> input)
    {
        int s;
        Terminal t;
        Collection<ParsingAction> actions;
        ParserConfiguration[] branches;

        assert parserConf.status == ParserStatus.RUNNING;
        assert parserConf.pos < input.size();

        s=parserConf.stackPeek().state;
        t=input.get(parserConf.pos);
        actions=table.actions(s,t);

        if (actions == null || actions.size() == 0)
        {
            dprintln(parserConf.hashCode()+": error: actions("+s+","+t+")=∅");
            parserConf.status=ParserStatus.ERROR;

            gluon.profiling.Profiling.inc("final:parse-branches");

            return;
        }

        branches=initBranches(parserConf,actions.size());

        int i=0;
        for (ParsingAction action: actions)
        {
            boolean prune=false;

            if (action instanceof ParsingActionShift)
                shift(branches[i],(ParsingActionShift)action);
            else if (action instanceof ParsingActionReduce)
                reduce(branches[i],(ParsingActionReduce)action);
            else if (action instanceof ParsingActionAccept)
                accept(branches[i]);
            else
                assert false;

            /* check if we have a lca */
            if (branches[i].getTerminalNum() == input.size()-1
                && parserConf.lca == null
                && action instanceof ParsingActionReduce)
            {
                ParsingActionReduce red=(ParsingActionReduce)action;

                branches[i].lca=red.getProduction().getHead();

                if (PRUNE_BY_REPEATED_LCA
                    && acceptedLCA.contains(branches[i].lca))
                    prune=true;
            }

            if (!prune && !parserConf.isLoop(branches[i]))
                parseLifo.add(branches[i]);
            else
                gluon.profiling.Profiling.inc("final:parse-branches");

            i++;
        }
    }
    
    private ParserConfiguration getInitialConfiguration()
    {
        ParserConfiguration initConfig=new ParserConfiguration();

        initConfig.stackPush(new ParserStackNode(table.getInitialState(),0));

        return initConfig;
    }
    
    // Argument input should be an ArrayList for performance reasons
    public int parse(List<Terminal> input, ParserCallback pcb)
    {
        int ret=0;
        ParserConfiguration initialConfig;

        assert input.size() > 0 
            && input.get(input.size()-1) instanceof EOITerminal
            : "input should end with $";

        parseLifo=new Stack<ParserConfiguration>();
        acceptedLCA=new HashSet<NonTerminal>();

        initialConfig=getInitialConfiguration();

        parseLifo.add(initialConfig);

        while (parseLifo.size() > 0)
        {
            ParserConfiguration parserConf=parseLifo.pop();
            
            switch (parserConf.status)
            {
            case RUNNING : parseSingleParser(parserConf,input); break;
            case ACCEPTED: 
                int z;
                NonTerminal lca=parserConf.lca;

                assert parserConf.lca != null;

                z=pcb.callback(parserConf.getActionList(),lca);
                acceptedLCA.add(lca);

                if (z != 0)
                    ret=z;

                gluon.profiling.Profiling.inc("final:parse-branches");

                break;
            case ERROR   : assert false : 
                "Why do we have error configs in the parser lifo?"; break;
            }
        }

        // free memory
        parseLifo=null;
        acceptedLCA=null;

        return ret;
    }
}
