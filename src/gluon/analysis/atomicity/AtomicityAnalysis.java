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

import soot.tagkit.AnnotationTag;
import soot.tagkit.Tag;
import soot.tagkit.VisibilityAnnotationTag;

import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;

import gluon.analysis.programBehavior.PPNonTerminal;

class MethodQueue
{
    public final SootMethod method;
    public final boolean reachedAtomically;

    public MethodQueue(SootMethod m, boolean ra)
    {
        method=m;
        reachedAtomically=ra;
    }

    @Override
    public int hashCode()
    {
        return method.hashCode()^(reachedAtomically ? 0x04f667a1 : 0xecc363c6);
    }

    @Override
    public boolean equals(Object o)
    {
        MethodQueue other;

        if (!(o instanceof MethodQueue))
            return false;

        other=(MethodQueue)o;

        return reachedAtomically == other.reachedAtomically
            && method.equals(other.method);
    }
}

public class AtomicityAnalysis
{
    private static final String ATOMIC_METHOD_ANNOTATION="Atomic";

    private static final boolean DEBUG=false;
   
    private final CallGraph callGraph;
    private final Collection<SootMethod> threads;

    private final Map<SootMethod,Boolean> methodsAtomicity;

    private boolean synchMode;

    public AtomicityAnalysis(CallGraph cg, Collection<SootMethod> threads)
    {
        this.callGraph=cg;
        this.threads=threads;

        this.methodsAtomicity=new HashMap<SootMethod,Boolean>(2*callGraph.size());

        synchMode=false;
    }
    
    private void dprintln(String s)
    {
        if (DEBUG)
            System.out.println(this.getClass().getSimpleName()+": "+s);
    }

    private void debugPrintMethods()
    {
        System.out.println("Atomicity of executed methods:");

        for (Map.Entry<SootMethod,Boolean> m: methodsAtomicity.entrySet())
            System.out.println("  "+(m.getValue() ? "A" : "N")
                               +" "+m.getKey().getSignature());

        System.out.println();
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

    private boolean isAtomicMethod(SootMethod method)
    {
        return synchMode ? method.isSynchronized() : isAtomicAnnotated(method);
    }

    private void analyzeReachableMethods(SootMethod entryMethod)
    {
        Queue<MethodQueue> methodQueue=new LinkedList<MethodQueue>();
        Set<MethodQueue> enqueuedMethods
            =new HashSet<MethodQueue>(2*callGraph.size());
        MethodQueue entryMethodQueue
            =new MethodQueue(entryMethod,isAtomicMethod(entryMethod));

        methodQueue.add(entryMethodQueue);
        enqueuedMethods.add(entryMethodQueue);
        
        while (methodQueue.size() > 0)
        {
            MethodQueue mq=methodQueue.poll();
            SootMethod method=mq.method;
            boolean reachedAtomically=mq.reachedAtomically;

            if (!methodsAtomicity.containsKey(method))
                methodsAtomicity.put(method,reachedAtomically);
            else
                methodsAtomicity.put(method,methodsAtomicity.get(method) 
                                            && reachedAtomically);

            for (Iterator<Edge> it=callGraph.edgesOutOf(method); 
                 it.hasNext(); )
            {
                Edge e=it.next();
                SootMethod m=e.tgt();
                MethodQueue succmq=new MethodQueue(m,reachedAtomically
                                                   || isAtomicMethod(m));

                assert m != null;
                
                if (enqueuedMethods.contains(succmq)
                    || (!gluon.Main.WITH_JAVA_LIB && m.isJavaLibraryMethod()))
                    continue;
                
                if (e.isThreadRunCall())
                    continue;
                
                methodQueue.add(succmq);
                
                enqueuedMethods.add(succmq);
            }            
        }
    }
    
    public void analyze()
    {
        for (SootMethod th: threads)
            analyzeReachableMethods(th);

        if (DEBUG)
            debugPrintMethods();
    }

    public boolean isAtomic(PPNonTerminal nonterm)
    {
        Boolean b=methodsAtomicity.get(nonterm.getMethod());

        return b == null ? false : (boolean)b;
    }
}
