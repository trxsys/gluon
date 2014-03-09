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

import gluon.analysis.programBehavior.PPTerminal;
import gluon.analysis.programBehavior.PPNonTerminal;

import gluon.parsing.parseTree.ParseTree;
import gluon.parsing.parseTree.ParseTreeNode;

public class AtomicityAnalysis
{
    private static final String ATOMIC_METHOD_ANNOTATION="Atomic";

    private boolean synchMode;

    public AtomicityAnalysis()
    {
        synchMode=false;
    }

    public void setSynchMode()
    {
        synchMode=true;
    }

    private static boolean isAtomicAnnotated(SootMethod method)
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

    public boolean isAtomic(WordInstance word)
    {
        ParseTree tree=word.getParseTree();

        assert word.getLCA().equals(tree.getLCA().getElem());

        /* Climb the tree from the LCA to the root.
         * If some of these nodes is atomically executed then this is atomic.
         */
        for (ParseTreeNode n=tree.getLCA(); n != null; n=n.getParent())
        {
            PPNonTerminal nonterm;
                
            assert n.getElem() instanceof PPNonTerminal;
            
            nonterm=(PPNonTerminal)n.getElem();

            if (!synchMode)
            {
                if (isAtomicAnnotated(nonterm.getMethod()))
                    return true;
            }
            else
            {
                if (nonterm.isSynchBlock()
                    || nonterm.getMethod().isSynchronized())
                    return true;
            }
        }

        return false;
    }
}
