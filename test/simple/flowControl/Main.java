package test.simple.flowControl;

public class Main
{
    public static Module m;

    public static boolean cond()
    {
        m.c();
        return m == new Module();
    }

    public static void main(String[] args)
    { 
        m=new Module();

        if (cond())
            m.a();
        else
            m.b();

        while (cond())
            m.d();

        do
        {
            m.e();
        } while (cond());
    }
}
