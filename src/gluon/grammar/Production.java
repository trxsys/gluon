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

package gluon.grammar;

import java.util.ArrayList;

public class Production
{
    private NonTerminal head;
    private ArrayList<Symbol> body; // it is important to garantee O(1) get(n)

    public Production(NonTerminal h, ArrayList<Symbol> b)
    {
        this(h);
        body=b;
    }

    public Production(NonTerminal h)
    {
        head=h;
        body=new ArrayList<Symbol>(2);
    }

    public void appendToBody(Symbol e)
    {
        body.add(e);
    }

    public NonTerminal getHead()
    {
        return head;
    }

    public ArrayList<Symbol> getBody()
    {
        return body;
    }

    public int bodyLength()
    {
        return body.size();
    }

    /* pre: string does not contain nonterm. */
    public void replace(NonTerminal nonterm, ArrayList<Symbol> string)
    {
        ArrayList<Symbol> oldBody=body;

        assert !string.contains(nonterm);

        /* Most likely there will only be one replace so this size should be
         * good.
         */
        body=new ArrayList<Symbol>(string.size()+oldBody.size()-1);

        for (Symbol e: oldBody)
            if (e.equals(nonterm))
                body.addAll(string);
            else
                body.add(e);
    }

    public boolean isDirectLoop()
    {
        return bodyLength() == 1 && getBody().get(0).equals(getHead());
    }

    @Override
    public int hashCode()
    {
        int hash;

        hash=head.hashCode();

        for (Symbol e: body)
            hash^=e.hashCode();

        return hash;
    }

    @Override
    public boolean equals(Object o)
    {
        Production other;

        if (!(o instanceof Production))
            return false;

        other=(Production)o;

        return other.head.equals(head) && other.body.equals(body);
    }

    @Override
    public String toString()
    {
        String s;

        s=head.toString()+" →";

        for (Symbol e: body)
            s+=' '+e.toString();

        if (body.size() == 0)
            s+=" ε";

        return s;
    }

    public Production clone()
    {
        ArrayList<Symbol> newBody=new ArrayList<Symbol>(body.size());

        newBody.addAll(body);

        return new Production(head,newBody);
    }
}
