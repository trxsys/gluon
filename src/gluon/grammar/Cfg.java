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

    private Map<Symbol,Set<Production>> productionsContaining;

    private Set<Symbol> lexicalElements;
    private Set<NonTerminal> nonterminals;
    private Set<Terminal> terminals;
    private boolean LESetsUptodate; /* true if the above sets are up to date */

    public Cfg()
    {
        clear();
    }

    public Cfg(NonTerminal start, Collection<Production> prods)
    {
        this();

        for (Production p: prods)
            addProduction(p);

        setStart(start);
    }

    private void clear()
    {
        productions=new HashMap<NonTerminal,Set<Production>>();
        start=null;
        size=0;

        productionsContaining=new HashMap<Symbol,Set<Production>>();

        LESetsUptodate=true;
        lexicalElements=new HashSet<Symbol>();
        nonterminals=new HashSet<NonTerminal>();
        terminals=new HashSet<Terminal>();
    }

    private void dprintln(String s)
    {
        if (DEBUG)
            System.out.println(this.getClass().getSimpleName()+": "+s);
    }

    public boolean addProduction(Production p)
    {
        if (!productions.containsKey(p.getHead()))
        {
            Set<Production> c=new HashSet<Production>();

            productions.put(p.getHead(),c);
        }
        else if (productions.get(p.getHead()).contains(p))
        {
            dprintln("    filtering "+p+": already there");
            return false;
        }

        productions.get(p.getHead()).add(p);

        updateLESetsProduction(p);

        /* Maintain productionsContaining */
        for (Symbol e: p.getBody())
        {
            if (!productionsContaining.containsKey(e))
            {
                Set<Production> c=new HashSet<Production>();

                productionsContaining.put(e,c);
            }

            productionsContaining.get(e).add(p);
        }

        size++;

        return true;
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
        return productions.containsKey(n)
            ? new ArrayList<Production>(productions.get(n))
            : new ArrayList<Production>(0);
    }

    public Collection<Production> getProductionsContaining(Symbol e)
    {
        return productionsContaining.containsKey(e)
            ? new ArrayList<Production>(productionsContaining.get(e))
            : new ArrayList<Production>(0);
    }

    public boolean removeProduction(Production prod)
    {
        Set<Production> prodSet;

        prodSet=productions.get(prod.getHead());

        if (prodSet == null)
            return false;

        if (!prodSet.remove(prod))
            return false;

        if (prodSet.size() == 0)
            productions.remove(prod.getHead());

        size--;

        LESetsUptodate=false;

        /* Maintain productionsContaining */
        for (Symbol e: prod.getBody())
            productionsContaining.get(e).remove(prod);

        return true;
    }

    public int size()
    {
        return size;
    }

    private void updateLESetsProduction(Production prod)
    {
        lexicalElements.add(prod.getHead());
        nonterminals.add(prod.getHead());

        for (Symbol e: prod.getBody())
        {
            if (e instanceof NonTerminal)
                nonterminals.add((NonTerminal)e);
            else if (e instanceof Terminal)
                terminals.add((Terminal)e);

            lexicalElements.add(e);
        }
    }

    private void updateLESets()
    {
        if (LESetsUptodate)
            return;

        lexicalElements.clear();
        nonterminals.clear();
        terminals.clear();

        for (Collection<Production> c: productions.values())
            for (Production p: c)
                updateLESetsProduction(p);
    }

    public Collection<Symbol> getSymbols()
    {
        updateLESets();

        return lexicalElements;
    }

    public Collection<Terminal> getTerminals()
    {
        updateLESets();

        return terminals;
    }

    public Collection<NonTerminal> getNonTerminals()
    {
        updateLESets();

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
        return new Cfg(start,getProductions());
    }
}
