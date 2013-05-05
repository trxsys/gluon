package x.cfg.parsing.tomitaParser;

import java.util.Collection;
import java.util.List;
import java.util.Stack;

import java.util.ArrayList;

import x.cfg.Terminal;
import x.cfg.EOITerminal;

import x.cfg.parsing.parsingTable.ParsingTable;
import x.cfg.parsing.parsingTable.parsingAction.*;
import x.cfg.parsing.tomitaParser.stackElement.*;

public class TomitaParser
{
    private static final boolean DEBUG=true;

    private final ParsingTable table;

    public TomitaParser(ParsingTable t)
    {
        table=t;
    }

    // Input should be an ArrayList for performance reasons
    public void parse(ArrayList<Terminal> input)
    {
        Stack<StackElement> stack=new Stack<StackElement>();
        int pos=0;

        assert input.size() > 0 
            && input.get(input.size()-1) instanceof EOITerminal
            : "input should end with $";

        stack.push(new StackState(table.getInitialState()));
        
        while (true)
        {
            int s=((StackState)stack.peek()).getState();
            Terminal t=input.get(pos);
            Collection<ParsingAction> actions;
            
            actions=table.actions(s,t);

            if (actions == null 
                 || actions.size() == 0)
                break; // XXX ERROR

            // TODO proof of concept: only do first action
            {
                ParsingAction a=actions.iterator().next();

                if (a instanceof ParsingActionShift)
                    ;
                else if (a instanceof ParsingActionReduce)
                    ;
                else if (a instanceof ParsingActionAccept)
                    ;
                else
                    assert false;
            }
        }
    }
}
