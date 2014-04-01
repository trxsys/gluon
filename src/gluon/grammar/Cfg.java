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
    
    private int size;
    
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

    private void dprintln(String s)
    {
        if (DEBUG)
            System.out.println(this.getClass().getSimpleName()+": "+s);
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

    public Cfg clone()
    {
        Cfg clone=new Cfg();

        for (Production p: getProductions())
            clone.addProduction(p);

        clone.setStart(getStart());

        return clone;
    }
}
