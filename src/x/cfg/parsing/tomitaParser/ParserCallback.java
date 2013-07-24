package x.cfg.parsing.tomitaParser;

import java.util.List;

import x.cfg.parsing.parsingTable.parsingAction.ParsingAction;
import x.cfg.NonTerminal;

public interface ParserCallback
{
    public int callback(List<ParsingAction> actions, NonTerminal lca);
}
