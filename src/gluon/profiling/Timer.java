package gluon.profiling;

import java.util.Map;
import java.util.Set;

import java.util.HashMap;

public class Timer
{
    private static Map<String,Long> timer;
    private static Map<String,Long> running;

    static {
        timer=new HashMap<String,Long>();
        running=new HashMap<String,Long>();
    }

    private Timer()
    {
    }

    public static void start(String id)
    {
        long now=System.currentTimeMillis();

        assert !running.containsKey(id);

        running.put(id,now);
    }

    public static void stop(String id)
    {
        long now=System.currentTimeMillis();
        long delta;
        long acc=0;

        assert running.containsKey(id);

        delta=now-running.get(id);

        assert delta >= 0;
        
        running.remove(id);

        if (timer.containsKey(id))
            acc=timer.get(id);

        timer.put(id,acc+delta);
    }

    public static Set<String> getIds()
    {
        return timer.keySet();
    }

    public static long getTime(String id)
    {
        Long t=timer.get(id);

        return t == null ? -1 : t;
    }
}
