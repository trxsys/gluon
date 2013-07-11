package test.basicAbcNonAtomic;

@interface Atomic
{
}

@interface Contract
{
    String clauses();
}

@Contract(clauses ="a b c;"
                  +"l l l;"
                  +"i1 i2;")
class Module
{
    public Module() {}
    
    public void a() {}
    public void b() {}
    public void c() {}

    public void l() {}

    public void i1() {}
    public void i2() {}
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
    
    private static void q()
    {
        m.a();
        m.b();
        g();
    }

    @Atomic
    private static void k()
    {
        q();
    }

    @Atomic
    private static void foo()
    {
            m.i1();
            m.i2();
    }
    
    public static void main(String[] args)
    {
        m=new Module();

        f();
        g();
        k();

        m.a();

        while (Math.random() < 0.5)
            m.l();

        m.a();

        if (Math.random() < 0.5)
        {
            m.i1();
            m.i2();
        }
        else
        {
            foo();
        }
    }
}
