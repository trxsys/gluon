package test.threads;

class Module
{
    public void modulemethod()
    {
    }
}

class TestThread
    extends Thread
{
    private Module m;

    private void g()
    {
        run();
        m.modulemethod();
    }

    @Override
    public void run()
    {
        m=new Module();
        g();
    }
}

public class Main
{
    public static void f()
    {
        f();
        new TestThread().start();
    }

    public static void main(String[] args)
    { 
        f();
    }
}
