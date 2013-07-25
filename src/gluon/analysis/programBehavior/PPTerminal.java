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

package gluon.analysis.programBehavior;

import soot.SootMethod;

// Represents a call to the module under analysis
public class PPTerminal
    extends gluon.cfg.Terminal
{
    private final SootMethod method; // module method

    public PPTerminal(SootMethod m)
    {
        super(m.getName());
        method=m;
    }

    public PPTerminal(String s)
    {
        super(s);
        method=null;
    }

    @Override
    public boolean isEOI()
    {
        return false;
    }

    @Override
    public int hashCode()
    {
        return toString().hashCode();
    }

    @Override
    public boolean equals(Object o)
    {
        PPTerminal other;

        if (!(o instanceof PPTerminal))
            return false;

        other=(PPTerminal)o;

        return other.name.equals(super.name);
    }

    @Override
    public PPTerminal clone()
    {
        return method != null ? new PPTerminal(method) 
            : new PPTerminal(super.getName());
    }
}
