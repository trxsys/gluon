package test.simple.fieldargs;

import test.common.Atomic;
import test.common.Contract;

@Contract(clauses="a(X) b(X);")
class Module
{
    public Module() { }
    public void a(String bar) { }
    public void b(String foo) { }
}

public class Main
{
    private Module m;
    private String v="42";

    public void test()
    {
        m=new Module();

        m.a(v);
        m.b(v);

        System.out.println(v);
    }

    public static void main(String[] args)
    {
        new Main().test();
    }
}
