package test.auction;

/**
 * @author Diogo Sousa, nº 28970, Turno P10
 *
 * Docente (prática): Pedro Leandro da Silva Amaral
 * Docente (teórico-prática): Miguel Goulão
 */

import java.util.Comparator;
import java.util.Random;

/* SorterQuickSort implements quicksort, a non-stable, in-place sorting
 * algorithm. Quicksort takes O(n**2) worst case (the near worst case 
 * is unlikely) and O(n*lg(n)) on average (the average is also the best case).
 * This implementation uses randomization so no specific input will trigger
 * the worst case quadratic time.
 */
public class SorterQuickSort<T> implements Sorter<T>
{
	private Array<T> array;
	private Comparator<T> cmp;
	private Random gen;	
    
	public SorterQuickSort(Comparator<T> cmp)
	{
		this.array=null;
		this.cmp=cmp;
		gen=new Random();
	}
	
	public void setArray(Array<T> array)
	{
		this.array=array;
	}
    
	/* Swaps pivot (always in index e) with a random element */
	private void randomizePivot(int b, int e)
	{
		array.swap(e,gen.nextInt(e-b+1)+b);
	}
	
	/* Adapted from Programming Challenges, page 91 */
	private int partition(int b, int e)
	{
		final int p=e;   /* Pivot index (always the last elem) */
		int firstHigh=b; /* Index of the first element greater 
                            than the pivot */
		
		randomizePivot(b,e);
		
		for (int i=b; i < e; i++) /* Iterate though all elements 
                                     but the pivot */
			if (cmp.compare(array.get(i),array.get(p)) < 0)
            {
				array.swap(i,firstHigh++);
                Main.m.b();
            }

		array.swap(firstHigh,p); /* Pivot goes to its position */
		
		return firstHigh;
	}
	
	private void quicksortRecursive(int b, int e)
	{
		int pivot; /* Pivot index */
        
        Main.m.a();
		
		if (e > b)
		{
			pivot=partition(b,e);
			
			quicksortRecursive(b,pivot-1);
            Main.m.b();
			quicksortRecursive(pivot+1,e);
		}
        
        Main.m.c();
	}
	
	public void sort()
	{
		quicksortRecursive(0,array.size()-1);

        Main.m.a();
	}
}
