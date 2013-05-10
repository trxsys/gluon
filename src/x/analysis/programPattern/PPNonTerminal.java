package x.analysis.programPattern;

public class PPNonTerminal
    extends x.cfg.NonTerminal
{
    public PPNonTerminal(String s)
    {
        super(s);
    }
    
    @Override
    public int hashCode()
    {
        return super.name.hashCode();
    }

    @Override
    public boolean equals(Object o)
    {
        PPNonTerminal other;

        if (!(o instanceof PPNonTerminal))
            return false;

        other=(PPNonTerminal)o;

        return other.name.equals(super.name);
    }
}
