package test.simple.fieldargs;

import test.common.Atomic;
import test.common.Contract;

@Contract(clauses="a(X) b(X);"
                 +"ai(X) bi(X)")
class Module
{
    public Module() { }
    public void a(String bar) { }
    public void b(String foo) { }
    public void ai(int bar) { }
    public void bi(int foo) { }
}

public class Main
{
    private Module m;
    private String v="42";
    private int vi=42;

    public void test()
    {
        m=new Module();

        m.a(v);
        m.b(v);

        System.out.println(v);

        test2();
    }

    public void test2()
    {
        m=new Module();

        m.ai(vi);
        m.bi(vi);

        System.out.println(vi);
    }

    public static void main(String[] args)
    {
        new Main().test();
    }
}
