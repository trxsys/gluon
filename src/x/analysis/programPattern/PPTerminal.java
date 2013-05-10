package x.analysis.programPattern;

import soot.SootMethod;

// Represents a call to the module under analysis
public class PPTerminal
    extends x.cfg.Terminal
{
    private final SootMethod method; // module method

    private final int atomicRegion; // < 0 if it is a non atomic access

    public PPTerminal(SootMethod m, int a)
    {
        super(m.getName());
        method=m;        
        atomicRegion=a;
    }

    public PPTerminal(String s)
    {
        super(s);
        method=null;
        atomicRegion=-1;
    }

    public boolean isAtomicRegion()
    {
        return atomicRegion >= 0;
    }

    public int getAtomicRegion()
    {
        return atomicRegion;
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
}
