package gluon.cfg.parsing.parseTree;

import java.util.LinkedList;
import java.util.ArrayList;
import java.util.Stack;
import java.util.List;

import gluon.cfg.Production;
import gluon.cfg.LexicalElement;
import gluon.cfg.NonTerminal;
import gluon.cfg.Terminal;

import gluon.cfg.parsing.parsingTable.parsingAction.*;

class ParseTreeNode
{
    private LexicalElement elem;
    private ParseTreeNode parent;
    public int count; /* For getLCA() */

    public ParseTreeNode(LexicalElement e)
    {
        elem=e;
        parent=null;
        count=0;
    }

    public void setParent(ParseTreeNode p)
    {
        parent=p;
    }


    public ParseTreeNode getParent()
    {
        return parent;
    }

    public LexicalElement getElem()
    {
        return elem;
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
        leafs=null;
    }

    public void buildTree(List<Terminal> word, List<ParsingAction> actions)
    {
        Stack<ParseTreeNode> stack=new Stack<ParseTreeNode>();
        int pos=0;

        leafs=new ArrayList<ParseTreeNode>(word.size());
        
        for (ParsingAction a: actions)
            if (a instanceof ParsingActionReduce)
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
            else if (a instanceof ParsingActionShift)
            {
                ParseTreeNode node=new ParseTreeNode(word.get(pos));
                stack.push(node);
                leafs.add(node);
                pos++;
            }
            else if (a instanceof ParsingActionAccept)
                assert pos == word.size()-1; /* -1 because of $ */
    }

    public NonTerminal getLCA()
    {
        assert leafs != null;
        
        for (ParseTreeNode node: leafs)
            while (node != null)
            {
                node.count++;

                if (node.count == leafs.size()
                    && node.getElem() instanceof NonTerminal)
                    return (NonTerminal)node.getElem();

                node=node.getParent();
            }

        return null;
    }
}
