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

package gluon.grammar.parsing.parsingTable;

import gluon.grammar.Production;
import gluon.grammar.LexicalElement;

class Item
{
    private final Production prod;
    private final int pos;

    public Item(Production prod, int pos)
    {
        this.prod=prod;
        this.pos=pos;

        assert 0 <= pos && pos <= prod.bodyLength();
    }

    public Production getProduction()
    {
        return prod;
    }

    public int getDotPos()
    {
        return pos;
    }

    public LexicalElement getNextToDot()
    {
        if (isComplete())
            return null;

        return prod.getBody().get(pos);
    }

    public boolean isComplete()
    {
        return pos == prod.bodyLength();
    }

    @Override
    public int hashCode()
    {
        return prod.hashCode()^pos;
    }

    @Override
    public boolean equals(Object o)
    {
        Item other;

        if (!(o instanceof Item))
            return false;

        other=(Item)o;

        return other.pos == pos && other.prod.equals(prod);
    }

    @Override
    public String toString()
    {
        return prod.toString()+", "+pos;
    }
}
