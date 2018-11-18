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

import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.tagkit.LineNumberTag;
import soot.tagkit.SourceFileTag;

import java.util.ArrayList;
import java.util.List;

/* Represents a call to the module under analysis */
public class PBTerminal
    extends gluon.grammar.Terminal
{
    private final SootMethod method; /* module's method */
    private final Unit codeUnit;
    private SootMethod codeUnitMethod;

    /* arguments is null if the arguments are to be ignored.
     * An null argument represents an argument that should be ignored.
     */
    private List<String> arguments;
    private String ret; /* null if not used */

    public PBTerminal(SootMethod m, Unit u, SootMethod cUnitMethod)
    {
        super(m.getName());
        method=m;
        codeUnit=u;
        arguments=null;
        ret=null;
        codeUnitMethod=cUnitMethod;
    }

    public PBTerminal(String s)
    {
        super(s);
        method=null;
        codeUnit=null;
        arguments=null;
        ret=null;
        codeUnitMethod=null;
    }

    public Unit getCodeUnit()
    {
        return codeUnit;
    }

    public SootMethod getCodeMethod()
    {
        return codeUnitMethod;
    }

    public SootClass getCodeClass()
    {
        return getCodeMethod().getDeclaringClass();
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

    public String getReturn()
    {
        return ret;
    }

    public void setReturn(String r)
    {
        ret=r;
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

        return (ret != null ? ret+"=" : "")+str;
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
        PBTerminal other;

        if (!(o instanceof PBTerminal))
            return false;

        other=(PBTerminal)o;

        return other.name.equals(super.name);
    }

    @Override
    public PBTerminal clone()
    {
        PBTerminal clone=method != null ? new PBTerminal(method,codeUnit,codeUnitMethod)
            : new PBTerminal(super.getName());

        clone.arguments=new ArrayList<String>(arguments.size());
        clone.arguments.addAll(arguments);

        clone.ret=ret;

        assert clone.codeUnitMethod == codeUnitMethod;

        return clone;
    }
}
