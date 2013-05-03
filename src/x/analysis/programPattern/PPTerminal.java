package x.analysis.programPattern;

import soot.SootMethod;

// Represents a call to the module under analysis
class PPTerminal
    extends x.cfg.Terminal
{
    private SootMethod method;
    
    public PPTerminal(SootMethod m)
    {
        assert m != null;
        method=m;
    }

    public boolean isEOI()
    {
        return false;
    }

    @Override
    public int hashCode()
    {
        return toString().hashCode();
    }

    @Override
    public boolean equals(Object o)
    {
        PPTerminal other;

        if (!(o instanceof PPTerminal))
            return false;

        other=(PPTerminal)o;

        return other.toString().equals(toString());
    }
    
    @Override
    public String toString()
    {
        return method.getName();
    }
}
