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

import gluon.grammar.Production;
import gluon.grammar.Terminal;
import gluon.grammar.NonTerminal;
import gluon.grammar.EOITerminal;

import gluon.parsing.parsingTable.ParsingTable;
import gluon.parsing.parsingTable.parsingAction.*;

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
public class ParserNormal
    extends Parser
{
    public ParserNormal(ParsingTable t)
    {
        super(t);
    }

    @Override
    protected void shift(ParserConfiguration parserConf,
                         ParsingActionShift shift)
    {
        parserConf.stackPush(new ParserStackNode(shift.getState(),1));
        parserConf.pos++;

        parserConf.setAction(shift);

        dprintln(parserConf.hashCode()+": shift "+shift.getState());
    }

    @Override    
    protected void reduce(ParserConfiguration parserConf,
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

    @Override    
    protected void accept(ParserConfiguration parserConf)
    {
        parserConf.status=ParserStatus.ACCEPTED;
        dprintln(parserConf.hashCode()+": accept");
    }

    @Override
    protected Collection<ParserConfiguration> getInitialConfigurations()
    {
        ParserConfiguration initConfig=new ParserConfiguration();
        Collection<ParserConfiguration> configurations;

        initConfig.stackPush(new ParserStackNode(table.getInitialState(),0));

        configurations=new ArrayList<ParserConfiguration>(0);
        configurations.add(initConfig);

        return configurations;
    }

    @Override
    public int parse(List<Terminal> input, ParserCallback pcb)
        throws ParserAbortedException
    {
       assert input.size() > 0 
            && input.get(input.size()-1) instanceof EOITerminal
            : "input should end with $";

       return super.parse(input,pcb);
    }
}
