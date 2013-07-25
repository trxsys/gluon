package gluon.startFreeRegExp;

import java.util.Set;
import java.util.List;

import java.util.HashSet;
import java.util.ArrayList;

public class StarFreeRegExpString
    extends StarFreeRegExp
{
    private final String string;

    public StarFreeRegExpString(String s)
    {
        assert s.length() > 0;
        string=s;
    }

    @Override
    public Set<List<String>> getWords()
    {
        Set<List<String>> words=new HashSet<List<String>>();
        ArrayList<String> word=new ArrayList<String>(1);

        word.add(string);

        words.add(word);

        return words;
    }

    @Override
    public String toString()
    {
        return string;
    }
}
