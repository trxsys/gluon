package gluon.startFreeRegExp;

import java.util.Set;
import java.util.List;

import java.util.HashSet;

public class StarFreeRegExpEmpty
    extends StarFreeRegExp
{
    public StarFreeRegExpEmpty()
    {

    }

    @Override
    public Set<List<String>> getWords()
    {
        return new HashSet<List<String>>();
    }

    @Override
    public String toString()
    {
        return "∅";
    }
}
