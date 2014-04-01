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

package gluon.grammar;

public class CfgOptimizer
{
    private CfgOptimizer()
    {
        assert false;
    }

    public Cfg optimize(Cfg grammar)
    {
        return grammar; // TODO
    }
    /*
          
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
                
                if (prod.getBody().size() > 1 || prod.getHead().equals(start)
                    || prod.getHead().noRemove())
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
    */
}
