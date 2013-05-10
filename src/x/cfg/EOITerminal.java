package x.cfg;

public final class EOITerminal
    extends Terminal
{
    public EOITerminal()
    {
        super("$");
    }

    public boolean isEOI()
    {
        return true;
    }

    @Override
    public int hashCode()
    {
        return 0xdecbfe1d;
    }

    @Override
    public boolean equals(Object o)
    {
        return o instanceof EOITerminal;
    }
}
