package x.cfg.parsing.parseTree;

import java.util.ArrayList;
import java.util.List;

import x.cfg.Production;
import x.cfg.LexicalElement;
import x.cfg.NonTerminal;
import x.cfg.Terminal;

import x.cfg.parsing.parsingTable.parsingAction.*;

class ParseTreeNode
{
    private LexicalElement elem;
    private ParseTreeNode parent;

    public ParseTreeNode(LexicalElement e)
    {
        elem=e;
        parent=null;
    }
}

public class ParseTree
{
    public ParseTree()
    {
        
    }

    public void buildTree(List<Terminal> word,
                          List<ParsingAction> actions)
    {
        List<ParseTreeNode> list=new ArrayList<ParseTreeNode>(2*word.size());

        for (Terminal t: word)
            list.add(new ParseTreeNode(t));
        
        //        for (i)
    }

    public NonTerminal getLCANonTerm(List<LexicalElement> word)
    {

        return null; // TODO
    }
}
