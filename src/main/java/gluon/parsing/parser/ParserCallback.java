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

package gluon.parsing.parser;

import gluon.grammar.NonTerminal;
import gluon.parsing.parsingTable.parsingAction.ParsingAction;

import java.util.List;

public interface ParserCallback
{
    /* Whenever the parser reaches an LCA this is called with the list of
     * parsing actions so far.
     * Notice that this only contains a partial parse tree (up to the LCA);
     * there might be multiple ways to complete this tree up to the start
     * nonterminal.
     *
     * If the return value is false the parsing branch is discarded.
     */
    public boolean onLCA(List<ParsingAction> actions, NonTerminal lca);

    /* Called periodically while the parser is running.
     * It can be used to implement a timeout when the parser is running for too
     * long.
     */
    public boolean shouldAbort();
    public void accepted(List<ParsingAction> actions, NonTerminal lca);
}
