package test.simple.exampleargs;

import test.common.Atomic;
import test.common.Contract;

@Contract(clauses="X=d e(X);"
                 +"a b c(X,X);"
                 +"a b c(X,Y);"
                 +"c c;"
                 +"c(X,Y) c(K,_);")
class Module
{
    public Module() { }
    public void a() { }
    public void b() { }
    public void c(int x, int y) { }
    public int d() { return 1; }
    public void e(int foo) { }
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

    private static void h()
    {
        int v=0;

        v=m.d();
        m.e(42);

        /* otherwise v is removed by the compiler for not being used */
        System.out.println(v);
    }
    
    public static void main(String[] args)
    {
        int v;

        m=new Module();
        
        for (int i=0; i < 10; i++)
            if (i%2 == 0)
                m.a();
            else
                m.b();
        
        f();
        g();

        v=m.d();
        m.e(v);

        h();
    }
}
