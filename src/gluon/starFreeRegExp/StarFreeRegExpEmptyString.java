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

package gluon.startFreeRegExp;

import java.util.Set;
import java.util.List;

import java.util.HashSet;
import java.util.ArrayList;

public class StarFreeRegExpEmptyString
    extends StarFreeRegExp
{
    public StarFreeRegExpEmptyString()
    {
    }

    @Override
    public Set<List<String>> getWords()
    {
        Set<List<String>> w=new HashSet<List<String>>();

        w.add(new ArrayList<String>(0));

        return w;
    }

    @Override
    public String toString()
    {
        return "ε";
    }
}
