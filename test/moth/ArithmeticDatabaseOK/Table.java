package test.moth.ArithmeticDatabaseOK;

import java.util.List;
import java.util.LinkedList;
import java.util.Iterator;

import test.common.Atomic;
import test.common.Contract;

@Contract(clauses ="get_max_key insert;"
                  +"iterator insert;")
public class Table<K extends Comparable<K>,V> implements Iterable<Pair<K,V>>
{
	private List<Pair<K,V>> table;

	public Table()
	{
		table=new LinkedList<Pair<K,V>>();
	}

	@Atomic
	public /*synchronized*/ void insert(K k, V v)
	{
		table.add(new Pair<K,V>(k,v));
	}

	@Atomic
	public /*synchronized*/ int count()
	{
		return table.size();
	}

	@Atomic
	public /*synchronized*/ K get_max_key()
        {
		K max=null;

		for (Pair<K,V> row: table)
			if (max == null 
			    || max.compareTo(row.k) < 0)
				max=row.k;

		return max;
	}

//	@Atomic
	public /*synchronized*/ Iterator<Pair<K,V>> iterator()
        {
		return table.iterator();
	}

	@Atomic
	public /*synchronized*/ String toString()
	{
		String r="{";
		int i=0;

		for (Pair<K,V> p: table)
		{
			r+=(i == 0 ? "" : ",")+"\n ("+p.k+","+p.v+")";
			i++;
		}

		return r+"}";
	}
}
