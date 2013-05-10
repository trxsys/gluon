package x.cfg;

public abstract class NonTerminal
    extends LexicalElement
{
    public NonTerminal(String name)
    {
        super(name);
    }

    @Override
    public abstract NonTerminal clone();
}
