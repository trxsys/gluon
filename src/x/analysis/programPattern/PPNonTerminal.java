package x.analysis.programPattern;

class PPNonTerminal
    extends x.cfg.NonTerminal
{
    private String s;
    private boolean atomic;
    
    public PPNonTerminal(String s)
    {
        this(s,false);
    }
    
    public PPNonTerminal(String s, boolean atomic)
    {
        this.s=s;
        this.atomic=atomic;
    }

    @Override
    public int hashCode()
    {
        return s.hashCode()^(atomic ? 1 : 0);
    }

    @Override
    public boolean equals(Object o)
    {
        PPNonTerminal other;

        if (!(o instanceof PPNonTerminal))
            return false;

        other=(PPNonTerminal)o;

        if (!other.s.equals(s))
            return false;

        return other.atomic == atomic;
    }
    
    @Override
    public String toString()
    {
        return (atomic ? "@" : "")+s;
    }
    
    public boolean isAtomic()
    {
        return atomic;
    }
}
