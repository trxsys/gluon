package x.analysis.atomicMethods;

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

    private static final boolean DEBUG=false;
   
    private final CallGraph callGraph;
    private final Collection<SootMethod> threads;

    private final Map<SootMethod,Boolean> methodsAtomicity;

    public AtomicMethods(CallGraph cg, Collection<SootMethod> threads)
    {
        this.callGraph=cg;
        this.threads=threads;

        this.methodsAtomicity=new HashMap<SootMethod,Boolean>(2*callGraph.size());
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

    private static boolean isAtomicMethod(SootMethod method)
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
                    || (!x.Main.WITH_JAVA_LIB && m.isJavaLibraryMethod()))
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

    public boolean isAtomic(SootMethod m)
    {
        Boolean b=methodsAtomicity.get(m);

        return b == null ? false : (boolean)b;
    }
}
