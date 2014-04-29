package test.simple.recursion2;

import test.common.Contract;

@Contract(clauses="a a b c;"
                 +"b c;"
                 +"b c c")
class Module
{
    public Module() { }
    public void a() { }
    public void b() { }
    public void c() { }
}

public class Main
{
    private static Module m;

    private static void f(int c)
    {
        if (c%2 == 0)
            f(c-1);
        else if (c%4 == 0)
        {
            while (c%3 == 0)
            {
                m.a();
                c++;
                f(c-1);
            }
        }
        else if (c%5 == 0)
            m.c();
        else if (c%6 == 0)
        {
            m.b();
            f(c+1);
        }
    }

    public static void main(String[] args)
    {
        m=new Module();
        f(42);
    }
}
