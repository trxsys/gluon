package x.cfg.parsing.tomitaParser;

import java.util.Collection;
import java.util.List;
import java.util.Stack;
import java.util.Queue;

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
    public Stack<Integer> stack;
    public int pos;
    public List<ParsingActionReduce> reductions;
    
    public ParserStatus status;
    
    public ParserConfiguration()
    {
        stack=new Stack<Integer>();
        pos=0;
        reductions=new LinkedList<ParsingActionReduce>();
        
        status=ParserStatus.RUNNING;
    }
}

public class TomitaParser
{
    private static final int SINGLE_ROUND_STEPS=256;
    
    private static final boolean DEBUG=true;
    
    private final ParsingTable table;
    private Queue<ParserConfiguration> parseFifo;
    
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
        parserConf.stack.push(shift.getState());
        parserConf.pos++;
        
        dprintln(parserConf.hashCode()+": shift "+shift.getState());
    }
    
    private void reduce(ParserConfiguration parserConf,
                        ParsingActionReduce reduction)
    {
        Production p=reduction.getProduction();
        int s;
        
        for (int i=0; i < p.bodyLength(); i++)
            parserConf.stack.pop();
        
        s=parserConf.stack.peek();
        
        parserConf.stack.push(table.goTo(s,p.getHead()));
        
        parserConf.reductions.add(reduction);
        
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

        branches[0]=parserConf;
        
        for (int i=1; i < n; i++)
        {
            branches[i]=new ParserConfiguration();

            // TODO: more efficient!
            branches[i].stack=(Stack<Integer>)parserConf.stack.clone();
            branches[i].pos=parserConf.pos;
            branches[i].reductions=new LinkedList<ParsingActionReduce>();
            branches[i].reductions.addAll(parserConf.reductions);
        }

        return branches;
    }

    private void parseSingleParser(ParserConfiguration parserConf, 
                                   ArrayList<Terminal> input)
    {
        for (int k=0; k < SINGLE_ROUND_STEPS
                      && parserConf.status != ParserStatus.ACCEPTED; k++)
        {
            int s;
            Terminal t;
            Collection<ParsingAction> actions;
            ParserConfiguration[] branches;

            assert parserConf.status == ParserStatus.RUNNING;            
            assert parserConf.stack.size() > 0;
            assert parserConf.pos < input.size();
            
            s=parserConf.stack.peek();
            t=input.get(parserConf.pos);
            actions=table.actions(s,t);
            
            if (actions == null 
                || actions.size() == 0)
            {
                dprintln(parserConf.hashCode()+": error: actions("
                         +s+","+t+")=∅");
                parserConf.status=ParserStatus.ERROR;
                break;
            }
            
            branches=initBranches(parserConf,actions.size());

            assert branches[0] == parserConf; // for performance reasons

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

                // Add branched parser
                if (i > 0){
                    dprintln("new branch: "+branches[i].hashCode());
                parseFifo.add(branches[i]);
                }
                i++;
            } 
        }
    }
    
    private ParserConfiguration getInitialConfiguration()
    {
        ParserConfiguration initConfig=new ParserConfiguration();
        
        initConfig.stack.push(table.getInitialState());
        
        return initConfig;
    }
    
    // Input should be an ArrayList for performance reasons
    public Collection<List<ParsingActionReduce>> parse(ArrayList<Terminal> input)
    {
        Collection<List<ParsingActionReduce>> accepted
            =new LinkedList<List<ParsingActionReduce>>();
        
        assert input.size() > 0 
            && input.get(input.size()-1) instanceof EOITerminal
            : "input should end with $";
        
        parseFifo=new LinkedList<ParserConfiguration>();
        
        parseFifo.add(getInitialConfiguration());
        
        while (parseFifo.size() > 0)
        {
            ParserConfiguration parserConf=parseFifo.poll();
            
            parseSingleParser(parserConf,input);
            
            switch (parserConf.status)
            {
            case RUNNING : parseFifo.add(parserConf); break;
            case ACCEPTED: accepted.add(parserConf.reductions); break;
            case ERROR   : break;
            }
        }
        
        parseFifo=null;
        
        return accepted;
    }
}
