/* This file is part of Gluon.
 *
 * Gluon is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Gluon is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Gluon.  If not, see <http://www.gnu.org/licenses/>.
 */

package gluon.parsing.parseTree;

import gluon.grammar.EOITerminal;
import gluon.grammar.NonTerminal;
import gluon.grammar.Symbol;
import gluon.grammar.Terminal;
import gluon.parsing.parsingTable.parsingAction.ParsingAction;
import gluon.parsing.parsingTable.parsingAction.ParsingActionAccept;
import gluon.parsing.parsingTable.parsingAction.ParsingActionReduce;
import gluon.parsing.parsingTable.parsingAction.ParsingActionShift;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class ParseTree
{
    private static final boolean DEBUG=false;

    private List<ParseTreeNode> leafs;
    private final List<Terminal> word;
    private final List<ParsingAction> actions;

    public ParseTree(List<Terminal> word, List<ParsingAction> actions)
    {
        this.word=word;
        this.actions=actions;
    }

    public void buildTree()
    {
        Stack<ParseTreeNode> stack=new Stack<ParseTreeNode>();
        int pos=0;

        gluon.profiling.Timer.start(".buildTree");

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

                    /* Update leaf with true parsed terminal */
                    if (node.getElem() instanceof Terminal)
                    {
                        Symbol nodeTerm=red.getProduction()
                            .getBody().get(len-1-i);

                        assert nodeTerm instanceof Terminal;

                        node.setElem(nodeTerm);
                    }

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
            {
                int inputTerminals;

                inputTerminals=word.size()
                    -(word.get(word.size()-1) instanceof EOITerminal ? 1 : 0);

                assert pos == inputTerminals;
            }

        gluon.profiling.Timer.stop(".buildTree");
    }

    public List<Terminal> getTerminals()
    {
        List<Terminal> terminals=new ArrayList<Terminal>(leafs.size());

        assert leafs != null;

        for (ParseTreeNode node: leafs)
        {
            assert node.getElem() instanceof Terminal;

            terminals.add((Terminal)node.getElem());
        }

        return terminals;
    }

    public ParseTreeNode getLCA()
    {
        assert leafs != null;

        for (ParseTreeNode node: leafs)
            while (node != null)
            {
                node.count++;

                if (node.count == leafs.size()
                    && node.getElem() instanceof NonTerminal)
                    return node;

                node=node.getParent();
            }

        return null;
    }
}
