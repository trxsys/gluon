package x.cfg;

public abstract class LexicalElement
{
    protected String name;

    public LexicalElement(String name)
    {
        assert name != null;
        this.name=name;
    }

    public void setName(String newName)
    {
        name=newName;
    }

    public String getName()
    {
        return name;
    }

    @Override
    public String toString()
    {
        return getName();
    }

    public abstract int hashCode();
    public abstract boolean equals(Object o);

    public abstract LexicalElement clone();
}
