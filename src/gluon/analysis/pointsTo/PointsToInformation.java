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

package gluon.analysis.pointsTo;

import soot.Scene;
import soot.SootClass;
import soot.Local;
import soot.PointsToAnalysis;
import soot.PointsToSet;
import soot.Type;

import soot.jimple.spark.pag.PAG;
import soot.jimple.spark.pag.Node;
import soot.jimple.spark.pag.AllocNode;
import soot.jimple.spark.sets.P2SetVisitor;
import soot.jimple.spark.sets.DoublePointsToSet;

import java.util.Collection;
import java.util.Iterator;

import java.util.LinkedList;

public class PointsToInformation
{
    private PointsToInformation()
    {
        assert false;
    }

    public static boolean isModuleInstance(Type module, Type obj)
    {
        Type leastCommonType;

        try { leastCommonType=module.merge(obj,Scene.v()); }
        catch (Exception _) { return false; }

        return leastCommonType.equals(module);
    }

    public static Collection<AllocNode> getModuleAllocationSites(SootClass module)
    {
        Collection<AllocNode> allocSites=new LinkedList<AllocNode>();
        PointsToAnalysis pta=Scene.v().getPointsToAnalysis();
        PAG pag;
        Iterator it;

        assert pta instanceof PAG;

        pag=(PAG)pta;

        for (it=pag.getAllocNodeNumberer().iterator();
             it.hasNext(); )
        {
            AllocNode an=(AllocNode)it.next();

            if (isModuleInstance(module.getType(),an.getType()))
                allocSites.add(an);
        }

        return allocSites;
    }

    public static Collection<AllocNode> getReachableAllocSites(Local l)
    {
        final Collection<AllocNode> asites=new LinkedList<AllocNode>();
        PointsToAnalysis pta;
        PointsToSet rObjs;

        DoublePointsToSet reacObjs;

        pta=Scene.v().getPointsToAnalysis();
        rObjs=pta.reachingObjects(l);

        if (!(rObjs instanceof DoublePointsToSet))
            return asites;

        reacObjs=(DoublePointsToSet)rObjs;

        reacObjs.forall(
                        new P2SetVisitor()
                        {
                            public final void visit(Node n)
                            {
                                if (n instanceof AllocNode)
                                    asites.add((AllocNode)n);
                            }
                        }
                        );

        return asites;
    }
}
