package test.auction;

/**
 * @author Diogo Sousa, nº 28970, Turno P10
 *
 * Docente (prática): Pedro Leandro da Silva Amaral
 * Docente (teórico-prática): Miguel Goulão
 */

/* Interface for a array sorter */
public interface Sorter<T>
{
	public void sort();
	public void setArray(Array<T> array);
}
