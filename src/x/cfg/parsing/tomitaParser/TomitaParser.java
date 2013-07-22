package x.cfg.parsing.tomitaParser;

import java.util.Collection;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import java.util.Stack;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.ArrayList;

import x.cfg.Production;
import x.cfg.Terminal;
import x.cfg.NonTerminal;
import x.cfg.EOITerminal;

import x.cfg.parsing.parsingTable.ParsingTable;
import x.cfg.parsing.parsingTable.parsingAction.*;

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

    ParserStackNode stackTop; 

    private ParsingAction action;

    public int pos;
    public ParserStatus status;

    public ParserConfiguration(ParserConfiguration p)
    {
        parentComplete=p;
        parent=p;
        stackTop=null;
        pos=parent != null ? p.pos : 0;
        action=null;

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
}

/* This is a partial implementation of the tomita parser. In particular we do not
 * merge configuration states as described in the full tomita implementation.
 */
public class TomitaParser
{
    private static final boolean DEBUG=false;

    private final ParsingTable table;
    private Stack<ParserConfiguration> parseLifo;

    public TomitaParser(ParsingTable t)
    {
        table=t;
        parseLifo=null;
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
                                   ArrayList<Terminal> input)
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
            return;
        }

        branches=initBranches(parserConf,actions.size());

        int i=0;
        for (ParsingAction action: actions)
        {
            if (action instanceof ParsingActionShift)
                shift(branches[i],(ParsingActionShift)action);
            else if (action instanceof ParsingActionReduce)
                reduce(branches[i],(ParsingActionReduce)action);
            else if (action instanceof ParsingActionAccept)
                accept(branches[i]);
            else
                assert false;

            if (!parserConf.isLoop(branches[i]))
                parseLifo.add(branches[i]);

            i++;
        }
    }
    
    private ParserConfiguration getInitialConfiguration()
    {
        ParserConfiguration initConfig=new ParserConfiguration();

        initConfig.stackPush(new ParserStackNode(table.getInitialState(),0));

        return initConfig;
    }
    
    // Input should be an ArrayList for performance reasons
    public int parse(ArrayList<Terminal> input, ParserCallback pcb)
    {
        int ret=0;
        ParserConfiguration initialConfig;

        assert input.size() > 0 
            && input.get(input.size()-1) instanceof EOITerminal
            : "input should end with $";

        parseLifo=new Stack<ParserConfiguration>();

        initialConfig=getInitialConfiguration();

        parseLifo.add(initialConfig);

        while (parseLifo.size() > 0)
        {
            ParserConfiguration parserConf=parseLifo.pop();
            
            switch (parserConf.status)
            {
            case RUNNING : parseSingleParser(parserConf,input); break;
            case ACCEPTED: 
                int z=pcb.callback(parserConf.getActionList());
                if (z != 0)
                    ret=z;
                break;
            case ERROR   : assert false : 
                "Why do we have error configs in the parser lifo?"; break;
            }
        }
        
        parseLifo=null;

        return ret;
    }
}
