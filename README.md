x
=

This tool verifies the atomicity execution of sequences of method calls performed
to a module (represented by a class).

The following code exemplifies this and can be found in ```test/simple/example```.

```java
@Contract(clauses="a b c;")
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
    
    public static void f()
    {
        m.c();
    }
    
    @Atomic
    public static void g()
    {
        m.a();
        m.b();
        f();
    }
    
    public static void main(String[] args)
    {
        m=new Module();
        
        for (int i=0; i < 0; i++)
            if (i%2 == 0)
                m.a();
            else
                m.b();
        
        f();
        g();
    }
}
```

Compiling
=========

To compile this tool just run

```shell
ant
```

Compiling the tests is equally simple:

```shell
ant tests
```

Running the Example Program
===========================

After the compilation you just have to run

```shell
./test.sh example
```

to analyze the example program. This example can be found in ```test/simple/example```.
You are encouraged to modify it and play with this tool.

Running the Validation Tests
============================

After the compilation you just have to run

```shell
cd test/validation
./run.sh
```

A batch of tests will be executed. The results are saved in each of the test
directories, in a file named ```result```.
