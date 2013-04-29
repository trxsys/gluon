package x.cfg;

import java.util.List;
import java.util.ArrayList;

public class Production
{
    private NonTerminal head;
    private List<LexicalElement> body;

    public Production(NonTerminal h, List<LexicalElement> b)
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

    public List<LexicalElement> getBody()
    {
        return body;
    }

    public int bodyLength()
    {
        return body.size();
    }

    public void rewrite(LexicalElement oldElement, LexicalElement newElement)
    {
        for (int i=0; i < body.size(); i++)
            if (body.get(i).equals(oldElement))
                body.set(i,newElement);
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

        if (!other.head.equals(head))
            return false;

        return other.body.equals(body);
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
}
