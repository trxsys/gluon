package test.simple.exampleargs;

import test.common.Atomic;
import test.common.Contract;

@Contract(clauses="a b c(X,X);"
                 +"a b c(X,Y);"
                 +"c c;"
                 +"c(X,Y) c(K,_);")
class Module
{
    public Module() { }
    public void a() { }
    public void b() { }
    public void c(int x, int y) { }
}

public class Main
{
    private static Module m;
    
    private static void f()
    {
        m.c(0,1);
    }
    
    @Atomic
    private static void g()
    {
        m.a();
        m.b();
        f();
    }
    
    public static void main(String[] args)
    {
        m=new Module();
        
        for (int i=0; i < 10; i++)
            if (i%2 == 0)
                m.a();
            else
                m.b();
        
        f();
        g();
    }
}
