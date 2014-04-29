package test.simple.grammaropt;

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
    private static Module m;

    public static void main(String[] args)
    {
        m=new Module();

        for (int i=0; i < 10; i++)
            if (i%2 == 0)
            {
                if (i%3 == 0)
                    m.a();
                else
                    System.out.println("bar");
            }
            else
                System.out.println("foo");
    }
}
