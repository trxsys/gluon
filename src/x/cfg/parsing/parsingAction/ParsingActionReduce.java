package x.cfg.parsing.parsingAction;

import x.cfg.Production;

public class ParsingActionReduce
    implements ParsingAction
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
    public String toString()
    {
        return "r["+production.toString()+"]";
    }
}
