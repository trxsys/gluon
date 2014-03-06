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

package gluon.parsing.parseTree;

import gluon.grammar.LexicalElement;

public class ParseTreeNode
{
    private LexicalElement elem;
    private ParseTreeNode parent;
    protected int count; /* For getLCA() */

    public ParseTreeNode(LexicalElement e)
    {
        elem=e;
        parent=null;
        count=0;
    }

    public void setParent(ParseTreeNode p)
    {
        parent=p;
    }

    public ParseTreeNode getParent()
    {
        return parent;
    }

    public void setElem(LexicalElement e)
    {
        elem=e;
    }

    public LexicalElement getElem()
    {
        return elem;
    }

    @Override
    public String toString()
    {
        return elem.toString()+"_"+hashCode();
    }
}
