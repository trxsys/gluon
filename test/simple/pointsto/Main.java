package test.simple.pointsto;

import test.common.Atomic;
import test.common.Contract;

@Contract(clauses="a b c;"
                 +"c c;")
class Module
{
    public Module() { }
    public void a() { }
    public void b() { }
    public void c() { }
}

public class Main
{
    private Module m;
    private Module m2=new Module();

    private void f()
    {
        m.c();
    }

    @Atomic
    private void g()
    {
        m.a();
        m.b();
        f();
    }

    private void go()
    {
        m=new Module();

        for (int i=0; i < 10; i++)
            if (i%2 == 0)
                m.a();
            else
            {
                m.b();

                if (i%5 == 0)
                    m=m2;
            }

        f();
        g();
    }

    public static void main(String[] args)
    {
        new Main().go();
    }
}
