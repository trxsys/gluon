package x.re;

public class RESymbol<A>
    implements RegExp<A>
{
    private A symbol;

    public RESymbol(A symbol)
    {
        this.symbol=symbol;
    }

    public A getSymbol()
    {
        return symbol;
    }
}
