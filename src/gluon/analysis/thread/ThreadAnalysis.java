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

package gluon.analysis.thread;

import java.util.Collection;
import java.util.Iterator;

import java.util.LinkedList;
import java.util.Set;
import java.util.HashSet;
import java.util.Queue;

import soot.Kind;
import soot.MethodOrMethodContext;
import soot.SootMethod;

import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;

public class ThreadAnalysis
{
    private static final boolean DEBUG=false;

    private final CallGraph callGraph;

    private Collection<SootMethod> threadsEntryMethod;

    public ThreadAnalysis(CallGraph cg)
    {
        callGraph=cg;
        threadsEntryMethod=new LinkedList<SootMethod>();
    }

    private void dprintln(String s)
    {
        if (DEBUG)
            System.out.println(this.getClass().getSimpleName()+": "+s);
    }

    private void analyzeEdge(Edge edge)
    {
        SootMethod m=edge.tgt();

        if (edge.isThreadRunCall())
        {
            threadsEntryMethod.add(m);
            dprintln("Found thread entry method: "+m.getSignature());
        }
    }

    private void analyzeReachableMethods(SootMethod entryMethod)
    {
        Queue<SootMethod> methodQueue=new LinkedList<SootMethod>();
        Set<SootMethod> enqueuedMethods=new HashSet<SootMethod>(2*callGraph.size());

        methodQueue.add(entryMethod);
        enqueuedMethods.add(entryMethod);

        while (methodQueue.size() > 0)
        {
            SootMethod method=methodQueue.poll();

            for (Iterator<Edge> it=callGraph.edgesOutOf(method);
                 it.hasNext(); )
            {
                Edge e=it.next();
                SootMethod m=e.tgt();

                assert m != null;

                if (enqueuedMethods.contains(m)
                    || (!gluon.Main.WITH_JAVA_LIB && m.isJavaLibraryMethod()))
                    continue;

                methodQueue.add(m);
                enqueuedMethods.add(m);

                analyzeEdge(e);
            }
        }
    }

    public void analyze()
    {
        for (Iterator<MethodOrMethodContext> it=callGraph.sourceMethods();
             it.hasNext(); )
        {
            MethodOrMethodContext mc=it.next();
            SootMethod m;

            if (!(mc instanceof SootMethod))
                continue;

            m=(SootMethod)mc;

            if (m.isMain())
            {
                dprintln("Found main: "+m.getName());

                threadsEntryMethod.add(m);
                analyzeReachableMethods(m);
            }
        }
    }

    public Collection<SootMethod> getThreadsEntryMethod()
    {
        return threadsEntryMethod;
    }
}
