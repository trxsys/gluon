package x.cfg;

import java.util.Collection;
import java.util.Set;
import java.util.Map;

import java.util.LinkedList;
import java.util.HashMap;
import java.util.HashSet;

public class Cfg
{
    private Map<NonTerminal,Collection<Production>> productions;
    private NonTerminal start;

    private int size=0;

    public Set<LexicalElement> lexicalElements;
    public Set<NonTerminal> nonterminals;
    public Set<Terminal> terminals;
    
    public Cfg()
    {
        productions=new HashMap<NonTerminal,Collection<Production>>();
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
            Collection<Production> c=new LinkedList<Production>();

            c.add(p);
            productions.put(p.getHead(),c);
        }
        else
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
        return getProductionsOf(start) == 1;
    }

    private void rewrite(Map<NonTerminal,Collection<Production>> nonTermUsages,
                         NonTerminal oldTerm, LexicalElement newElement)
    {
        for (Production p: nonTermUsages.get(oldTerm))
        {
            p.rewrite(oldTerm,newElement);

            // we must update prods
            if (newElement instanceof NonTerminal)
            {
                assert nonTermUsages.containsKey((NonTerminal)newElement);

                nonTermUsages.get((NonTerminal)newElement).add(p);
            }
        }
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

    public void optimize()
    {
        Map<NonTerminal,Collection<Production>> nonTermUsages=nonTermUsages();
        Collection<NonTerminal> toRemove=new LinkedList<NonTerminal>();

        for (Collection<Production> prods: productions.values())
            if (prods.size() == 1)
            {
                Production prod=prods.iterator().next();
                LexicalElement body; 

                if (prod.getBody().size() != 1 || prod.getHead().equals(start))
                    continue;

                assert prod.getBody().size() == 1;

                body=prod.getBody().get(0);

                if (body.equals(prod.getHead()))
                    continue;

                rewrite(nonTermUsages,prod.getHead(),body);

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
