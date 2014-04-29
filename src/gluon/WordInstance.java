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

package gluon;

import soot.Value;
import soot.SootMethod;
import soot.Unit;

import soot.jimple.Stmt;
import soot.jimple.AssignStmt;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;

import gluon.analysis.programBehavior.PPTerminal;
import gluon.analysis.programBehavior.PPNonTerminal;
import gluon.analysis.valueEquivalence.ValueEquivAnalysis;
import gluon.analysis.valueEquivalence.ValueM;

import gluon.grammar.Terminal;
import gluon.grammar.NonTerminal;
import gluon.parsing.parsingTable.parsingAction.*;
import gluon.parsing.parseTree.ParseTree;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;

public class WordInstance
{
    private static final boolean DEBUG=false;

    private PPNonTerminal lca;
    private List<Terminal> word;
    private List<ParsingAction> actions;

    public WordInstance(PPNonTerminal lca, List<Terminal> word,
                        List<ParsingAction> actions)
    {
        this.lca=lca;
        this.word=word;
        this.actions=actions;
    }

    private void dprint(String s)
    {
        if (DEBUG)
            System.out.print(s);
    }

    private void dprintln(String s)
    {
        dprint(s+"\n");
    }

    public SootMethod getLCAMethod()
    {
        return lca.getMethod();
    }

    public PPNonTerminal getLCA()
    {
        return lca;
    }

    private ParseTree parseTree=null;
    public ParseTree getParseTree()
    {
        ParseTree tree;

        if (parseTree != null)
            return parseTree;

        tree=new ParseTree(word,actions);

        tree.buildTree();

        return tree;
    }

    /* Memoization */
    private List<PPTerminal> parsingTerminals=null;
    public List<PPTerminal> getParsingTerminals()
    {
        ParseTree tree;
        List<PPTerminal> ppterms;
        List<Terminal> terms;

        if (parsingTerminals != null)
            return parsingTerminals;

        tree=getParseTree();

        terms=tree.getTerminals();
        ppterms=new ArrayList<PPTerminal>(terms.size());

        for (Terminal t: terms)
            ppterms.add((PPTerminal)t);

        return parsingTerminals=ppterms;
    }

    public boolean assertLCASanityCheck()
    {
        if (DEBUG)
        {
            ParseTree ptree=new ParseTree(word,actions);

            ptree.buildTree();

            return lca.equals(ptree.getLCA().getElem());
        }
        else
            return true;
    }

    public String actionsStr()
    {
        String r="";

        for (ParsingAction a: actions)
        {
            if (a instanceof ParsingActionShift)
                r+="s ; ";
            else if (a instanceof ParsingActionReduce)
                r+=((ParsingActionReduce)a).getProduction()+" ; ";
            else if (a instanceof ParsingActionAccept)
                r+=a.toString();
        }

        return r;
    }

    public static String wordStr(List<Terminal> word)
    {
        String r="";

        for (int i=0; i < word.size(); i++)
        {
            Terminal term=word.get(i);

            if (term instanceof gluon.grammar.EOITerminal)
                break;

            assert term instanceof PPTerminal;

            r+=(i > 0 ? " " : "")+((PPTerminal)term).getFullName();
        }

        return r;
    }

    public String wordStr()
    {
        return wordStr(word);
    }

    private int tryUnify(Map<String,ValueM> unif, String contractVar, ValueM v,
                         ValueEquivAnalysis vEquiv)
    {
        if (contractVar == null)
            return 0;

        if (unif.containsKey(contractVar))
            return vEquiv.equivTo(unif.get(contractVar),v) ? 0 : -1;

        unif.put(contractVar,v);

        return 0;
    }

    public boolean argumentsMatch(ValueEquivAnalysis vEquiv)
    {
        List<PPTerminal> parsedWord;
        /* contract variable -> program value */
        Map<String,ValueM> unif=new HashMap<String,ValueM>();

        parsedWord=getParsingTerminals();

        assert parsedWord.size() == word.size()-1; /* -1 because of the '$' */

        for (int i=0; i < parsedWord.size(); i++)
        {
            PPTerminal termC=(PPTerminal)word.get(i);
            PPTerminal termP=parsedWord.get(i);
            SootMethod method=termP.getCodeMethod();
            List<String> argumentsC;
            Unit unit;

            assert termC.equals(termP); /* equals ignores arguments and return */
            assert method != null;

            argumentsC=termC.getArguments();

            unit=termP.getCodeUnit();

            if (termC.getReturn() != null)
            {
                String contractVar=termC.getReturn();

                /* unify return value */
                if (unit instanceof AssignStmt)
                {
                    ValueM v=new ValueM(method,((AssignStmt)unit).getLeftOp());

                    dprintln("      Trying unification "+contractVar+" <-> "+v);

                    if (tryUnify(unif,contractVar,v,vEquiv) != 0)
                        return false;
                }
                else
                {
                    /* If control reaches here then the contract does specify a
                     * return value but the word in the client program does not
                     * assign the return value to a variable.  In this case
                     * we don't fail immediatly because it is possible that the
                     * variable is not used elsewhere.  So we unify the variable
                     * with null.  That variable will not be able to be unified
                     * with anything but null values.
                     */

                    if (tryUnify(unif,contractVar,null,vEquiv) != 0)
                        return false;
                }
            }

            for (int j=0; argumentsC != null && j < argumentsC.size(); j++)
            {
                InvokeExpr call=((Stmt)unit).getInvokeExpr();
                String contractVar=argumentsC.get(j);
                ValueM v;

                v=new ValueM(method,call.getArg(j));

                dprintln("      Trying unification "+contractVar+" <-> "+v);

                if (tryUnify(unif,contractVar,v,vEquiv) != 0)
                    return false;
            }
        }

        return true;
    }

    @Override
    public boolean equals(Object o)
    {
        WordInstance other;
        List<PPTerminal> thisParsingTerminals;
        List<PPTerminal> otherParsingTerminals;

        if (!(o instanceof WordInstance))
            return false;

        other=(WordInstance)o;

        if (!getLCAMethod().equals(other.getLCAMethod()))
            return false;

        thisParsingTerminals=getParsingTerminals();
        otherParsingTerminals=other.getParsingTerminals();

        if (thisParsingTerminals.size() != otherParsingTerminals.size())
            return false;

        for (int i=0; i < thisParsingTerminals.size(); i++)
        {
            PPTerminal tterm=thisParsingTerminals.get(i);
            PPTerminal oterm=otherParsingTerminals.get(i);

            if (tterm.getLineNumber() != oterm.getLineNumber()
                || !tterm.getSourceFile().equals(oterm.getSourceFile()))
                return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int h=getLCAMethod().getSignature().hashCode();

        for (PPTerminal t: getParsingTerminals())
            h=h^(t.getLineNumber()*92821)^t.getSourceFile().hashCode();

        return h;
    }
}
