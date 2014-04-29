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

package gluon.contract;

import gluon.Main;

import soot.SootClass;
import soot.SootMethod;

import gluon.contract.parsing.ContractVisitorExtractWords;
import gluon.contract.parsing.node.*;

import gluon.grammar.Terminal;
import gluon.analysis.programBehavior.PPTerminal;

import soot.tagkit.AnnotationTag;
import soot.tagkit.Tag;
import soot.tagkit.VisibilityAnnotationTag;
import soot.tagkit.AnnotationStringElem;

import java.io.PushbackReader;
import java.io.StringReader;

import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;

public class Contract
{
    private static final boolean DEBUG=false;

    private static final String CONTRACT_ANNOTATION="Contract";

    private SootClass module;  // class of the module to analyze
    private Collection<List<Terminal>> contract;

    public Contract(SootClass mod, String rawContract)
    {
        module=mod;
        contract=null;
        loadRawContract(rawContract);
    }

    public Contract(SootClass mod)
    {
        module=mod;
        contract=null;
    }

    private void dprint(String s)
    {
        if (DEBUG)
            System.out.print(s);
    }

    private void dprintln(String s)
    {
        dprint(s+"\n");
    }

    private Start parseContract(String clause)
    {
        PushbackReader reader=new PushbackReader(new StringReader(clause));
        gluon.contract.parsing.lexer.Lexer lexer
            =new gluon.contract.parsing.lexer.Lexer(reader);
        gluon.contract.parsing.parser.Parser parser
            =new gluon.contract.parsing.parser.Parser(lexer);
        Start ast=null;

        try
        {
            ast=parser.parse();
        }
        catch (gluon.contract.parsing.parser.ParserException pe)
        {
            Main.fatal("syntax error in contract: "+clause+": "+pe.getMessage());
        }
        catch (gluon.contract.parsing.lexer.LexerException pe)
        {
            Main.fatal("syntax error in contract: "+clause+": "+pe.getMessage());
        }
        catch (java.io.IOException _)
        {
            assert false : "this should not happen";
        }

        assert ast != null;

        return ast;
    }

    private void loadRawContractClause(String clause)
    {
        Start ast;
        ContractVisitorExtractWords visitorWords
            =new ContractVisitorExtractWords();

        ast=parseContract(clause);
        ast.apply(visitorWords);

        for (List<PPTerminal> word: visitorWords.getWords())
        {
            List<Terminal> contractWord;

            for (PPTerminal t: word)
                if (module.declaresMethodByName(t.getName()))
                {
                    try
                    {
                        SootMethod m=module.getMethodByName(t.getName());

                        if (t.getArguments() != null
                            && t.getArguments().size() != m.getParameterCount())
                            Main.fatal(t.getName()
                                       +": wrong number of parameters!");
                    }
                    catch (Exception _)
                    {
                        Main.warning(t.getName()+": ambiguous method!");
                    }
                }
                else
                    Main.fatal(t.getName()+": no such method!");

            contractWord=new ArrayList<Terminal>(word.size()+1);

            contractWord.addAll(word);
            contractWord.add(new gluon.grammar.EOITerminal());

            contract.add(contractWord);
        }
    }

    private void loadRawContract(String rawContract)
    {
        contract=new LinkedList<List<Terminal>>();

        for (String clause: rawContract.split(";"))
        {
            clause=clause.trim();

            if (clause.length() == 0)
                continue;

            loadRawContractClause(clause);
        }

        dprintln("contract: "+contract);
    }

    private String extractContractRaw()
    {
         Tag tag=module.getTag("VisibilityAnnotationTag");

         if (tag == null)
            return null;

        VisibilityAnnotationTag visibilityAnnotationTag=(VisibilityAnnotationTag) tag;
        List<AnnotationTag> annotations=visibilityAnnotationTag.getAnnotations();

        for (AnnotationTag annotationTag: annotations)
            if (annotationTag.getType().endsWith("/"+CONTRACT_ANNOTATION+";")
                && annotationTag.getNumElems() == 1
                && annotationTag.getElemAt(0) instanceof AnnotationStringElem
                && annotationTag.getElemAt(0).getName().equals("clauses"))
            {
                AnnotationStringElem e=(AnnotationStringElem)annotationTag.getElemAt(0);

                return e.getValue();
            }

        return null;
    }

    public void loadAnnotatedContract()
    {
        loadRawContract(extractContractRaw());
    }

    public Collection<List<Terminal>> getWords()
    {
        return contract;
    }

    public int clauseNum()
    {
        return contract.size();
    }
}
