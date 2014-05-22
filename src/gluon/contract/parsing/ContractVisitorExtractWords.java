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

package gluon.contract.parsing;

import gluon.contract.parsing.node.*;
import gluon.contract.parsing.analysis.Analysis;

import gluon.grammar.Terminal;
import gluon.analysis.programBehavior.PBTerminal;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

public class ContractVisitorExtractWords
    implements Analysis
{
    private Map<Node,Collection<List<PBTerminal>>> words;
    private Node root;

    public ContractVisitorExtractWords()
    {
        root=null;
        words=new HashMap<Node,Collection<List<PBTerminal>>>(64);
    }

    @Override
    public void caseStart(Start node)
    {
        root=node.getPClause();
        root.apply(this);
    }

    @Override
    public void caseAAltClause(AAltClause node)
    {
        List<List<PBTerminal>> w=new ArrayList<List<PBTerminal>>(16);

        node.getL().apply(this);
        node.getR().apply(this);

        w.addAll(words.get(node.getL()));
        w.addAll(words.get(node.getR()));

        words.put(node,w);
    }

    @Override
    public void caseAConcatClause(AConcatClause node)
    {
        List<List<PBTerminal>> w=new ArrayList<List<PBTerminal>>(16);

        node.getL().apply(this);
        node.getR().apply(this);

        for (List<PBTerminal> wl: words.get(node.getL()))
            for (List<PBTerminal> wr: words.get(node.getR()))
            {
                List<PBTerminal> wlr=new ArrayList<PBTerminal>(wl.size()+wr.size());

                wlr.addAll(wl);
                wlr.addAll(wr);

                w.add(wlr);
            }

        words.put(node,w);
    }

    @Override
    public void caseAMethodClause(AMethodClause node)
    {
        List<List<PBTerminal>> w=new ArrayList<List<PBTerminal>>(1);
        List<PBTerminal> word=new ArrayList<PBTerminal>(1);
        String methodName=node.getMethod().getText();
        PBTerminal term=new PBTerminal(methodName);

        for (TId varId: node.getId())
        {
            String var=varId.getText();

            if (var.equals("_"))
                term.addArgument(null);
            else
                term.addArgument(var);
        }

        if (node.getRet() != null)
            term.setReturn(node.getRet().getText());

        word.add(term);
        w.add(word);

        words.put(node,w);
    }

    public Collection<List<PBTerminal>> getWords()
    {
        assert root != null && words.containsKey(root);

        return words.get(root);
    }

    @Override
    public void caseTLpar(TLpar node) { }

    @Override
    public void caseTRpar(TRpar node) { }

    @Override
    public void caseTAlt(TAlt node) { }

    @Override
    public void caseTConcat(TConcat node) { }

    @Override
    public void caseTComma(TComma node) { }

    @Override
    public void caseTEq(TEq node) { }

    @Override
    public void caseTId(TId node) { }

    @Override
    public void caseEOF(EOF node) { }

    @Override
    public void caseInvalidToken(InvalidToken node) { }

    @Override
    public Object getIn(Node node) { return null; }

    @Override
    public void setIn(Node node, Object o) { }

    @Override
    public Object getOut(Node node) { return null; }

    @Override
    public void setOut(Node node, Object o) { }

}
