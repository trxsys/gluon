package x.cfg;

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
    private static final boolean DEBUG=true;
    
    private Map<NonTerminal,Collection<Production>> productions;
    private NonTerminal start;
    
    private int size=0;
    
    public Set<LexicalElement> lexicalElements;
    public Set<NonTerminal> nonterminals;
    public Set<Terminal> terminals;
    
    public Cfg()
    {
        clear();
    }
    
    private void clear()
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
            /* This must not be a set since production are modified in some
             * methods. This whould cause corruption of both hash tables and trees.
             */
            Collection<Production> c=new LinkedList<Production>();
            
            c.add(p);
            productions.put(p.getHead(),c);
        }
        else if (productions.get(p.getHead()).contains(p))
            dprintln("    filtering "+p+": already there");

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
    
    private Collection<ArrayList<LexicalElement>>
        getSubwords(ArrayList<LexicalElement> body)
    {
        Collection<ArrayList<LexicalElement>> subwords
            =new LinkedList<ArrayList<LexicalElement>>();
        
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

        // Add empty production (if the size is non zero the must be already)
        // something that derives the empty word
        if (body.size() == 0)
            subwords.add(new ArrayList<LexicalElement>(0));
                
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
