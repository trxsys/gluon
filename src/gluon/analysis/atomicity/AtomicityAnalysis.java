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

package gluon.analysis.atomicity;

import java.util.Collection;
import java.util.Iterator;

import java.util.Set;
import java.util.Map;
import java.util.List;
import java.util.Queue;

import java.util.LinkedList;
import java.util.HashMap;
import java.util.HashSet;

import soot.Kind;
import soot.MethodOrMethodContext;
import soot.SootMethod;
import soot.Unit;

import soot.tagkit.AnnotationTag;
import soot.tagkit.Tag;
import soot.tagkit.VisibilityAnnotationTag;

import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;

import gluon.WordInstance;

import gluon.analysis.programBehavior.PBTerminal;
import gluon.analysis.programBehavior.PBNonTerminal;

import gluon.grammar.Cfg;
import gluon.grammar.NonTerminal;
import gluon.grammar.Production;
import gluon.grammar.Symbol;

class NonTermAtomicity
{
    public final PBNonTerminal nonterm;
    public final boolean reachedAtomically;

    public NonTermAtomicity(PBNonTerminal nont, boolean ra)
    {
        nonterm=nont;
        reachedAtomically=ra;
    }

    @Override
    public int hashCode()
    {
        return nonterm.hashCode()^(reachedAtomically ? 0 : 0x6d24f632);
    }

    @Override
    public boolean equals(Object o)
    {
        NonTermAtomicity other;

        if (!(o instanceof NonTermAtomicity))
            return false;

        other=(NonTermAtomicity)o;

        return reachedAtomically == other.reachedAtomically
            && nonterm.equals(other.nonterm);
    }
}

public class AtomicityAnalysis
{
    private static final String ATOMIC_METHOD_ANNOTATION="Atomic";

    private Cfg grammar;
    private boolean synchMode;

    private final Map<NonTerminal,Boolean> nontermsAtomicity;

    public AtomicityAnalysis(Cfg grammar)
    {
        int nonterms=2*grammar.getNonTerminals().size();

        this.grammar=grammar;
        this.synchMode=false;
        this.nontermsAtomicity=new HashMap<NonTerminal,Boolean>(nonterms);
    }

    public static boolean isAtomicAnnotated(SootMethod method)
    {
        Tag tag=method.getTag("VisibilityAnnotationTag");

        if (tag == null)
            return false;

        VisibilityAnnotationTag visibilityAnnotationTag=(VisibilityAnnotationTag)tag;
        List<AnnotationTag> annotations=visibilityAnnotationTag.getAnnotations();

        for (AnnotationTag annotationTag: annotations)
            if (annotationTag.getType().endsWith("/"+ATOMIC_METHOD_ANNOTATION+";"))
                return true;

        return false;
    }

    public void setSynchMode()
    {
        synchMode=true;
    }

    public void analyze()
    {
        Queue<NonTermAtomicity> queue=new LinkedList<NonTermAtomicity>();
        Set<NonTermAtomicity> enqueued=new HashSet<NonTermAtomicity>();
        NonTermAtomicity start;

        assert !((PBNonTerminal)grammar.getStart()).isAtomic()
            : "The start symbol should be artificial.";

        start=new NonTermAtomicity((PBNonTerminal)grammar.getStart(),false);

        queue.add(start);
        enqueued.add(start);

        while (queue.size() > 0)
        {
            NonTermAtomicity nonterma=queue.poll();
            PBNonTerminal nonterm=nonterma.nonterm;
            boolean reachedAtomically=nonterma.reachedAtomically;

            if (!nontermsAtomicity.containsKey(nonterm))
                nontermsAtomicity.put(nonterm,reachedAtomically);
            else
                nontermsAtomicity.put(nonterm,
                                      nontermsAtomicity.get(nonterm)
                                      && reachedAtomically);

            for (Production p: grammar.getProductionsOf(nonterm))
                for (Symbol s: p.getBody())
                {
                    PBNonTerminal n;
                    NonTermAtomicity succ;

                    if (!(s instanceof PBNonTerminal))
                        continue;

                    n=(PBNonTerminal)s;

                    succ=new NonTermAtomicity(n,reachedAtomically
                                              || n.isAtomic());

                    if (!enqueued.contains(succ))
                    {
                        queue.add(succ);
                        enqueued.add(succ);
                    }
                }
        }
    }

    public boolean isAtomic(WordInstance word)
    {
        assert word.getLCA() != null;
        assert nontermsAtomicity.containsKey(word.getLCA());

        return nontermsAtomicity.get(word.getLCA());
    }
}
