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
import soot.SootClass;
import soot.Unit;

import soot.tagkit.LineNumberTag;
import soot.tagkit.SourceFileTag;
import soot.tagkit.Tag;

// Represents a call to the module under analysis
public class PPTerminal
    extends gluon.grammar.Terminal
{
    private final SootMethod method; // module method
    private final Unit codeUnit;

    public PPTerminal(SootMethod m, Unit u)
    {
        super(m.getName());
        method=m;
        codeUnit=u;
    }

    public PPTerminal(String s)
    {
        super(s);
        method=null;
        codeUnit=null;
    }

    public Unit getCodeUnit()
    {
        return codeUnit;
    }

    public SootMethod getCodeMethod()
    {
        return method;
    }

    public int getLineNumber()
    {
        assert codeUnit != null;

        LineNumberTag lineTag=(LineNumberTag)codeUnit.getTag("LineNumberTag");
        int linenum=-1;

        if (lineTag != null)
                linenum=lineTag.getLineNumber();

        return linenum;
    }

    public SootClass getCodeClass()
    {
        return getCodeMethod().getDeclaringClass();
    }

    public String getSourceFile()
    {
        assert method != null;

        String source="?";
        SootClass c=getCodeClass();
        SourceFileTag sourceTag=(SourceFileTag)c.getTag("SourceFileTag");

        if (sourceTag != null)
            source=sourceTag.getSourceFile();

        return source;
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
        return method != null ? new PPTerminal(method,codeUnit)
            : new PPTerminal(super.getName());
    }
}
