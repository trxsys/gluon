package x.analysis.programPattern.parsingAction;

public class ParsingActionShift
    implements ParsingAction
{
    private int state;

    public ParsingActionShift(int s)
    {
        state=s;
    }

    public int state()
    {
        return state;
    }

    @Override
    public String toString()
    {
        return "s"+state;
    }
}
