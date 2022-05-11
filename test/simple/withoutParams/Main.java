package test.simple.withoutParams;

import test.common.Contract;

@Contract(clauses="indexOf remove;")
class Module
{
    public Module() {}

    public int indexOf() { return 0; }
    public void remove(int i) {}
}

public class Main {
    private static Module m;

    public static void main(String[] args) {
        m = new Module();

        int j = 2;
        int i = m.indexOf();
        m.remove(j);
    }
}