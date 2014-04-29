package test.simple.auction;

/**
 * @author Diogo Sousa, nº 28970, Turno P10
 *
 * Docente (prática): Pedro Leandro da Silva Amaral
 * Docente (teórico-prática): Miguel Goulão
 */

/* Interface for general iteratores */
public interface Iterator<T>
{
	/* Set the current item index pointing to the head of the list */
	public void toHead();

	/* Set the current item index pointing to the tail of the list */
	public void toTail();

	/* End of list? */
	public boolean end();

	/* End of list? (in reverse order) */
	public boolean rend();

	/* Set the current item index pointing to the next element */
	public void next();

	/* Set the current item index pointing to the previous element */
	public void prev();

	/* Is the current item index pointing to a legal position? */
	public boolean isLegalPos();

	/* Returns the current item */
	public T get();

	/* Total number of entries in the list */
	public int size();

	/* Number of entries left */
	public int left();

	/* Number of entries left (in reverse order) */
	public int rleft();
}
