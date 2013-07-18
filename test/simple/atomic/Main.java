package test.simple.atomic;

@interface Atomic
{
}

class Module
{
    public Module() {}
    
    public void a() {}
    public void b() {}
}

public class Main
{
    private static Module m;

    private static void na()
    {
    }

    private static void a1()
    {
        na();        
    }

    private static void a0()
    {
        a1();
    }
    
    @Atomic
    private static void f()
    {
        if (Math.random() == 23.0)
            return;
        m.a();
        f();
        a0();
        m.b();
    }
    
    public static void main(String[] args)
    {
        m=new Module();
        f();
        na();
    }
}

