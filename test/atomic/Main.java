package test.atomic;

@interface Atomic
{
}

class Module
{
    public Module() {}

    public void a() {}
    public void b() {}
}

public class Main
{
    private static Module m;

    @Atomic
    private static void f()
    {
	if (Math.random() == 23.0)
	    return;
	m.a();
	f();
	m.b();
    }

    public static void main(String[] args)
    {
	m=new Module();
	f();
    }
}

