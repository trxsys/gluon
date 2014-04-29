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

package gluon.parsing.parsingTable;

/* This is a implementation of a LR(0) parsing table generator as described in
 * the dragon book (Compilers - Principles, Techniques, & Tools; second edition).
 * Pages 241..254.
 */

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.List;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.HashSet;
import java.util.HashMap;

import gluon.grammar.Cfg;
import gluon.grammar.NonTerminal;
import gluon.grammar.Terminal;
import gluon.grammar.EOITerminal;
import gluon.grammar.Production;
import gluon.grammar.Symbol;

import gluon.parsing.parsingTable.parsingAction.*;

public class ParsingTable
{
    private static final boolean DEBUG=false;

    private static final Terminal EOI_TERMINAL=new EOITerminal();

    // state -> nonterm -> state
    private List<Map<NonTerminal,Integer>> gotoTable;
    // state -> term -> set of actions
    private List<Map<Terminal,Collection<ParsingAction>>> actionTable;

    // state -> items of the state
    private List<Set<Item>> states;
    // items of the state -> state
    private Map<Set<Item>,Integer> stateMap;
    private int initialState;

    private Map<Symbol,Set<List<Terminal>>> first;
    private Map<NonTerminal,Collection<Terminal>> follow;

    private Cfg grammar;

    public ParsingTable(Cfg g)
    {
        grammar=g;
        gotoTable=null;
        actionTable=null;
        states=null;
        follow=null;
        initialState=-1;
    }

    private void debugPrintStates()
    {
        System.out.println("== States ==");
        System.out.println();

        for (Set<Item> state: states)
        {
            for (Item i: state)
                System.out.println(i);

            System.out.println();
        }
    }

    private void debugPrintGotoTable()
    {
        System.out.println("== Goto Table ==");

        for (int i=0; i < gotoTable.size(); i++)
        {
            System.out.print(i+"    ");

            for (Map.Entry<NonTerminal,Integer> entry: gotoTable.get(i).entrySet())
                System.out.print(entry.getKey()+"↦"+entry.getValue()+"  ");

            System.out.println();
        }

        System.out.println();
    }

    private void debugPrintActionTable()
    {
        System.out.println("== Action Table ==");

        for (int i=0; i < actionTable.size(); i++)
        {
            System.out.print(i+"    ");

            for (Map.Entry<Terminal,Collection<ParsingAction>> entry:
                     actionTable.get(i).entrySet())
            {
                int count=0;

                System.out.print(entry.getKey()+"->");

                for (ParsingAction a: entry.getValue())
                    System.out.print((count++ == 0 ? "" : ",")+a.toString());

                System.out.print("  ");
            }

            System.out.println();
        }

        System.out.println();
    }

    private void closure(Set<Item> items)
    {
        Set<Item> nextNewItems;
        Set<Item> newItems;
        boolean fixPoint;

        nextNewItems=new HashSet<Item>();
        newItems=new HashSet<Item>();

        newItems.addAll(items);

        do
        {
            nextNewItems.clear();

            for (Item i: newItems)
            {
                /* If we have item A -> B . C D and C -> E is a production,
                 * then we add C -> . E to nextNewItems
                 */
                Symbol c=i.getNextToDot();

                if (!(c instanceof NonTerminal))
                    continue;

                Collection<Production> toAdd=grammar.getProductionsOf((NonTerminal)c);

                for (Production p: toAdd)
                {
                    Item newItem=new Item(p,0);

                    if (!items.contains(newItem))
                        nextNewItems.add(newItem);
                }
            }

            fixPoint=nextNewItems.size() == 0;

            items.addAll(nextNewItems);

            {
                Set<Item> tmp=newItems;

                newItems=nextNewItems;
                nextNewItems=tmp;
            }

        } while (!fixPoint);
    }

    private Set<Item> goTo(Set<Item> items, Symbol e)
    {
        Set<Item> ret=new HashSet<Item>();

        for (Item i: items)
        {
            /* If i is A -> B . C D and e == C then add A -> B C . D to ret
             */
            Symbol c=i.getNextToDot();

            if (e.equals(c))
                ret.add(new Item(i.getProduction(),i.getDotPos()+1));
        }

        closure(ret);

        return ret;
    }

    private Production getStartProduction()
    {
        NonTerminal start=grammar.getStart();
        Collection<Production> prods=grammar.getProductionsOf(start);

        assert prods.size() == 1;

        return prods.iterator().next();
    }

    private Set<Item> buildInitialState()
    {
        Set<Item> state=new HashSet<Item>();

        state.add(new Item(getStartProduction(),0));

        closure(state);

        return state;
    }

    private void buildStateMap()
    {
        assert states != null;

        stateMap=new HashMap<Set<Item>,Integer>();

        for (int i=0; i < states.size(); i++)
            stateMap.put(states.get(i),i);
    }

    private Collection<Symbol> getStateNextLexicalSymbols(Set<Item> state)
    {
        Collection<Symbol> simbols=new HashSet<Symbol>(state.size()*2);

        for (Item i: state)
            if (i.getNextToDot() != null)
                simbols.add(i.getNextToDot());

        return simbols;
    }

    private void buildStates()
    {
        Set<Set<Item>> statesToAdd;
        Set<Set<Item>> newStates;
        boolean fixPoint;

        states=new ArrayList<Set<Item>>(grammar.size()*3);

        states.add(buildInitialState());
        initialState=0;

        statesToAdd=new HashSet<Set<Item>>(grammar.size()*3);
        newStates=new HashSet<Set<Item>>(grammar.size()*3);

        newStates.add(buildInitialState());

        do
        {
            statesToAdd.clear();

            for (Set<Item> state: newStates)
                for (Symbol e: getStateNextLexicalSymbols(state))
                {
                    Set<Item> nextState;

                    nextState=goTo(state,e);

                    if (nextState.size() > 0
                        && !states.contains(nextState))
                        statesToAdd.add(nextState);
                }

            fixPoint=statesToAdd.size() == 0;

            states.addAll(statesToAdd);

            {
                Set<Set<Item>> tmp=newStates;

                newStates=statesToAdd;
                statesToAdd=tmp;
            }
        } while (!fixPoint);

        buildStateMap();
    }

    private Collection<NonTerminal> getStateNextNonTerminals(Set<Item> state)
    {
        Collection<NonTerminal> simbols=new HashSet<NonTerminal>();

        for (Item i: state)
                if (i.getNextToDot() instanceof NonTerminal)
                    simbols.add((NonTerminal)i.getNextToDot());

        return simbols;
    }

    private void buildGotoTable()
    {
        gotoTable=new ArrayList<Map<NonTerminal,Integer>>(states.size());

        for (Set<Item> state: states)
        {
            Map<NonTerminal,Integer> gotoRow=new HashMap<NonTerminal,Integer>();

            for (NonTerminal n: getStateNextNonTerminals(state))
            {
                Set<Item> destState=goTo(state,n);

                if (destState.size() > 0)
                {
                    assert stateMap.containsKey(destState);

                    gotoRow.put(n,stateMap.get(destState));
                }
            }

            gotoTable.add(gotoRow);
        }
    }

    private void computeFirst()
    {
        final List<Terminal> EMPTY_WORD=new ArrayList<Terminal>(0);

        boolean fixPoint;

        first=new HashMap<Symbol,Set<List<Terminal>>>();

        for (Symbol e: grammar.getSymbols())
            first.put(e,new HashSet<List<Terminal>>());

        for (Terminal t: grammar.getTerminals())
        {
            List<Terminal> word=new ArrayList<Terminal>(1);

            word.add(t);

            first.get(t).add(word);
        }

        do
        {
            fixPoint=true;

            for (NonTerminal n: grammar.getNonTerminals())
                for (Production p: grammar.getProductionsOf(n))
                {
                    boolean deriveEmptyWord=true;
                    int beforeSize=first.get(n).size();

                    for (Symbol e: p.getBody())
                    {
                        Set<List<Terminal>> firstsE=first.get(e);

                        for (List<Terminal> f: firstsE)
                            if (!f.equals(EMPTY_WORD))
                                first.get(n).add(f);

                        if (!firstsE.contains(EMPTY_WORD))
                        {
                            deriveEmptyWord=false;
                            break;
                        }
                    }

                    if (deriveEmptyWord)
                        first.get(n).add(EMPTY_WORD);

                    if (first.get(n).size() != beforeSize)
                        fixPoint=false;
            }
        } while (!fixPoint);
    }

    private Set<List<Terminal>> first(List<Symbol> s)
    {
        final List<Terminal> EMPTY_WORD=new ArrayList<Terminal>(0);

        Set<List<Terminal>> ret=new HashSet<List<Terminal>>();
        boolean deriveEmptyWord=true;

        for (Symbol e: s)
        {
            Set<List<Terminal>> firstsE=first.get(e);

            for (List<Terminal> f: firstsE)
                if (!f.equals(EMPTY_WORD))
                    ret.add(f);

            if (!firstsE.contains(EMPTY_WORD))
            {
                deriveEmptyWord=false;
                break;
            }
        }

        if (deriveEmptyWord)
            ret.add(EMPTY_WORD);

        return ret;
    }

    private void computeFollow()
    {
        final List<Terminal> EMPTY_WORD=new ArrayList<Terminal>(0);

        boolean fixPoint;
        Collection<Production> productions=grammar.getProductions();

        follow=new HashMap<NonTerminal,Collection<Terminal>>(grammar.size());

        for (NonTerminal n: grammar.getNonTerminals())
            follow.put(n,new HashSet<Terminal>());

        follow.get(grammar.getStart()).add(EOI_TERMINAL);

        do
        {
            fixPoint=true;

            for (Production p: productions)
            {
                List<Symbol> right=new LinkedList<Symbol>();

                right.addAll(p.getBody());

                while (right.size() > 0)
                {
                    Symbol e=right.remove(0);
                    NonTerminal n;
                    Set<List<Terminal>> firstRight;
                    Collection<Terminal> followN;
                    int beforeSize;

                    if (!(e instanceof NonTerminal))
                        continue;

                    n=(NonTerminal)e;

                    followN=follow.get(n);

                    beforeSize=followN.size();

                    firstRight=first(right);

                    for (List<Terminal> t: firstRight)
                    {
                        Terminal term;

                        assert t.size() == 0 || t.size() == 1;

                        if (t.size() == 0)
                            continue;

                        term=t.get(0);

                        followN.add(term);
                    }

                    if (firstRight.contains(EMPTY_WORD))
                        followN.addAll(follow.get(p.getHead()));

                    if (followN.size() != beforeSize)
                        fixPoint=false;
                }
            }
        } while (!fixPoint);
    }

    private void buildActionTable()
    {
        actionTable=new ArrayList<Map<Terminal,Collection<ParsingAction>>>(states.size());

        for (Set<Item> state: states)
        {
            Map<Terminal,Collection<ParsingAction>> actionRow
                =new HashMap<Terminal,Collection<ParsingAction>>();

            for (Item i: state)
            {
                Symbol next=i.getNextToDot();

                if (next instanceof Terminal)
                {
                    Terminal t=(Terminal)next;
                    Set<Item> destState=goTo(state,t);
                    ParsingAction a;

                    assert stateMap.containsKey(destState);

                    a=new ParsingActionShift(stateMap.get(destState));

                    if (!actionRow.containsKey(t))
                        actionRow.put(t,new HashSet<ParsingAction>(8));

                    actionRow.get(t).add(a);
                }

                if (!i.getProduction().getHead().equals(grammar.getStart())
                    && i.isComplete())
                    for (Terminal t: follow.get(i.getProduction().getHead()))
                    {
                        // reduce H -> B
                        ParsingAction a;

                        a=new ParsingActionReduce(i.getProduction());

                        if (!actionRow.containsKey(t))
                            actionRow.put(t,new HashSet<ParsingAction>(8));

                        actionRow.get(t).add(a);
                    }

                // Item S' -> S .
                if (i.getProduction().getHead().equals(grammar.getStart())
                    && i.getDotPos() == 1)
                {
                    assert i.getProduction().bodyLength() == 1;

                    if (!actionRow.containsKey(EOI_TERMINAL))
                        actionRow.put(EOI_TERMINAL,new HashSet<ParsingAction>(8));

                    actionRow.get(EOI_TERMINAL).add(new ParsingActionAccept());
                }
            }

            actionTable.add(actionRow);
        }
    }

    public void buildParsingTable()
    {
        assert gotoTable == null : "Parsing table should only be built only once";

        assert grammar.hasUniqueStart() : "Grammar must only have a start production "
            +"to generate the parsing table";

        // 18'540
        if (DEBUG)
            System.out.println("start states "+System.currentTimeMillis());
        buildStates();
        if (DEBUG)
            System.out.println("end states "+System.currentTimeMillis());

        assert initialState >= 0;

        // 0'572
        if (DEBUG)
            System.out.println("start goto "+System.currentTimeMillis());
        buildGotoTable();
        if (DEBUG)
            System.out.println("end goto "+System.currentTimeMillis());

        // 0'023
        if (DEBUG)
            System.out.println("start first "+System.currentTimeMillis());
        computeFirst();
        if (DEBUG)
            System.out.println("end first "+System.currentTimeMillis());

        // 2'100
        if (DEBUG)
            System.out.println("start follow "+System.currentTimeMillis());
        computeFollow();
        if (DEBUG)
            System.out.println("end follow "+System.currentTimeMillis());

        // 0'098
        if (DEBUG)
            System.out.println("start action "+System.currentTimeMillis());
        buildActionTable();
        if (DEBUG)
            System.out.println("end action "+System.currentTimeMillis());

        if (DEBUG)
        {
            // debugPrintStates();
            debugPrintGotoTable();
            debugPrintActionTable();
        }

        // free unused data structures
        first=null;
        follow=null;
        stateMap=null;
        states=null;
        grammar=null;
    }

    public int goTo(int state, NonTerminal n)
    {
        Map<NonTerminal,Integer> row;
        Integer destState;

        assert gotoTable != null;
        assert 0 <= state && state < gotoTable.size();

        row=gotoTable.get(state);

        destState=row.get(n);

        return destState == null ? -1 : destState;
    }

    public Collection<ParsingAction> actions(int state, Terminal t)
    {
        Map<Terminal,Collection<ParsingAction>> row;
        Collection<ParsingAction> actions;

        assert actionTable != null;
        assert 0 <= state && state < gotoTable.size();

        row=actionTable.get(state);

        actions=row.get(t);

        return actions;
    }

    public int getInitialState()
    {
        return initialState;
    }

    public int numberOfStates()
    {
        return actionTable.size();
    }
}
