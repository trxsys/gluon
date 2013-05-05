package x.cfg.parsing.parsingTable.parsingAction;

public class ParsingActionShift
    implements ParsingAction
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
    public String toString()
    {
        return "s"+state;
    }
}
