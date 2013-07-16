package test.simple.recursion;

public class Main
{
    public static Module m;

    public static void main(String[] args)
    { 
        m=new Module();

        m.a();
        C.foo(m);
        m.b();
        main(new String[0]);
        m.c();
    }
}
