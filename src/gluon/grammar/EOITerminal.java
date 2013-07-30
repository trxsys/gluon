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

package gluon.grammar;

public final class EOITerminal
    extends Terminal
{
    public EOITerminal()
    {
        super("$");
    }

    public boolean isEOI()
    {
        return true;
    }

    @Override
    public int hashCode()
    {
        return 0xdecbfe1d;
    }

    @Override
    public boolean equals(Object o)
    {
        return o instanceof EOITerminal;
    }

    @Override
    public EOITerminal clone()
    {
        return new EOITerminal();
    }
}
