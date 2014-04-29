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

public class PPNonTerminal
    extends gluon.grammar.NonTerminal
{
    private SootMethod method;
    private boolean noRemove;
    private boolean synchBlock;

    public PPNonTerminal(String s, SootMethod m)
    {
        super(s);
        method=m;
        noRemove=false;
        synchBlock=false;
    }

    public SootMethod getMethod()
    {
        return method;
    }

    public void setNoRemove()
    {
        noRemove=true;
    }

    public void setSynchBlock()
    {
        synchBlock=true;
    }

    public boolean isSynchBlock()
    {
        return synchBlock;
    }

    @Override
    public boolean noRemove()
    {
        return noRemove;
    }

    @Override
    public int hashCode()
    {
        return super.name.hashCode();
    }

    @Override
    public boolean equals(Object o)
    {
        PPNonTerminal other;

        if (!(o instanceof PPNonTerminal))
            return false;

        other=(PPNonTerminal)o;

        return other.name.equals(super.name);
    }

    @Override
    public PPNonTerminal clone()
    {
        PPNonTerminal clone=new PPNonTerminal(super.name,method);

        clone.noRemove=noRemove;
        clone.synchBlock=synchBlock;

        return clone;
    }
}
