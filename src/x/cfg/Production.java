package x.cfg;

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
