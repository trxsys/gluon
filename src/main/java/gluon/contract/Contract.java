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
import gluon.analysis.programBehavior.PBTerminal;
import gluon.contract.parsing.ContractVisitorExtractWords;
import gluon.contract.parsing.node.Start;
import gluon.grammar.Terminal;
import soot.SootClass;
import soot.SootMethod;
import soot.tagkit.*;

import java.io.PushbackReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

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
        catch (java.io.IOException e)
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

        for (List<PBTerminal> word: visitorWords.getWords())
        {
            List<Terminal> contractWord;

            for (PBTerminal t: word)
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
                    catch (Exception e)
                    {
                        Main.warning(t.getName()+": ambiguous method!");
                    }
                }
                else
                    Main.fatal(t.getName()+": no such method!");

            contractWord=new ArrayList<Terminal>(word.size()+1);

            contractWord.addAll(word);

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

        for (AnnotationTag annotationTag: annotations) {
            Collection<AnnotationElem> elements=annotationTag.getElems();

            if (annotationTag.getType().endsWith("/"+CONTRACT_ANNOTATION+";")
                && elements.size() == 1) {
                AnnotationElem elem=elements.iterator().next();

                if (elem instanceof AnnotationStringElem
                    && elem.getName().equals("clauses"))
                {
                    AnnotationStringElem e=(AnnotationStringElem)elem;

                    return e.getValue();
                }
            }
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
