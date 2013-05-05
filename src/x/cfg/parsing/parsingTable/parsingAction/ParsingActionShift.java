package x.cfg.parsing.parsingTable.parsingAction;

public class ParsingActionShift
    extends ParsingAction
{
    private int state;

    public ParsingActionShift(int s)
    {
        state=s;
    }

    public int getState()
    {
        return state;
    }

    @Override
    public int hashCode()
    {
        return state;
    }

    @Override
    public boolean equals(Object o)
    {
        ParsingActionShift other;

        if (!(o instanceof ParsingActionShift))
            return false;

        other=(ParsingActionShift)o;

        return other.state == state;
    }

    @Override
    public String toString()
    {
        return "s"+state;
    }
}
