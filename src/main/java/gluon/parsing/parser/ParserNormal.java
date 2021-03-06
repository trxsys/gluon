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

import gluon.grammar.EOITerminal;
import gluon.grammar.Terminal;
import gluon.parsing.parsingTable.ParsingTable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ParserNormal
    extends Parser
{
    public ParserNormal(ParsingTable t)
    {
        super(t);
    }

    @Override
    protected Collection<ParserConfiguration>
        getInitialConfigurations(List<Terminal> input)
    {
        ParserConfiguration initConfig=new ParserConfiguration();
        Collection<ParserConfiguration> configurations;

        initConfig.getStack().push(new ParsingStackNode(table.getInitialState(),0));

        configurations=new ArrayList<ParserConfiguration>(1);
        configurations.add(initConfig);

        return configurations;
    }

    @Override
    public void parse(List<Terminal> input, ParserCallback pcb)
        throws ParserAbortedException
    {
        assert input.size() == 0
            || input.get(input.size()-1) instanceof EOITerminal
            : "input should end with $";

        super.parse(input,pcb);
    }
}
