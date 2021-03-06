Gluon
=====

Gluon statically verifies the atomicity of the execution of sequences of calls
to methods of a class.

The following code exemplifies how should this tool be used. This and can be
found in ```test/simple/example```.

```java
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
    
    private static void f()
    {
        m.c();
    }
    
    @Atomic
    private static void g()
    {
        m.a();
        m.b();
        f();
    }
    
    public static void main(String[] args)
    {
        m=new Module();
        
        for (int i=0; i < 10; i++)
            if (i%2 == 0)
                m.a();
            else
                m.b();
        
        f();
        g();
    }
}
```

In this case we have two traces that can call ```a() b() c()```: one calls
```a()``` and ```b()``` in the ```for``` loop and ```c()``` in method ```f()```;
and one calls this sequence of methods in ```g()```. Only the latter guarantees the
atomicity of execution of this sequence of calls.

We also check that the calls ```c() c()``` are never performed.

The output of this test is

```
Checking thread Main.main():

  Verifying word a b c:

      Method: Main.g()
      Calls Location: Main.java:28 Main.java:29 Main.java:22
      Atomic: YES

      Method: Main.main()
      Calls Location: Main.java:39 Main.java:41 Main.java:22
      Atomic: NO

  Verifying word c c:

    No occurrences

```

Compiling
=========

To compile gluon you need to run

```shell
sbt compile
```

Compiling the tests is equally easy:

```shell
sbt compileTests
```

Running the Example Test
========================

After the compilation gluon and the tests the example test can be ran with

```shell
./test.sh example
```

This example can be found in `test/simple/example`. You are encouraged to
modify and play with the example.

Running the Validation Tests
============================

After the compilation a set of validation tests can be run with

```shell
cd test/validation
./run.sh
```

The results are saved in each of the test directories, in a file
named ```result```.
