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

package gluon.grammar.transform;

import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;

import gluon.grammar.Cfg;
import gluon.grammar.Production;
import gluon.grammar.NonTerminal;
import gluon.grammar.Symbol;

public class CfgSubwords
{
    private static final boolean DEBUG=false;

    private CfgSubwords()
    {
        assert false;
    }

    private static void dprintln(String s)
    {
        if (DEBUG)
            System.out.println("CfgSubwords: "+s);
    }

    private static Collection<ArrayList<Symbol>>
        getPrefixes(ArrayList<Symbol> body)
    {
        Collection<ArrayList<Symbol>> prefixes
            =new LinkedList<ArrayList<Symbol>>();

        for (int i=1; i <= body.size(); i++)
        {
            ArrayList<Symbol> pre=new ArrayList<Symbol>(i);
            Symbol last;

            for (int j=0; j < i-1; j++)
                pre.add(body.get(j));

            if (body.get(i-1) instanceof NonTerminal)
            {
                last=body.get(i-1).clone();
                ((NonTerminal)last).setName(last.getName()+"<");
            }
            else
            {
                last=body.get(i-1);

                prefixes.add(new ArrayList<Symbol>(pre));
            }

            pre.add(last);

            prefixes.add(pre);
        }

        // Add empty production
        prefixes.add(new ArrayList<Symbol>());

        return prefixes;
    }

    private static void addPrefixes(Cfg grammar, Collection<Production> prods)
    {
        for (Production p: prods)
        {
            NonTerminal newProdHead=p.getHead().clone();

            newProdHead.setName(newProdHead.getName()+"<");

            dprintln("prefixes of "+p+":");

            for (ArrayList<Symbol> pre: getPrefixes(p.getBody()))
            {
                Production preProd=new Production(newProdHead,pre);

                dprintln("  "+preProd);
                grammar.addProduction(preProd);
            }

            dprintln("");
        }
    }

    private static Collection<ArrayList<Symbol>>
        getSuffixes(ArrayList<Symbol> body)
    {
        Collection<ArrayList<Symbol>> suffixes
            =new LinkedList<ArrayList<Symbol>>();

        for (int i=body.size(); i >= 1; i--)
        {
            ArrayList<Symbol> suff=new ArrayList<Symbol>(i);
            Symbol first;

            if (body.get(i-1) instanceof NonTerminal)
            {
                first=body.get(i-1).clone();
                ((NonTerminal)first).setName(first.getName()+">");
            }
            else
            {
                List<Symbol> l=body.subList(i,body.size());

                suffixes.add(new ArrayList<Symbol>(l));

                first=body.get(i-1);
            }

            suff.add(first);

            for (int j=i; j < body.size(); j++)
                suff.add(body.get(j));

            suffixes.add(suff);
        }

        // Add empty production
        suffixes.add(new ArrayList<Symbol>());

        return suffixes;
    }

    private static void addSuffixes(Cfg grammar, Collection<Production> prods)
    {
        for (Production p: prods)
        {
            NonTerminal newProdHead=p.getHead().clone();

            newProdHead.setName(newProdHead.getName()+">");

            dprintln("suffixes of "+p+":");

            for (ArrayList<Symbol> suff: getSuffixes(p.getBody()))
            {
                Production suffProd=new Production(newProdHead,suff);

                dprintln("  "+suffProd);
                grammar.addProduction(suffProd);
            }

            dprintln("");
        }
    }

    private static void getSubwords(List<Symbol> body,
                                    Collection<ArrayList<Symbol>> subwords)
    {
        for (int i=0; i <= body.size(); i++)
        {
            ArrayList<Symbol> left;
            ArrayList<Symbol> right;
            Collection<ArrayList<Symbol>> leftSuffs;
            Collection<ArrayList<Symbol>> rightPres;

            left=new ArrayList<Symbol>(body.subList(0,i));
            right=new ArrayList<Symbol>(body.subList(i,body.size()));

            leftSuffs=getSuffixes(left);
            rightPres=getPrefixes(right);

            for (ArrayList<Symbol> leftSuff: leftSuffs)
                for (ArrayList<Symbol> rightPre: rightPres)
                {
                    ArrayList<Symbol> sub
                        =new ArrayList<Symbol>(leftSuff.size()
                                                       +rightPre.size());

                    sub.addAll(leftSuff);
                    sub.addAll(rightPre);

                    subwords.add(sub);
                }
        }

        for (int i=0; i < body.size(); i++)
        {
            ArrayList<Symbol> sub=new ArrayList<Symbol>(1);
            NonTerminal e;

            if (!(body.get(i) instanceof NonTerminal))
                continue;

            e=(NonTerminal)body.get(i).clone();

            e.setName(e.getName()+"<>");

            sub.add(e);

            subwords.add(sub);
        }

        // Add empty production (if the size is non zero there must be already
        // something that derives the empty word)
        if (body.size() == 0)
            subwords.add(new ArrayList<Symbol>(0));

        for (int i=0; i < body.size(); i++)
            for (int j=i+1; j <= body.size(); j++)
                if (!(i == 0 && j == body.size()))
                    getSubwords(body.subList(i,j),subwords);
    }

    private static Collection<ArrayList<Symbol>>
        getSubwords(List<Symbol> body)
    {
        Collection<ArrayList<Symbol>> subwords
            =new LinkedList<ArrayList<Symbol>>();

        getSubwords(body,subwords);

        return subwords;
    }

    private static void addSubwords(Cfg grammar, Collection<Production> prods)
    {
        for (Production p: prods)
        {
            NonTerminal newProdHead=p.getHead().clone();

            newProdHead.setName(newProdHead.getName()+"<>");

            dprintln("subwords of "+p+":");

            for (ArrayList<Symbol> sub: getSubwords(p.getBody()))
            {
                Production subProd=new Production(newProdHead,sub);

                dprintln("  "+subProd);
                grammar.addProduction(subProd);
            }

            dprintln("");
        }
    }

    /*
     * For each nonterminal X, add 3 new ones X< , X> , X<>.
     *
     * The idea is that X< will generate all prefixes of strings generated
     * by X; X> will generate all suffixes, and X<> will generate all
     * substrings.
     *
     * The new start symbol will be S<> .
     *
     * If the original grammar has a rule X → Y Z, then the new grammar will
     * have:
     *
     *     X< → Y< | Y Z<
     *     X> → Z> | Y> Z
     *     X<> → Y> Z< | Y<> | Z<>
     *
     * From http://www.reddit.com/r/compsci/comments/1drkvk/    \
     *        are_contextfree_languages_closed_under_subwords/
     */
    public static void subwordClosure(Cfg grammar)
    {
        Collection<Production> prods=grammar.getProductions();
        NonTerminal newStart;

        addPrefixes(grammar,prods);
        addSuffixes(grammar,prods);
        addSubwords(grammar,prods);

        newStart=grammar.getStart().clone();

        newStart.setName(newStart.getName()+"<>");

        grammar.setStart(newStart);
    }

    public static Cfg subwordGfg(Cfg grammar)
    {
        Cfg subwordGrammar=grammar.clone();

        subwordClosure(subwordGrammar);

        return subwordGrammar;
    }
}
