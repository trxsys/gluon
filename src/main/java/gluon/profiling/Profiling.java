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

public class Profiling
{
    private static Map<String,Long> values;

    static
    {
        values=new HashMap<String,Long>();
    }

    private Profiling()
    {
        assert false;
    }

    public static void set(String id, long v)
    {
        values.put(id,v);
    }

    public static long get(String id)
    {
        Long v=values.get(id);

        assert v != null;

        return v;
    }

    public static void inc(String id)
    {
        inc(id,1);
    }

    public static void inc(String id, int delta)
    {
        Long v=values.get(id);

        if (v == null)
            v=0L;

        set(id,v+delta);
    }

    public static List<String> getIds()
    {
        List<String> ids=new ArrayList<String>(values.size());

        ids.addAll(values.keySet());

        Collections.sort(ids);

        return ids;
    }
}
