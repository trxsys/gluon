/* This file is part of Gluon.
 *
 * Gluon is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Gluon is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Gluon.  If not, see <http://www.gnu.org/licenses/>.
 */

package gluon.profiling;

import java.util.*;

public class Timer
{
    private static Map<String,Long> timer;
    private static Map<String,Long> running;

    static
    {
        timer=new HashMap<String,Long>();
        running=new HashMap<String,Long>();
    }

    private Timer()
    {
        assert false;
    }

    public static void start(String id)
    {
        long now=System.currentTimeMillis();

        assert !running.containsKey(id) : "Timer is already running "+id;

        running.put(id,now);
    }

    public static void stop(String id)
    {
        long now=System.currentTimeMillis();
        long delta;
        long acc=0;

        assert running.containsKey(id) : "Timer is not running";

        delta=now-running.get(id);

        assert delta >= 0;

        running.remove(id);

        if (timer.containsKey(id))
            acc=timer.get(id);

        timer.put(id,acc+delta);
    }

    public static List<String> getIds()
    {
        List<String> ids=new ArrayList<String>(timer.size());

        ids.addAll(timer.keySet());

        Collections.sort(ids);

        return ids;
    }

    public static long getTime(String id)
    {
        Long t=timer.get(id);

        return t == null ? -1 : t;
    }
}
