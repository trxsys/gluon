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

package gluon.cfg;

import java.util.Collection;
import java.util.Set;
import java.util.Map;
import java.util.List;

import java.util.LinkedList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class Cfg
{
    private static final boolean DEBUG=false;
    
    private Map<NonTerminal,Set<Production>> productions;
    private NonTerminal start;
    
    private int size=0;
    
    private Set<LexicalElement> lexicalElements;
    private Set<NonTerminal> nonterminals;
    private Set<Terminal> terminals;
    
    public Cfg()
    {
        clear();
    }
    
    private void clear()
    {
        productions=new HashMap<NonTerminal,Set<Production>>();
        start=null;
        size=0;
        
        lexicalElements=new HashSet<LexicalElement>();
        nonterminals=new HashSet<NonTerminal>();
        terminals=new HashSet<Terminal>();
    }
    
    public void addProduction(Production p)
    {
        if (!productions.containsKey(p.getHead()))
        {
            Set<Production> c=new HashSet<Production>();
            
            productions.put(p.getHead(),c);
        }
        else if (productions.get(p.getHead()).contains(p))
        {
            dprintln("    filtering "+p+": already there");
            return;
        }

        productions.get(p.getHead()).add(p);
        
        lexicalElements.add(p.getHead());
        nonterminals.add(p.getHead());
        
        for (LexicalElement e: p.getBody())
        {
            if (e instanceof NonTerminal)
                nonterminals.add((NonTerminal)e);
            else if (e instanceof Terminal)
                terminals.add((Terminal)e);
            
            lexicalElements.add(e);
        }
        
        size++;
    }
    
    public void setStart(NonTerminal s)
    {
        assert nonterminals.contains(s);
        
        start=s;
    }
    
    public NonTerminal getStart()
    {
        return start;
    }
    
    public Collection<Production> getProductions()
    {
        Collection<Production> prods=new LinkedList<Production>();
        
        for (Collection<Production> c: productions.values())
            prods.addAll(c);
        
        return prods;
    }
    
    public Collection<Production> getProductionsOf(NonTerminal n)
    {
        return productions.containsKey(n) ? productions.get(n)
            : new LinkedList<Production>();
    }
    
    public int size()
    {
        return size;
    }
    
    public Collection<LexicalElement> getLexicalElements()
    {
        return lexicalElements;
    }
    
    public Collection<Terminal> getTerminals()
    {
        return terminals;
    }
    
    public Collection<NonTerminal> getNonTerminals()
    {
        return nonterminals;
    }
    
    public boolean hasUniqueStart()
    {
        return getProductionsOf(start).size() == 1;
    }
    
    private Map<NonTerminal,Collection<Production>> nonTermUsages()
    {
        Map<NonTerminal,Collection<Production>> nonTermUsages
            =new HashMap<NonTerminal,Collection<Production>>();
        
        for (Production p: getProductions())
            for (LexicalElement e: p.getBody())
                if (e instanceof NonTerminal)
                {
                    NonTerminal n=(NonTerminal)e;

                    if (!nonTermUsages.containsKey(n))
                        nonTermUsages.put(n,new LinkedList<Production>());
                    
                    nonTermUsages.get(n).add(p);
                }
        
        return nonTermUsages;
    }
    
    private void rewrite(Map<NonTerminal,Collection<Production>> nonTermUsages,
                         NonTerminal oldTerm, LexicalElement newElement)
    {
        Collection<Production> prods=nonTermUsages.get(oldTerm);

        if (prods == null)
            return;

        for (Production p: prods)
        {
            productions.get(p.getHead()).remove(p);

            p.rewrite(oldTerm,newElement);
            
            productions.get(p.getHead()).add(p);

            // we must update nonTermUsages
            if (newElement instanceof NonTerminal)
            {
                assert nonTermUsages.containsKey((NonTerminal)newElement);
                
                nonTermUsages.get((NonTerminal)newElement).add(p);
            }
        }
    }
    
    private void erase(Map<NonTerminal,Collection<Production>> nonTermUsages,
                       NonTerminal nonterm)
    {
        Collection<Production> prods=nonTermUsages.get(nonterm);

        if (prods == null)
            return;

        for (Production p: prods)
        {
            productions.get(p.getHead()).remove(p);

            p.erase(nonterm);

            productions.get(p.getHead()).add(p);

            // no need to update nonTermUsages
        }
    }

    private void optimizeSingletonProductions()
    {
        Map<NonTerminal,Collection<Production>> nonTermUsages=nonTermUsages();
        Collection<NonTerminal> toRemove=new LinkedList<NonTerminal>();
        
        for (Set<Production> prods: productions.values())
            if (prods.size() == 1)
            {
                Production prod=prods.iterator().next();
                LexicalElement body; 
                
                if (prod.getBody().size() > 1 || prod.getHead().equals(start))
                    continue;
                
                assert prod.getBody().size() <= 1;
                
                body=prod.getBody().size() == 1 ? prod.getBody().get(0) : null;
                
                if (prod.getHead().equals(body))
                    continue;
                
                if (body != null)
                    rewrite(nonTermUsages,prod.getHead(),body);
                else
                    erase(nonTermUsages,prod.getHead());

                toRemove.add(prod.getHead());
            }
        
        for (NonTerminal n: toRemove)
        {
            productions.remove(n);
            nonterminals.remove(n);
            lexicalElements.remove(n);
            size--;
        }
    }

    public void optimize()
    {
        optimizeSingletonProductions();
    }
    
    private void dprintln(String s)
    {
        if (DEBUG)
            System.out.println(this.getClass().getSimpleName()+": "+s);
    }
    
    private Collection<ArrayList<LexicalElement>>
        getPrefixes(ArrayList<LexicalElement> body)
    {
        Collection<ArrayList<LexicalElement>> prefixes
            =new LinkedList<ArrayList<LexicalElement>>();
        
        for (int i=1; i <= body.size(); i++)
        {
            ArrayList<LexicalElement> pre=new ArrayList<LexicalElement>(i);
            LexicalElement last;
            
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
                
                prefixes.add(new ArrayList<LexicalElement>(pre));
            }
            
            pre.add(last);
            
            prefixes.add(pre);            
        }
        
        // Add empty production
        prefixes.add(new ArrayList<LexicalElement>());
        
        return prefixes;
    }
    
    private void addPrefixes(Collection<Production> prods)
    {
        for (Production p: prods)
        {
            NonTerminal newProdHead=p.getHead().clone();
            
            newProdHead.setName(newProdHead.getName()+"<");
            
            dprintln("prefixes of "+p+":");
            
            for (ArrayList<LexicalElement> pre: getPrefixes(p.getBody()))
            {
                Production preProd=new Production(newProdHead,pre);
                
                dprintln("  "+preProd);
                addProduction(preProd);
            }
            
            dprintln("");
        }
    }
    
    private Collection<ArrayList<LexicalElement>>
        getSuffixes(ArrayList<LexicalElement> body)
    {
        Collection<ArrayList<LexicalElement>> suffixes
            =new LinkedList<ArrayList<LexicalElement>>();
        
        for (int i=body.size(); i >= 1; i--)
        {
            ArrayList<LexicalElement> suff=new ArrayList<LexicalElement>(i);
            LexicalElement first;
            
            if (body.get(i-1) instanceof NonTerminal)
            {
                first=body.get(i-1).clone();
                ((NonTerminal)first).setName(first.getName()+">");
            }
            else
            {
                List<LexicalElement> l=body.subList(i,body.size());

                suffixes.add(new ArrayList<LexicalElement>(l));

                first=body.get(i-1);
            }
            
            suff.add(first);
            
            for (int j=i; j < body.size(); j++)
                suff.add(body.get(j));
            
            suffixes.add(suff);
        }
        
        // Add empty production
        suffixes.add(new ArrayList<LexicalElement>());
        
        return suffixes;
    }
    
    private void addSuffixes(Collection<Production> prods)
    {
        for (Production p: prods)
        {
            NonTerminal newProdHead=p.getHead().clone();
            
            newProdHead.setName(newProdHead.getName()+">");
            
            dprintln("suffixes of "+p+":");
            
            for (ArrayList<LexicalElement> suff: getSuffixes(p.getBody()))
            {
                Production suffProd=new Production(newProdHead,suff);
                
                dprintln("  "+suffProd);
                addProduction(suffProd);
            }            
            
            dprintln("");
        }
    }

    private void getSubwords(List<LexicalElement> body,
                             Collection<ArrayList<LexicalElement>> subwords)
    {
        for (int i=0; i <= body.size(); i++)
        {
            ArrayList<LexicalElement> left;
            ArrayList<LexicalElement> right;
            Collection<ArrayList<LexicalElement>> leftSuffs;
            Collection<ArrayList<LexicalElement>> rightPres;

            left=new ArrayList<LexicalElement>(body.subList(0,i));
            right=new ArrayList<LexicalElement>(body.subList(i,body.size()));

            leftSuffs=getSuffixes(left);
            rightPres=getPrefixes(right);

            for (ArrayList<LexicalElement> leftSuff: leftSuffs)
                for (ArrayList<LexicalElement> rightPre: rightPres)
                {
                    ArrayList<LexicalElement> sub
                        =new ArrayList<LexicalElement>(leftSuff.size()
                                                       +rightPre.size());

                    sub.addAll(leftSuff);
                    sub.addAll(rightPre);

                    subwords.add(sub);
                }
        }

        for (int i=0; i < body.size(); i++)
        {
            ArrayList<LexicalElement> sub=new ArrayList<LexicalElement>(1);
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
            subwords.add(new ArrayList<LexicalElement>(0));

        for (int i=0; i < body.size(); i++)
            for (int j=i+1; j <= body.size(); j++)
                if (!(i == 0 && j == body.size()))
                    getSubwords(body.subList(i,j),subwords);
    }
    
    private Collection<ArrayList<LexicalElement>>
        getSubwords(List<LexicalElement> body)
    {
        Collection<ArrayList<LexicalElement>> subwords
            =new LinkedList<ArrayList<LexicalElement>>();

        getSubwords(body,subwords);

        return subwords;
    }
    
    private void addSubwords(Collection<Production> prods)
    {
        for (Production p: prods)
        {
            NonTerminal newProdHead=p.getHead().clone();
            
            newProdHead.setName(newProdHead.getName()+"<>");
            
            dprintln("subwords of "+p+":");
            
            for (ArrayList<LexicalElement> sub: getSubwords(p.getBody()))
            {
                Production subProd=new Production(newProdHead,sub);
                
                dprintln("  "+subProd);
                addProduction(subProd);
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
     * If the original grammar has a rule X -> Y Z, then the new grammar will
     * have:
     *
     *     X< -> Y< | Y Z<
     *     X> -> Z> | Y> Z
     *     X<> -> Y> Z< | Y<> | Z<>
     *
     * From http://www.reddit.com/r/compsci/comments/1drkvk/    \
     *        are_contextfree_languages_closed_under_subwords/ 
     */
    public void subwordClosure()
    {
        Collection<Production> prods=getProductions();
        
        addPrefixes(prods);        
        addSuffixes(prods);
        addSubwords(prods);
        
        NonTerminal newStart=start.clone();

        newStart.setName(newStart.getName()+"<>");

        setStart(newStart);
    }
    
    @Override
    public String toString()
    {
        String s;
        
        if (start == null)
            s="Grammar has no start non-terminal defined!\n";
        else
            s="Start: "+start.toString()+"\n";
        
        for (Production p: getProductions())
            s+=p.toString()+"\n";
        
        return s;
    }
}
