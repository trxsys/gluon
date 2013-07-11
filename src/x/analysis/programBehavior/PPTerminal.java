package x.analysis.programBehavior;

import soot.SootMethod;

// Represents a call to the module under analysis
public class PPTerminal
    extends x.cfg.Terminal
{
    private final SootMethod method; // module method

    public PPTerminal(SootMethod m)
    {
        super(m.getName());
        method=m;
    }

    public PPTerminal(String s)
    {
        super(s);
        method=null;
    }

    @Override
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

        return other.name.equals(super.name);
    }

    @Override
    public PPTerminal clone()
    {
        return method != null ? new PPTerminal(method) 
            : new PPTerminal(super.getName());
    }
}
