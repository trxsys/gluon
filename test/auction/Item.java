package test.auction;

/**
 * @author Diogo Sousa, nº 28970, Turno P10
 *
 * Docente (prática): Pedro Leandro da Silva Amaral
 * Docente (teórico-prática): Miguel Goulão
 */

public class Item
{
	private String name;  /* Article's name */
	
	public Item(String name)
	{
		this.name=name;
	}	
	
	public String getName()
	{
        Main.m.j();
		return name;
	}

	public void setName(String name)
	{
		this.name=name;
	}
}
