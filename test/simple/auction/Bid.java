package test.simple.auction;

/**
 * @author Diogo Sousa, nº 28970, Turno P10
 *
 * Docente (prática): Pedro Leandro da Silva Amaral
 * Docente (teórico-prática): Miguel Goulão
 */

public class Bid implements Comparable<Bid>
{
	public static final char PLACEHOLDER=':'; /* Placeholder for toString()
						     and fromString() */
	public static final char CLASS_TYPE='b'; /* For toString()
						    and fromString() */

	private int value;
	private Customer customer;

	public Bid(int value, Customer customer)
	{
		this.value=value;
		this.customer=customer;
	}

	/* Just for comparing purposes */
	public Bid(int value)
	{
		this(value,null);
	}

	/* Useful to load a object fromString() */
	public Bid()
	{
		this(0);
	}

	public int getValue()
	{
		return value;
	}

	public Customer getCustomer()
	{
		return customer;
	}

	public void setCustomer(Customer customer)
	{
		this.customer=customer;
	}

	public boolean isHigher(Bid other)
	{
        Main.m.h();
		return compareTo(other) < 0;
	}

	public int compareTo(Bid other)
	{
		return value-other.value;
	}

	/* This should always keep symmetry with fromString() */
	public String toString(String auctionName, String itemName)
	{
        Main.m.g();
		return CLASS_TYPE+""+PLACEHOLDER+auctionName+PLACEHOLDER
			+itemName+PLACEHOLDER+customer.getName()+PLACEHOLDER
			+value;
	}

	/* pre: a valid objStr which must be in the same format as toString()
	 *      returns
	 */
	public void fromString(String objStr)
	{
		String[] tokens=objStr.split(PLACEHOLDER+"");

		value=Integer.parseInt(tokens[4]);
	}
}
