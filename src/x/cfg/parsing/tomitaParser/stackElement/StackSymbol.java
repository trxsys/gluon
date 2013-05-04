package x.cfg.parsing.tomitaParser.stackElement;

import x.cfg.LexicalElement;

public class StackSymbol
    extends StackElement
{
    private final LexicalElement symbol;

    public StackSymbol(LexicalElement s)
    {
        symbol=s;
    }
}
