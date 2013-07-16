package test.simple.recursion;

public class C
{
    public static void foo(Module m)
    {
        m.d();
        foo(m);
        m.e();
        foo(m);
        m.f();
        Main.main(new String[0]);
        m.g();
    }
}
