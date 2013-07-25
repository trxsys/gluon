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

package gluon.cfg;

import java.util.ArrayList;

public class Production
{
    private NonTerminal head;
    private ArrayList<LexicalElement> body; // it is important to garantee O(1) get(n)

    public Production(NonTerminal h, ArrayList<LexicalElement> b)
    {
        this(h);
        body=b;
    }

    public Production(NonTerminal h)
    {
        head=h;
        body=new ArrayList<LexicalElement>(2);
    }

    public void appendToBody(LexicalElement e)
    {
        body.add(e);
    }

    public NonTerminal getHead()
    {
        return head;
    }

    public ArrayList<LexicalElement> getBody()
    {
        return body;
    }

    public int bodyLength()
    {
        return body.size();
    }

    public boolean hasTerminals()
    {
        for (int i=0; i < body.size(); i++)
            if (body.get(i) instanceof Terminal)
                return true;

        return false;
    }

    public void rewrite(LexicalElement oldElement, LexicalElement newElement)
    {
        for (int i=0; i < body.size(); i++)
            if (body.get(i).equals(oldElement))
                body.set(i,newElement);
    }

    public void erase(LexicalElement element)
    {
        for (int i=0; i < body.size(); i++)
            if (body.get(i).equals(element))
            {
                body.remove(i);
                i--;
            }
    }

    @Override
    public int hashCode()
    {
        int hash;

        hash=head.hashCode();

        for (LexicalElement e: body)
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

        for (LexicalElement e: body)
            s+=' '+e.toString();

        if (body.size() == 0)
            s+=" ε";

        return s;
    }

    public Production clone()
    {
        ArrayList<LexicalElement> newBody
            =new ArrayList<LexicalElement>(body.size());

        newBody.addAll(body);

        return new Production(head,newBody);
    }
}
