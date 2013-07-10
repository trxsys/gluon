package x.cfg.parsing.tomitaParser;

import java.util.List;

import x.cfg.parsing.parsingTable.parsingAction.ParsingAction;

public interface ParserCallback
{
    public int callback(List<ParsingAction> actions);
}
