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

package gluon.analysis.valueEquivalence;

import soot.Value;
import soot.SootMethod;
import soot.Local;

public class ValueM
{
    private SootMethod method;
    private Value value;

    public ValueM(SootMethod m, Value v)
    {
        method=m;
        value=v;
    }

    public SootMethod getMethod()
    {
        return method;
    }

    public Value getValue()
    {
        return value;
    }

    public Local getValueAsLocal()
    {
        assert isLocal();

        return (Local)value;
    }

    public boolean isLocal()
    {
        return value instanceof Local;
    }

    boolean basicEquivTo(Object o)
    {
        ValueM other;

        if (!(o instanceof ValueM))
            return false;

        other=(ValueM)o;

        /* this case is not handled well by soot */
        if (isLocal())
            return method.getSignature().equals(other.method.getSignature())
                && getValueAsLocal().getName().equals(other.getValueAsLocal().getName());

        return value.equivTo(other.value);
    }

    @Override
    public boolean equals(Object o)
    {
        ValueM other;

        if (!(o instanceof ValueM))
            return false;

        other=(ValueM)o;

        /* this case is not handled well by soot */
        if (isLocal())
            return method.getSignature().equals(other.method.getSignature())
                && getValueAsLocal().getName().equals(other.getValueAsLocal().getName());

        return value.equals(other.value);
    }

    @Override
    public int hashCode()
    {
        int vh;

        /* this case is not handled well by soot */
        vh=isLocal() ? getValueAsLocal().getName().hashCode()
                     : value.hashCode();

        return vh^method.getSignature().hashCode();
    }

    @Override
    public String toString()
    {
        return method.getName()+":"+value.toString();
    }
}
