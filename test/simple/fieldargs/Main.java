package test.simple.fieldargs;

import test.common.Atomic;
import test.common.Contract;

@Contract(clauses="a(X) b(X);"
                 +"ai(X) bi(X);"
                 +"X=ai bi(X)")
class Module
{
    public Module m;
    public int foo=3;
    public Module() { }
    public void a(String bar) { }
    public void b(String foo) { }
    public int ai(int bar) { return 4; }
    public void bi(int foo) { }
}

public class Main
{
    private Module m;
    private String v="42";
    private int vi=42;
    private int[] vvi={1,7};

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

        test3();
    }

    public void test3()
    {
        m.ai(vvi[0]);
        m.bi(vvi[0]);

        System.out.println(vi);

        test4();
    }

    public void test4()
    {
        m.ai(m.m.foo);
        m.bi(m.m.foo);

        System.out.println(vi);

        test5();
    }

    public void test5()
    {
        int[] vec={1,7};

        m.ai(vec[0]);
        m.bi(vec[0]);

        test6();
    }

    public void test6()
    {
        int[][] vec={{1},{7}};

        m.ai(vec[0][0]);
        m.bi(vec[0][0]);

        test7(10);
    }

    public void test7(int a)
    {
        vvi[a]=m.ai(8);
        if (vvi[a] > 0)
            m.bi(vvi[a]);
    }

    public static void main(String[] args)
    {
        new Main().test();
    }
}
