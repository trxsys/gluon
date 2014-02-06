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

import java.util.ArrayList;
import java.util.List;

// Represents a call to the module under analysis
public class PPTerminal
    extends gluon.grammar.Terminal
{
    private final SootMethod method; // module method
    private final Unit codeUnit;
    /* arguments is null if the arguments are to be ignored.
     * An null argument represents an argument that should be ignored.
     */
    private List<String> arguments;
    
    public PPTerminal(SootMethod m, Unit u)
    {
        super(m.getName());
        method=m;
        codeUnit=u;
        arguments=null;
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

    public List<String> getArguments()
    {
        return arguments;
    }

    public void addArgument(String arg)
    {
        if (arguments == null)
            arguments=new ArrayList<String>(16);

        arguments.add(arg);
    }

    public String getFullName()
    {
        String str=getName();

        if (arguments != null)
        {
            int i=0;

            str+="(";

            for (String v: arguments)
                str+=(i++ == 0 ? "" : ",")+(v == null ? "_" : v);

            str+=")";
        }

        return str;
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
        PPTerminal clone=method != null ? new PPTerminal(method,codeUnit)
            : new PPTerminal(super.getName());

        clone.arguments=new ArrayList<String>(arguments.size());
        clone.arguments.addAll(arguments);

        return clone;
    }
}
