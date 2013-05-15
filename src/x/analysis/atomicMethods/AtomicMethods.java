package x.analysis.atomicMethods;

import java.util.Collection;
import java.util.Iterator;

import java.util.LinkedList;
import java.util.Set;
import java.util.List;
import java.util.HashSet;
import java.util.Queue;

import soot.Kind;
import soot.MethodOrMethodContext;
import soot.SootMethod;

import soot.tagkit.AnnotationTag;
import soot.tagkit.Tag;
import soot.tagkit.VisibilityAnnotationTag;

import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;

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

public class AtomicMethods
{
    private static final String ATOMIC_METHOD_ANNOTATION="Atomic";

    private static final boolean DEBUG=true;
   
    private final CallGraph callGraph;
    private final Collection<SootMethod> threads;

    private final Set<SootMethod> atomicMethods;

    public AtomicMethods(CallGraph cg, Collection<SootMethod> threads)
    {
        this.callGraph=cg;
        this.threads=threads;

        this.atomicMethods=new HashSet<SootMethod>(2*callGraph.size());
    }
    
    private void dprintln(String s)
    {
        if (DEBUG)
            System.out.println(this.getClass().getSimpleName()+": "+s);
    }

    private static boolean isAtomicMethod(SootMethod method)
    {
        Tag tag = method.getTag("VisibilityAnnotationTag");
        
        if (tag == null)
            return false;
        
        VisibilityAnnotationTag visibilityAnnotationTag = (VisibilityAnnotationTag) tag;
        List<AnnotationTag> annotations = visibilityAnnotationTag.getAnnotations();
        
        for (AnnotationTag annotationTag : annotations) 
            if (annotationTag.getType().endsWith("/"+ATOMIC_METHOD_ANNOTATION+";"))
                return true;
        
        return false;
    }

    private void analyzeReachableMethods(SootMethod entryMethod)
    {
        Queue<MethodQueue> methodQueue=new LinkedList<MethodQueue>();
        Set<SootMethod> enqueuedMethods=new HashSet<SootMethod>(2*callGraph.size());
        
        methodQueue.add(new MethodQueue(entryMethod,isAtomicMethod(entryMethod)));
        // enqueuedMethods.add(entryMethod);
        
        atomicMethods.add(entryMethod);

        while (methodQueue.size() > 0)
        {
            MethodQueue mq=methodQueue.poll();
            SootMethod method=mq.method;
            boolean reachedAtomically=mq.reachedAtomically;

            if (!reachedAtomically)
                atomicMethods.remove(entryMethod);

            for (Iterator<Edge> it=callGraph.edgesOutOf(method); 
                 it.hasNext(); )
            {
                Edge e=it.next();
                SootMethod m=e.tgt();

                assert m != null;
                
                if (true // enqueuedMethods.contains(m)
                    || (!x.Main.WITH_JAVA_LIB && m.isJavaLibraryMethod()))
                    continue;
                
                // methodQueue.add(m);
                atomicMethods.add(m);
                
                // enqueuedMethods.add(m);
            }            
        }
    }
    
    public void analyze()
    {
        for (SootMethod th: threads)
            analyzeReachableMethods(th);
    }
}
