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

public class StarFreeRegExpAlt
    extends StarFreeRegExp
{
    private final StarFreeRegExp left;
    private final StarFreeRegExp right;

    public StarFreeRegExpAlt(StarFreeRegExp l, StarFreeRegExp r)
    {
        left=l;
        right=r;
    }

    @Override
    public Set<List<String>> getWords()
    {
        Set<List<String>> words=new HashSet<List<String>>();

        words.addAll(left.getWords());
        words.addAll(right.getWords());

        return words;
    }

    @Override
    public String toString()
    {
        return "("+left.toString()+") | ("+right.toString()+")";
    }
}
