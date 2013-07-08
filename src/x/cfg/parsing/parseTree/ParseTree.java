package x.cfg.parsing.parseTree;

import java.util.LinkedList;
import java.util.ArrayList;
import java.util.Stack;
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

    public void setParent(ParseTreeNode p)
    {
        parent=p;
    }

    @Override
    public String toString()
    {
        return elem.toString()+"_"+hashCode();
    }
}

public class ParseTree
{
    private static final boolean DEBUG=false;

    private List<ParseTreeNode> leafs;
    
    public ParseTree()
    {
        leafs=new LinkedList<ParseTreeNode>();        
    }

    public void buildTree(List<Terminal> word, List<ParsingAction> actions)
    {
        Stack<ParseTreeNode> stack=new Stack<ParseTreeNode>();
        int pos=0;
        
        for (ParsingAction a: actions)
            if (a instanceof ParsingActionShift)
            {
                ParseTreeNode node=new ParseTreeNode(word.get(pos));
                stack.push(node);
                leafs.add(node);
                pos++;
            }
            else if (a instanceof ParsingActionReduce)
            {
                ParsingActionReduce red=(ParsingActionReduce)a;
                int len=red.getProduction().bodyLength();
                ParseTreeNode parent
                    =new ParseTreeNode(red.getProduction().getHead());

                for (int i=0; i < len; i++)
                {
                    ParseTreeNode node=stack.pop();
                    node.setParent(parent);

                    if (DEBUG)
                    {
                        System.out.println(parent);
                        System.out.println("      |");
                        System.out.println(node);
                        System.out.println();
                    }
                }

                stack.push(parent);
            }
            else if (a instanceof ParsingActionAccept)
                assert pos == word.size()-1; /* -1 because of $ */
    }

    public NonTerminal getLCANonTerm(List<LexicalElement> word)
    {

        return null; // TODO
    }
}
