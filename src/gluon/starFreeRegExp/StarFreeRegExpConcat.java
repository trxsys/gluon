package gluon.startFreeRegExp;

import java.util.Set;
import java.util.List;

import java.util.HashSet;
import java.util.ArrayList;

public class StarFreeRegExpConcat
    extends StarFreeRegExp
{
    private final StarFreeRegExp left;
    private final StarFreeRegExp right;

    public StarFreeRegExpConcat(StarFreeRegExp l, StarFreeRegExp r)
    {
        left=l;
        right=r;
    }

    @Override
    public Set<List<String>> getWords()
    {
        Set<List<String>> words=new HashSet<List<String>>();

        for (List<String> wl: left.getWords())
            for (List<String> wr: right.getWords())
            {
                List<String> wlr=new ArrayList<String>(wl.size()+wr.size());

                wlr.addAll(wl);
                wlr.addAll(wr);

                words.add(wlr);
            }

        return words;
    }

    @Override
    public String toString()
    {
        return "("+left.toString()+") ⋅ ("+right.toString()+")";
    }
}
