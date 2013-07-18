package test.simple.auction;

/**
 * @author Diogo Sousa, nº 28970, Turno P10
 *
 * Docente (prática): Pedro Leandro da Silva Amaral
 * Docente (teórico-prática): Miguel Goulão
 */

import java.util.Comparator;

/* Generic type array implementation.
 * 
 * Note: You may safely ignore the type safety warning at compile time.
 */
public class Array<T>
{
	private static final int INIT_SIZE=16; /* Initial array size */
	private static final int EXPANSION_COEFFICIENT=2;

	private T[] elem;
	private int count; /* Number of valid entries in elem */

	public Array()
	{
		elem=(T[])new Object[INIT_SIZE];
		count=0;
	}

	public Array(Array<T> array)
	{
		this(array.elem,0,array.count-1);
	}

	public Array(T[] array)
	{
		this(array,0,array.length-1);
	}

	/* pre: count <= array.length */
	public Array(T[] array, int count)
	{
		this(array,0,count-1);
	}

	/* Here begin and end are *indexes* 
	 *	
	 * pre: begin >= 0 && end < array.length
	 */
	public Array(T[] array, int begin, int end)
	{
		this();

		for (int i=begin; i <= end; i++)
			append(array[i]);
	}

	/* Returns the space left in elem array */
	private int spaceLeft()
	{
		return elem.length-count;
	}

	/* Expand elem array size to newSize */
	private void expand(int newSize)
	{
		T[] tmp=(T[])new Object[newSize];

		for (int i=0; i < count; i++)
			tmp[i]=elem[i];

        Main.m.a();

		elem=tmp;
	}

	/* Append e to elem array */
	public void append(T e)
	{
		if (spaceLeft() == 0)
			expand(EXPANSION_COEFFICIENT*elem.length);

        Main.m.b();

		elem[count++]=e;
	}

	/* Returns the number of elements in the array */
	public int size()
	{
		return count;
	}

	/* Returns the element in position index. 
	 *
	 * pre: isLegalIndex(index)
	 */
	public T get(int index)
	{
		return elem[index];
	}

	/* Sets v in position index. 
	 *
	 * pre: isLegalIndex(index)
	 */
	public void set(int index, T v)
	{
		elem[index]=v;
	}

	/* Swap two elements of the array 
	 *
	 * pre: isLegalIndex(p) && isLegalIndex(q)
	 */
	public void swap(int p, int q)
	{
		T tmp;

		tmp=elem[p];
		elem[p]=elem[q];
		elem[q]=tmp;
	}

	/* Sort the array according to cmp */
	public void sort(Sorter sorter)
	{
		sorter.setArray(this);
		sorter.sort();
	}

	/* Insert e at the given index
	 *
	 * pre: isLegalIndex(index)
	 */	
	public void insertAt(int index, T e)
	{
		if (spaceLeft() == 0)
			expand(EXPANSION_COEFFICIENT*elem.length);

		for (int i=count-1; i > index; i--)
        {
			elem[i+1]=elem[i];
        Main.m.c();
        }

		elem[index]=e;
		count++;
	}

	/* Remove the element at the given index
	 *
	 * pre: isLegalIndex(index)
	 */	
	public void remove(int index)
	{
		for (int i=index; i < count-1; i++)
			elem[i]=elem[i+1];
		
		count--;
	}

	/* Is legal index? */
	public boolean isLegalIndex(int index)
	{
        Main.m.d();
		return index >= 0 && index < count;
	}

	/* Returns a iterator */
	public Iterator<T> getIterator()
	{
		return new ArrayIterator<T>(this);
	}
}
