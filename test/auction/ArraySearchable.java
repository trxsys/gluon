package test.auction;

/**
 * @author Diogo Sousa, nº 28970, Turno P10
 *
 * Docente (prática): Pedro Leandro da Silva Amaral
 * Docente (teórico-prática): Miguel Goulão
 */

public class ArraySearchable<T extends Comparable<T>> extends Array<T>
{
	public ArraySearchable()
	{
		super();
	}

	/* Is "needle" in the "haystack"? */
	public boolean has(T needle)
	{
		return search(needle) != null;
	}

	/* Returns the object of the list which matches needle or null if
	 * not there is no match.
	 */
	public T search(T needle)
	{
		T r=null;

		for (Iterator<T> it=getIterator(); 
		     !it.end() && r == null; 
		     it.next())
		{
			T c=it.get();

            Main.m.l();
			
			if (it.get().compareTo(needle) == 0)
            {
				r=c;
                Main.m.j();
            }
		}

        Main.m.h();

		return r;
	}
}
