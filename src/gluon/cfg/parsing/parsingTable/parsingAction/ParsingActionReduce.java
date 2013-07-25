package gluon.cfg.parsing.parsingTable.parsingAction;

import gluon.cfg.Production;

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
