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

import gluon.analysis.pointsTo.PointsToInformation;

import soot.SootMethod;
import soot.Value;
import soot.Local;
import soot.Unit;
import soot.SootField;

import soot.jimple.Stmt;
import soot.jimple.AssignStmt;
import soot.jimple.InvokeExpr;
import soot.jimple.FieldRef;
import soot.jimple.ArrayRef;

import soot.jimple.spark.pag.AllocNode;

import java.util.Queue;
import java.util.Set;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;

/* Since the fields assingments to local variables are only done (statically)
 * before they are used, and are using SSA we don't need a full data-flow
 * analysis.
 */
public class ValueEquivAnalysis
{
    private Map<ValueM,Value> localAssigns;
    private Set<SootMethod> analizedMethods;

    public ValueEquivAnalysis()
    {
        localAssigns=new HashMap<ValueM,Value>();
        analizedMethods=new HashSet<SootMethod>();
    }

    private void analyzeMethod(SootMethod method)
    {
        if (!method.hasActiveBody())
            return;

        if (analizedMethods.contains(method))
            return;

        analizedMethods.add(method);

        gluon.profiling.Timer.start("analysis-vequiv");
        for (Unit u: method.getActiveBody().getUnits())
            if (u instanceof AssignStmt)
            {
                AssignStmt assign=(AssignStmt)u;
                Value l=assign.getLeftOp();
                Value r=assign.getRightOp();

                if (l instanceof Local)
                {
                    ValueM local=new ValueM(method,l);

                    assert !localAssigns.containsKey(local)
                        : "localAssigns contains "+localAssigns.keySet()
                         +" and we tried to add "+local+". Are we not using SSA?!";

                    localAssigns.put(local,r);

                    assert localAssigns.containsKey(local);
                }
            }
        gluon.profiling.Timer.stop("analysis-vequiv");
    }

    private boolean canPointToSameObject(ValueM u, ValueM v)
    {
        Local ul=u.getValueAsLocal();
        Local vl=v.getValueAsLocal();

        for (AllocNode a: PointsToInformation.getReachableAllocSites(ul))
            for (AllocNode b: PointsToInformation.getReachableAllocSites(vl))
                if (a.equals(b))
                    return true;

        return false;
    }

    private boolean containsSameField(ValueM u, ValueM v)
    {
        Value uValue;
        Value vValue;
        SootField uField;
        SootField vField;

        analyzeMethod(u.getMethod());
        analyzeMethod(v.getMethod());

        uValue=localAssigns.get(u);
        vValue=localAssigns.get(v);

        if (uValue == null || vValue == null
            || !(uValue instanceof FieldRef) || !(vValue instanceof FieldRef))
            return false;

        uField=((FieldRef)uValue).getField();
        vField=((FieldRef)vValue).getField();

        return uField.getSignature().equals(vField.getSignature());
    }

    private boolean containsSameArray(ValueM u, ValueM v)
    {
        Value uValue;
        Value vValue;
        Value uBase;
        Value vBase;

        analyzeMethod(u.getMethod());
        analyzeMethod(v.getMethod());

        uValue=localAssigns.get(u);
        vValue=localAssigns.get(v);

        if (uValue == null || vValue == null
            || !(uValue instanceof ArrayRef) || !(vValue instanceof ArrayRef))
            return false;

        uBase=((ArrayRef)uValue).getBase();
        vBase=((ArrayRef)vValue).getBase();

        assert !(uBase instanceof ArrayRef) && !(vBase instanceof ArrayRef);
        return equivTo(new ValueM(u.getMethod(),uBase),
                       new ValueM(v.getMethod(),vBase));
    }


    public boolean equivTo(ValueM u, ValueM v)
    {
        Value ua;
        Value va;

        if (u == null || v == null)
            return u == v;

        if (u.basicEquivTo(v))
            return true;

        if (u.isLocal() && v.isLocal())
        {
            if (canPointToSameObject(u,v))
                return true;

            if (containsSameField(u,v))
                return true;

            if (containsSameArray(u,v))
                return true;
        }

        return false;
    }
}
