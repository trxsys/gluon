package test.basicAbcNonAtomic;

@interface Atomic
{
}

class Module
{
    public Module() {}
    
    public void a() {}
    public void b() {}
    public void c() {}
}

public class Main
{
    private static Module m;
    
    @Atomic
    private static void f()
    {
        m.a();
        m.b();
    }
    
    @Atomic
    private static void g()
    {
        m.c();
    }
    
    public static void main(String[] args)
    {
        m=new Module();
        f();
        g();
    }
}
