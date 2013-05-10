package x.cfg;

public abstract class NonTerminal
    extends LexicalElement
{
    public NonTerminal(String name)
    {
        super(name);
    }

    public void setName(String newName)
    {
        name=newName;
    }

    @Override
    public abstract NonTerminal clone();
}
