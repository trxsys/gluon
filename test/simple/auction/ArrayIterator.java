package test.simple.auction;

/**
 * @author Diogo Sousa, nº 28970, Turno P10
 *
 * Docente (prática): Pedro Leandro da Silva Amaral
 * Docente (teórico-prática): Miguel Goulão
 */

public class ArrayIterator<T> implements Iterator<T>
{
	private Array<T> array;
	private int index;

	public ArrayIterator(Array<T> array)
	{
		this.array=array;
		toHead();
	}

	/* Set the current item index pointing to the head of the array */
	public void toHead()
	{
		index=0;
	}

	/* Set the current item index pointing to the tail of the array */
	public void toTail()
	{
		index=array.size()-1;
	}

	/* End of array? */
	public boolean end()
	{
		return index >= array.size();
	}

	/* End of array? (in reverse order) */
	public boolean rend()
	{
		return index < 0;
	}

	/* Set the current item index pointing to the next element */
	public void next()
	{
		index++;
	}

	/* Set the current item index pointing to the previous element */
	public void prev()
	{
		index--;
	}

	/* Is the current item index pointing to a legal position? */
	public boolean isLegalPos()
	{
		return !end() && !rend();
	}

	/* Returns the current item
	 *
	 * pre: islegalPos()
	 */
	public T get()
	{
        Main.m.j();
		return array.get(index);
	}

	/* Total number of entries in the array */
	public int size()
	{
		return array.size();
	}

	/* Number of entries left */
	public int left()
	{
        Main.m.d();
		return array.size()-(index+1);
	}

	/* Number of entries left (in reverse order) */
	public int rleft()
	{
		return index;
	}
}
