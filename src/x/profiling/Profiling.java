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
        return values.get(id);
    }

    public static Set<String> getIds()
    {
        return values.keySet();
    }
}
