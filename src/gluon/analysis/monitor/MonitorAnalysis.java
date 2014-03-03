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

package gluon.analysis.monitor;

import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;

import soot.jimple.EnterMonitorStmt;
import soot.jimple.ExitMonitorStmt;

import soot.toolkits.graph.UnitGraph;
import soot.toolkits.graph.BriefUnitGraph;

import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Collection;
import java.util.ArrayList;

class StackNode<E>
{
    private StackNode<E> parent;
    private E element;

    public StackNode(StackNode<E> p, E elem)
    {
        parent=p;
        element=elem;
    }

    public StackNode<E> parent()
    {
        return parent;
    }

    public E element()
    {
        return element;
    }
};

public class MonitorAnalysis
{
    private final Scene scene;
    private Map<EnterMonitorStmt,Collection<ExitMonitorStmt>> exitMon;

    private Set<Unit> visited;
   
    public MonitorAnalysis(Scene s)
    {
        scene=s;
        visited=null;
        exitMon=new HashMap<EnterMonitorStmt,Collection<ExitMonitorStmt>>();
    }
        
    private void analyzeUnit(SootMethod method, Unit unit, UnitGraph cfg,
                             StackNode<EnterMonitorStmt> stack)
    {
        if (visited.contains(unit))
            return; /* Unit already taken care of */

        if (unit instanceof EnterMonitorStmt)
            stack=new StackNode<EnterMonitorStmt>(stack,(EnterMonitorStmt)unit);
        else if (unit instanceof ExitMonitorStmt)
        {
            EnterMonitorStmt enter;

            assert stack != null : "no matching entermonitor";

            enter=stack.element();

            if (!exitMon.containsKey(enter))
                exitMon.put(enter,new ArrayList<ExitMonitorStmt>(8));

            exitMon.get(enter).add((ExitMonitorStmt)unit);

            stack=stack.parent();
        }

        for (Unit succ: cfg.getSuccsOf(unit))
            analyzeUnit(method,succ,cfg,stack);
    }

    public Collection<ExitMonitorStmt> getExitMonitor(EnterMonitorStmt enterMon)
    {
        assert exitMon.containsKey(enterMon);

        return exitMon.get(enterMon);
    }

    public void analyze()
    {
        visited=new HashSet<Unit>();

        for (SootClass c: scene.getClasses())
            for (SootMethod m: c.getMethods())
            {
                UnitGraph cfg;

                if (!m.hasActiveBody())
                    continue;
                
                cfg=new BriefUnitGraph(m.getActiveBody());
                
                for (Unit head: cfg.getHeads())
                {
                    analyzeUnit(m,head,cfg,null);
                }
            }

        visited=null;
    }
}
