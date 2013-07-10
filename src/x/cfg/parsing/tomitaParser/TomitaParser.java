package x.cfg.parsing.tomitaParser;

import java.util.Collection;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.ArrayList;

import x.cfg.Production;
import x.cfg.Terminal;
import x.cfg.EOITerminal;

import x.cfg.parsing.parsingTable.ParsingTable;
import x.cfg.parsing.parsingTable.parsingAction.*;

enum ParserStatus
{
    RUNNING,
    ACCEPTED,
    ERROR
}

class ParserConfiguration
{
    /* We need this so we don't lose reduction history due to stack pops
     */
    private final ParserConfiguration parentComplete;

    private ParserConfiguration parent;

    private int stackTop;

    private ParsingAction action;

    public int pos;
    public ParserStatus status;

    public ParserConfiguration(ParserConfiguration p)
    {
        parentComplete=p;
        parent=p;
        stackTop=-1;
        pos=parent != null ? p.pos : 0;
        action=null;

        status=ParserStatus.RUNNING;
    }

    public ParserConfiguration()
    {
        this(null);
    }

    public void setReduction(ParsingActionReduce r)
    {
        action=r;
    }

    public void setShift(ParsingActionShift s)
    {
        action=s;
    }

    public void setAccept(ParsingActionAccept a)
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

    public int stackPeek()
    {
        return stackTop >= 0 ? stackTop : parent.stackPeek();
    }

    public void stackPush(int t)
    {
        assert stackTop < 0;

        stackTop=t;
    }

    public void stackPop()
    {
        if (stackTop >= 0)
            stackTop=-1;
        else
        {
            assert parent != null;
            assert parent.stackTop >= 0;
            parent=parent.parent;
        }
    }

    @Override
    public int hashCode()
    {
        return ((pos<<12)*65599)^(stackPeek()*65599)^status.ordinal();
    }

    @Override
    public boolean equals(Object o)
    {
        ParserConfiguration other;

        if (!(o instanceof ParserConfiguration))
            return false;

        other=(ParserConfiguration)o;

        return other.pos == pos && other.stackPeek() == stackPeek()
            && other.status == status;
    }
}

public class TomitaParser
{
    private static final boolean DEBUG=false;

    private final ParsingTable table;
    private Queue<ParserConfiguration> parseFifo;
    private Set<ParserConfiguration> parseConfigs;

    public TomitaParser(ParsingTable t)
    {
        table=t;
        parseFifo=null;
    }
    
    private void dprintln(String s)
    {
        if (DEBUG)
            System.out.println(this.getClass().getSimpleName()+": "+s);
    }
    
    private void shift(ParserConfiguration parserConf,
                       ParsingActionShift shift)
    {
        parserConf.stackPush(shift.getState());
        parserConf.pos++;

        parserConf.setShift(shift);

        dprintln(parserConf.hashCode()+": shift "+shift.getState());
    }
    
    private void reduce(ParserConfiguration parserConf,
                        ParsingActionReduce reduction)
    {
        Production p=reduction.getProduction();
        int s;
        
        for (int i=0; i < p.bodyLength(); i++)
            parserConf.stackPop();
        
        s=parserConf.stackPeek();
        
        parserConf.stackPush(table.goTo(s,p.getHead()));
        
        parserConf.setReduction(reduction);
        
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

        s=parserConf.stackPeek();
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

            // if (!parseConfigs.contains(branches[i]))
            {
                parseFifo.add(branches[i]);
                parseConfigs.add(branches[i]);
            }

            i++;
        }
    }
    
    private ParserConfiguration getInitialConfiguration()
    {
        ParserConfiguration initConfig=new ParserConfiguration();

        initConfig.stackPush(table.getInitialState());

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

        parseFifo=new LinkedList<ParserConfiguration>();
        parseConfigs=new HashSet<ParserConfiguration>();

        initialConfig=getInitialConfiguration();

        parseFifo.add(initialConfig);
        parseConfigs.add(initialConfig);

        while (parseFifo.size() > 0)
        {
            ParserConfiguration parserConf=parseFifo.poll();
            
            switch (parserConf.status)
            {
            case RUNNING : parseSingleParser(parserConf,input); break;
            case ACCEPTED: 
                int z=pcb.callback(parserConf.getActionList());
                if (z != 0)
                    ret=z;
                break;
            case ERROR   : assert false : 
                "Why do we have error configs in the parser fifo?"; break;
            }
        }
        
        parseFifo=null;
        parseConfigs=null;

        return ret;
    }
}
