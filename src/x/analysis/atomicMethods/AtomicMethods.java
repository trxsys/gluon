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

public class AtomicMethods
{
    private static final String ATOMIC_METHOD_ANNOTATION="Atomic";

    private static final boolean DEBUG=true;
   
    private final CallGraph callGraph;
    private final Collection<SootMethod> threads;

    public AtomicMethods(CallGraph cg, Collection<SootMethod> threads)
    {
        this.callGraph=cg;
        this.threads=threads;
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

    /*
    private void analyzeEdge(Edge edge)
    {
        SootMethod m=edge.tgt();

        if (edge.isThreadRunCall())
        {
            
            dprintln("Found thread entry method: "+m.getSignature());
        }
    }
    */

    private void analyzeReachableMethods(SootMethod entryMethod)
    {
        Queue<SootMethod> methodQueue=new LinkedList<SootMethod>();
        Set<SootMethod> enqueuedMethods=new HashSet<SootMethod>(callGraph.size());
        
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
                    || (!x.Main.WITH_JAVA_LIB && m.isJavaLibraryMethod()))
                    continue;
                
                methodQueue.add(m);
                enqueuedMethods.add(m);

                // analyzeEdge(e);
            }            
        }
    }
    
    public void analyze()
    {
        for (SootMethod th: threads)
            analyzeReachableMethods(th);
    }
}
