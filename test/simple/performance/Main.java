package test.simple.performance;

import test.common.Atomic;
import test.common.Contract;

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
    private static int i=0;

    private static void f()
    {
        if (i == 8)
            return;

        m.c();

        while (i%12 == 34)
            if (i == 10)
                System.out.println(i++);
            else
                System.out.println(i);

        if (i == 10)
            f();
        m.c();
        if (i == 10)
            System.out.println(i);
        else
            System.out.println(i);
        if (i == 10)
            m.c();
        m.c();

        while (i%12 == 34)
            if (i == 10)
                System.out.println(i++);
            else
                System.out.println(i);

        if (i == 11)
            m.c();
        if (i == 10)
            System.out.println(i);
        else
            System.out.println(i);
        while (i < 10)
            m.c();
        if (i == 10)
            System.out.println(i);
        else
            System.out.println(i);
        if (i == 10)
            m.c();
        m.c();
        while (i < 10)
        {
            m.c();
            f();
        }
        if (i == 13)
        m.c();
    }

    @Atomic
    private static void g()
    {
        m.a();

        while (i%12 == 34)
            if (i == 10)
                System.out.println(i++);
            else
                System.out.println(i);

        m.b();

        while (i < 1000)
            if (i == 1)
                m.c();
            else
                f();
        if (i == 10)
            System.out.println(i);
        else
            System.out.println(i);
        while (i < 1000)
            if (i == 1)
                m.c();
            else
                f();
        if (i == 10)
            System.out.println(i);
        else
            System.out.println(i);
        while (i < 1000)
            if (i == 1)
                m.c();
            else
            {
        if (i == 10)
            System.out.println(i);
        else
            System.out.println(i);
                f();
            }
        while (i < 1000)
            if (i == 1)
                m.c();
            else
                f();

        while (i < 1000)
            if (i == 1)
                m.c();
            else
                f();

        while (i%12 == 34)
            if (i == 10)
                System.out.println(i++);
            else
                System.out.println(i);

        while (i < 1000)
            if (i == 1)
                m.c();
            else
                f();
        if (i == 10)
            System.out.println(i);
        else
            System.out.println(i);
        while (i < 1000)
            if (i == 1)
                m.c();
            else
                f();
        if (i == 10)
            System.out.println(i);
        else
            System.out.println(i);
        while (i < 1000)
            if (i == 1)
                m.c();
            else
                f();
    }

    public static void main(String[] args)
    {
        m=new Module();

        for (int i=0; i < 10; i++)
            if (i%2 == 0)
            {
        if (i == 10)
            System.out.println(i);
        else
            System.out.println(i);
                m.a();
                i++;
            }
            else
                m.b();

        while (i%12 == 34)
            if (i == 10)
                System.out.println(i++);
            else
                System.out.println(i);

        if (i == 10)
            System.out.println(i);
        else
            System.out.println(i);

        while (i%12 == 34)
            if (i == 10)
                System.out.println(i++);
            else
                System.out.println(i);

        if (i >50)
            i++;
        else
            i--;

        f();
        g();

        while (i%12 == 34)
            if (i == 10)
                System.out.println(i++);
            else
                System.out.println(i);

        while (i < 1000)
        {
        m.a();
        if (i == 10)
            m.b();
        else
        {
            m.c();
            if (i >50)
                i++;
            else
                i--;
            m.c();
        }
        m.c();
        }


        for (int i=0; i < 10; i++)
            if (i%2 == 0)
            {
                m.a();
        if (i == 10)
            System.out.println(i);
        else
            System.out.println(i);
                i++;
            }
            else
                m.b();

        while (i%12 == 34)
            if (i == 10)
                System.out.println(i++);
            else
                System.out.println(i);

            if (i >50)
                i++;
            else
                i--;


        if (i%2 == 0)
        while (i < 1000)
        {
        m.a();
        if (i == 10)
            m.b();
        else
        {
            m.c();
            i++;
            m.c();
        }
        m.c();
        }

        while (i%12 == 34)
            if (i == 10)
                System.out.println(i++);
            else
                System.out.println(i);


        for (int i=0; i < 10; i++)
            if (i%2 == 0)
            {
                m.a();
        if (i == 10)
            System.out.println(i);
        else
            System.out.println(i);
                i++;
            }
            else
                m.b();

            if (i >50)
                i++;
            else
                i--;

        while (i%12 == 34)
            if (i == 10)
                System.out.println(i++);
            else
                System.out.println(i);


        if (i%2 ==  1)
        while (i < 1000)
        {
        m.a();
        if (i == 10)
            m.b();
        else
        {
            m.c();
            i++;
            m.c();
        }
        m.c();
        }


        for (int i=0; i < 10; i++)
            if (i%2 == 0)
            {
                m.a();
        if (i == 10)
            System.out.println(i);
        else
            System.out.println(i);
                i++;
            }
            else
                m.b();

        while (i%12 == 34)
            if (i == 10)
                System.out.println(i++);
            else
                System.out.println(i);

            if (i >50)
                i++;
            else
                i--;


        if (i%2 == 0)
        while (i < 1000)
        {
        m.a();
        if (i == 10)
        {
            m.b();
            if (i >50)
                i++;
            else
            {
                i--;
                m.c();
            }
        }
        else
        {
            m.c();
            i--;
            m.c();
        }
        m.c();
        }

        while (i%12 == 34)
            if (i == 10)
                System.out.println(i++);
            else
                System.out.println(i);


        for (int i=0; i < 10; i++)
            if (i%2 == 0)
            {
                m.a();
                i++;
            }
            else
                m.b();
        if (i == 10)
            System.out.println(i);
        else
            System.out.println(i);
            if (i >50)
                i++;
            else
                i--;


 if (i%2 == 0)
        while (i < 1000)
        {
        m.a();
        if (i == 10)
        {
            m.b();
            if (i >50)
                i++;
            else
            {
                i--;
                m.c();
            }
        }
        else
        {
            m.c();
            i--;
            m.c();
        }
        m.c();
        }
        if (i == 10)
            System.out.println(i);
        else
            System.out.println(i);

        while (i%12 == 34)
            if (i == 10)
                System.out.println(i++);
            else
                System.out.println(i);

        for (int i=0; i < 10; i++)
            if (i%2 == 0)
            {
                m.a();
                i++;
            }
            else
                m.b();

            if (i >50)
                i++;
            else
                i--;
        if (i == 10)
            System.out.println(i);
        else
            System.out.println(i);

 if (i%2 == 0)
        while (i < 1000)
        {
        m.a();
        if (i == 10)
        {
            m.b();
            if (i >50)
                i++;
            else
            {
                i--;
                m.c();
            }
        }
        else
        {
            m.c();
            i--;
            m.c();
        }
        m.c();
        }


        for (int i=0; i < 10; i++)
            if (i%2 == 0)
            {
                m.a();
                i++;
            }
            else
                m.b();
        if (i == 10)
            System.out.println(i);
        else
            System.out.println(i);
            if (i >50)
                i++;
            else
                i--;


 if (i%26 == 0)
        while (i < 1000)
        {
        m.a();
        if (i == 10)
        {
            m.b();
            if (i >50)
                i++;
            else
            {
                i--;
                m.c();
            }
        }
        else
        {
            m.c();
            i--;
            m.c();
        }
        m.c();
        }


        for (int i=0; i < 10; i++)
            if (i%2 == 0)
            {
        if (i == 10)
            System.out.println(i);
        else
            System.out.println(i);
                m.a();
                i++;
            }
            else
                m.b();

            if (i >50)
                i++;
            else
                i--;


 if (i%22 == 0)
        while (i < 1000)
        {
        m.a();
        if (i == 10)
        {
            m.b();
            if (i >50)
                i++;
            else
            {
                i--;
                m.c();
            }
        }
        else
        {
            m.c();
            i--;
            m.c();
        }
        m.c();
        }


        for (int i=0; i < 10; i++)
            if (i%2 == 0)
            {
                m.a();
                i++;
        if (i == 10)
            System.out.println(i);
        else
            System.out.println(i);
            }
            else
                m.b();

            if (i >50)
                i++;
            else
                i--;


 if (i%20 == 2)
        while (i < 1000)
        {
        m.a();
        if (i == 10)
        {
            m.b();
            if (i >50)
                i++;
            else
            {
                i--;
                m.c();
            }
        }
        else
        {
            m.c();
            i--;
            m.c();
        }
        m.c();
        }
        if (i == 10)
            System.out.println(i);
        else
            System.out.println(i);
        for (int i=0; i < 10; i++)
            if (i%2 == 0)
            {
                m.a();
                i++;
            }
            else
                m.b();

            if (i >50)
                i++;
            else
                i--;

        if (i == 10)
            System.out.println(i);
        else
            System.out.println(i);

 if (i%26 == 0)
        while (i < 1000)
        {
        m.a();
        if (i == 10)
        {
            m.b();
            if (i >50)
                i++;
            else
            {
                i--;
                m.c();
            }
        }
        else
        {
            m.c();
            i--;
            m.c();
        }
        m.c();
        }


        for (int i=0; i < 10; i++)
            if (i%2 == 0)
            {
                m.a();
                i++;
            }
            else
                m.b();

        if (i == 10)
            System.out.println(i);
        else
            System.out.println(i);

            if (i >50)
                i++;
            else
                i--;


 if (i%22 == 0)
        while (i < 1000)
        {
        m.a();
        if (i == 10)
        {
            m.b();
            if (i >50)
                i++;
            else
            {
                i--;
                m.c();
            }
        }
        else
        {
            m.c();
            i--;
            m.c();
        }
        m.c();
        }

        if (i == 10)
            System.out.println(i);
        else
            System.out.println(i);

        for (int i=0; i < 10; i++)
            if (i%2 == 0)
            {
                m.a();
                i++;
            }
            else
                m.b();

            if (i >50)
                i++;
            else
                i--;


 if (i%26 == 0)
        while (i < 1000)
        {
        m.a();
        if (i == 10)
        {
            m.b();
            if (i >50)
                i++;
            else
            {
                i--;
                m.c();
            }
        }
        else
        {
            m.c();
            i--;
            m.c();
        }
        m.c();
        }


        for (int i=0; i < 10; i++)
            if (i%2 == 0)
            {
                m.a();
                i++;
            }
            else
                m.b();

        if (i == 10)
            System.out.println(i);
        else
            System.out.println(i);

            if (i >50)
                i++;
            else
                i--;


 if (i%22 == 0)
        while (i < 1000)
        {
        m.a();
        if (i == 10)
        {
            m.b();
            if (i >50)
                i++;
            else
            {
                i--;
                m.c();
            }
        }
        else
        {
            m.c();
            i--;
            m.c();
        }
        m.c();
        }


        for (int i=0; i < 10; i++)
            if (i%2 == 0)
            {
                m.a();
                i++;
            }
            else
                m.b();

            if (i >50)
                i++;
            else
                i--;
        if (i == 10)
            System.out.println(i);
        else
            System.out.println(i);
        for (int i=0; i < 10; i++)
            if (i%2 == 0)
            {
                m.a();
                i++;
        if (i == 10)
            System.out.println(i);
        else
            System.out.println(i);
            }
            else
                m.b();

            if (i >50)
                i++;
            else
                i--;


 if (i%26 == 0)
        while (i < 1000)
        {
        m.a();
        if (i == 10)
        {
            m.b();
            if (i >50)
                i++;
            else
            {
                i--;
                m.c();
            }
        }
        else
        {
            m.c();
            i--;
            m.c();
        }
        m.c();
        }


 if (i%26 == 0)
        while (i < 1000)
        {
        System.out.println(i);
        if (i == 10)
        {
        System.out.println(i);
            if (i >50)
                i++;
            else
            {
                i--;
        System.out.println(i);
            }
        }
        else
        {
        System.out.println(i);
            i--;
        System.out.println(i);
        }
        System.out.println(i);
        }



        for (int i=0; i < 10; i++)
            if (i%2 == 0)
            {
                m.a();
                i++;
            }
            else
                m.b();

        if (i == 10)
            System.out.println(i);
        else
            System.out.println(i);
            if (i >50)
                i++;
            else
                i--;


 if (i%22 == 0)
        while (i < 1000)
        {
        m.a();
        if (i == 10)
        {
            m.b();
            if (i >50)
                i++;
            else
            {
                i--;
                m.c();
            }
        }
        else
        {
            m.c();
            i--;
        if (i == 10)
            System.out.println(i);
        else
            System.out.println(i);
            m.c();
        }
        m.c();
        }


        for (int i=0; i < 10; i++)
            if (i%2 == 0)
            {
                m.a();
                i++;
            }
            else
                m.b();

            if (i >50)
                i++;
            else
                i--;

        if (i == 10)
            System.out.println(i);
        else
            System.out.println(i);

        while (i%12 == 34)
            if (i == 10)
                System.out.println(i);
            else
                System.out.println(i);


 if (i%26 == 0)
        while (i < 1000)
        {
        System.out.println(i);
        if (i == 10)
        {
        System.out.println(i);
            if (i >50)
                i++;
            else
            {
                i--;
        System.out.println(i);
            }
        }
        else
        {
        System.out.println(i);
            i--;
        System.out.println(i);
        }
        System.out.println(i);
        }


 if (i%20 == 2)
        while (i < 1000)
        {
        m.a();
        if (i == 10)
        {
            m.b();
            if (i >50)
                i++;
            else
            {


        while (i%12 == 34)
            if (i == 10)
                System.out.println(i);
            else
                System.out.println(i);
                i--;
                m.c();
            }

        while (i%12 == 34)
            if (i == 10)
                System.out.println(i);
            else
                System.out.println(i);

        }
        else
        {
            m.c();
            i--;
            m.c();
        }
        m.c();
        }

 if (i%26 == 0)
        while (i < 1000)
        {
        System.out.println(i);
        if (i == 10)
        {
        System.out.println(i);
            if (i >50)
                i++;
            else
            {
                i--;
        System.out.println(i);
            }
        }
        else
        {
        System.out.println(i);
            i--;
        System.out.println(i);
        }
        System.out.println(i);
        }



 if (i%26 == 0)
        while (i < 1000)
        {
        System.out.println(i);
        if (i == 10)
        {
        System.out.println(i);
            if (i >50)
                i++;
            else
            {
                i--;
        System.out.println(i);
            }
        }
        else
        {
        System.out.println(i);
            i--;
        System.out.println(i);
        }
        System.out.println(i);
        }



 if (i%26 == 0)
        while (i < 1000)
        {
        System.out.println(i);
        if (i == 10)
        {
        System.out.println(i);
            if (i >50)
                i++;
            else
            {
                i--;
        System.out.println(i);
            }
        }
        else
        {
        System.out.println(i);
            i--;
        System.out.println(i);
        }
        System.out.println(i);
        }


        while (i%12 == 34)
            if (i == 10)
                System.out.println(i);
            else
                System.out.println(i);



 if (i%26 == 0)
        while (i < 1000)
        {
        System.out.println(i);
        if (i == 10)
        {
        System.out.println(i);
            if (i >50)
                i++;
            else
            {
                i--;
        System.out.println(i);
            }
        }
        else
        {
        System.out.println(i);
            i--;
        System.out.println(i);
        }
        System.out.println(i);
        }



 if (i%26 == 0)
        while (i < 1000)
        {
        System.out.println(i);
        if (i == 10)
        {
        System.out.println(i);
            if (i >50)
                i++;

        while (i%12 == 34)
            if (i == 10)
                System.out.println(i);
            else
                System.out.println(i);

            {
                i--;
        System.out.println(i);
            }
        }
        else
        {
        System.out.println(i);
            i--;
        System.out.println(i);
        }
        System.out.println(i);
        }
    }
}
