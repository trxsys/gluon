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
