package test.simple.exampleargssimple;

import test.common.Contract;

@Contract(clauses="V=get(_) set(_,V);"
                 +"get(K) set(K,_);")
class Module
{
    public Module() { }
    public int get(int k) { return 0; }
    public void set(int k, int v) { }
}

public class Main
{
    private static Module m;

    public static void main(String[] args)
    {
        int a = 1;
        int b = 2;

        m = new Module();

        // Violates `V=get(_) set(_,V)`
        int v = m.get(a);
        m.set(b, v);

        // Violates `get(K) set(K,_)`:
        int u = m.get(a);
        m.set(a, 2 * u);

        // Does not violate any clause:
        int t = m.get(a);
        m.set(b, 2 * t);
    }
}
