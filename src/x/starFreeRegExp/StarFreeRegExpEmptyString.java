package x.startFreeRegExp;

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
