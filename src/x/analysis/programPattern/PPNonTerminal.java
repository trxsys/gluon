package x.analysis.programPattern;

public class PPNonTerminal
    extends x.cfg.NonTerminal
{
    private String s;
    
    public PPNonTerminal(String s)
    {
        this.s=s;
    }
    
    @Override
    public int hashCode()
    {
        return s.hashCode();
    }

    @Override
    public boolean equals(Object o)
    {
        PPNonTerminal other;

        if (!(o instanceof PPNonTerminal))
            return false;

        other=(PPNonTerminal)o;

        return other.s.equals(s);
    }
    
    @Override
    public String toString()
    {
        return s;
    }
}
