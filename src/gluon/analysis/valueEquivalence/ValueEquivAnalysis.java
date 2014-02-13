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

import gluon.PointsToInformation;

import soot.SootMethod;
import soot.Value;
import soot.Local;
import soot.Unit;
import soot.SootField;

import soot.jimple.Stmt;
import soot.jimple.AssignStmt;
import soot.jimple.InvokeExpr;
import soot.jimple.InstanceFieldRef;

import soot.jimple.spark.pag.AllocNode;

import java.util.Queue;
import java.util.Set;
import java.util.LinkedList;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;

/* Since the fields assingments to local variables are only done (statically) 
 * before they are used, and are using SSA we don't need a full data-flow
 * analysis.
 */

public class ValueEquivAnalysis
{
    private SootMethod entryMethod;

    private Map<ValueM,SootField> localFieldMap;
    private Set<SootMethod> analizedMethods;

    public ValueEquivAnalysis(SootMethod method)
    {
        entryMethod=method;
        
        localFieldMap=new HashMap<ValueM,SootField>();
        analizedMethods=new HashSet<SootMethod>();
    }

    private void analyzeMethod(SootMethod method)
    {
        if (!method.hasActiveBody())
            return;

        if (analizedMethods.contains(method))
            return;

        analizedMethods.add(method);

        for (Unit u: method.getActiveBody().getUnits())
            if (u instanceof AssignStmt)
            {
                AssignStmt assign=(AssignStmt)u;
                Value l=assign.getLeftOp();
                Value r=assign.getRightOp();

                if (l instanceof Local
                    && r instanceof InstanceFieldRef)
                {
                    SootField field=((InstanceFieldRef)r).getField();
                    ValueM local=new ValueM(method,l);

                    localFieldMap.put(local,field);
                }
            }
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
        SootField uField;
        SootField vField;

        gluon.profiling.Timer.start("analysis-vequiv");
        analyzeMethod(u.getMethod());
        analyzeMethod(v.getMethod());
        gluon.profiling.Timer.stop("analysis-vequiv");

        uField=localFieldMap.get(u);
        vField=localFieldMap.get(v);

        if (uField == null || vField == null)
            return false;

        return uField.getSignature().equals(vField.getSignature());
    }

    public boolean equivTo(ValueM u, ValueM v)
    {
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
        }

        return false;
    }
}
