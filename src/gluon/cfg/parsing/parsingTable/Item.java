package gluon.cfg.parsing.parsingTable;

import gluon.cfg.Production;
import gluon.cfg.LexicalElement;

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
