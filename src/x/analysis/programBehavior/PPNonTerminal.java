package x.analysis.programBehavior;

import soot.SootMethod;

public class PPNonTerminal
    extends x.cfg.NonTerminal
{
    private SootMethod method;

    public PPNonTerminal(String s, SootMethod m)
    {
        super(s);
        method=m;
    }

    public SootMethod getMethod()
    {
        return method;
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

    @Override
    public PPNonTerminal clone()
    {
        return new PPNonTerminal(super.name,method);
    }
}
