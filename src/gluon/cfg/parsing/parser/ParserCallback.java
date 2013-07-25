package gluon.cfg.parsing.parser;

import java.util.List;

import gluon.cfg.parsing.parsingTable.parsingAction.ParsingAction;
import gluon.cfg.NonTerminal;

public interface ParserCallback
{
    public int callback(List<ParsingAction> actions, NonTerminal lca);
}
