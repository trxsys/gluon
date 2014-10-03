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

import soot.Unit;
import soot.toolkits.graph.UnitGraph;

import java.util.Collection;
import java.util.ArrayList;

public class SynchedSection
{
    public final Unit entry;
    public final UnitGraph cfg;
    public final Collection<Unit> exit;

    public SynchedSection(Unit entry, UnitGraph cfg,
                          Collection<Unit> exit)
    {
        this.entry=entry;
        this.cfg=cfg;
        this.exit=new ArrayList<Unit>(8);
        this.exit.addAll(exit);
    }
}
