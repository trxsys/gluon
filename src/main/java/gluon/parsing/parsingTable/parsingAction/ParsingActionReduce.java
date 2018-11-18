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

package gluon.parsing.parsingTable.parsingAction;

import gluon.grammar.Production;

public class ParsingActionReduce
    extends ParsingAction
{
    private Production production;

    public ParsingActionReduce(Production p)
    {
        production=p;
    }

    public Production getProduction()
    {
        return production;
    }

    @Override
    public int hashCode()
    {
        return production.hashCode();
    }

    @Override
    public boolean equals(Object o)
    {
        ParsingActionReduce other;

        if (!(o instanceof ParsingActionReduce))
            return false;

        other=(ParsingActionReduce)o;

        return other.production.equals(production);
    }

    @Override
    public String toString()
    {
        return "r["+production.toString()+"]";
    }
}
