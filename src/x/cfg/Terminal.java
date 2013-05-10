package x.cfg;

public abstract class Terminal
    extends LexicalElement
{
    public Terminal(String name)
    {
        super(name);
    }

    public abstract boolean isEOI();
}
