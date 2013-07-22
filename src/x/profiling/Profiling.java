package x.profiling;

import java.util.Map;
import java.util.Set;

import java.util.HashMap;

public class Profiling
{
    private static Map<String,Integer> values;

    static {
        values=new HashMap<String,Integer>();
    }

    private Profiling()
    {
    }

    public static void set(String id, int v)
    {
        values.put(id,v);
    }

    public static int get(String id)
    {
        Integer v=values.get(id);

        assert v != null;

        return v;
    }

    public static void inc(String id)
    {
        inc(id,1);
    }

    public static void inc(String id, int delta)
    {
        Integer v=values.get(id);

        if (v == null)
            set(id,0);

        set(id,get(id)+delta);
    }

    public static Set<String> getIds()
    {
        return values.keySet();
    }
}
