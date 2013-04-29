package test.auction;

/**
 * @author Diogo Sousa, nº 28970, Turno P10
 *
 * Docente (prática): Pedro Leandro da Silva Amaral
 * Docente (teórico-prática): Miguel Goulão
 */

public class Customer implements Comparable<Customer>
{
	private static final int DEFAULT_REPUTATION_PENALTY=10;
	private static final int CREDIBILITY_LIMIT=50;
	
	public static final char PLACEHOLDER=':'; /* Placeholder for toString()
						     and fromString() */
	public static final char CLASS_TYPE='c'; /* For toString() 
						    and fromString() */
	
	private String name;        /* Customer's name */
	private int balance;        /* Customer's balance */
	private int reputation;     /* Customer's reputation */
	private int highestBid;     /* Customer's Highest Bid */
	
	public Customer(String name, int balance)
	{
		this.name=name; 
		this.balance=balance;
		reputation=100;
		highestBid=0;
	}	
	
	/* Just for comparing purposes */
	public Customer(String name)
	{
		this(name,0);
	}	
	
	/* Useful to load a object fromString() */
	public Customer()
	{
		this(null);
	}	
	
	public String getName()
	{
		return name;
	}
	
	public int getBalance()
	{
		return balance;
	}
	
	public int getReputation()
	{
		return reputation;
	}
	
	public int getHighestBid()
	{
		return highestBid;
	}
	
	public void setHighestBidIfHigher(int value)
	{
        Main.m.i();
		highestBid=Math.max(highestBid,value);
	}
	
	public void credit(int inc)
	{
		balance+=inc;
	}
	
	public void pay(int inc)
	{
		credit(-inc);
	}
	
	public void reputationPenalty()
	{
		reputation-=DEFAULT_REPUTATION_PENALTY;
	}
	
	public boolean isCredible()
	{
        Main.m.h();
		return reputation >= CREDIBILITY_LIMIT;
	}
	
	public int compareTo(Customer other)
	{
		return name.compareToIgnoreCase(other.name);
	}
	
	/* This should always keep symmetry with fromString() */
	public String toString()
	{
		return CLASS_TYPE+""+PLACEHOLDER+name.replace(PLACEHOLDER,' ')
			+PLACEHOLDER+balance+PLACEHOLDER+reputation
			+PLACEHOLDER+highestBid;
	}
	
	/* pre: a valid objStr which must be in the same format as toString()
	 *      returns
	 */
	public void fromString(String objStr)
	{
		String[] tokens=objStr.split(PLACEHOLDER+"");

        Main.m.l();
		
		name=tokens[1];
		balance=Integer.parseInt(tokens[2]);
		reputation=Integer.parseInt(tokens[3]);
		highestBid=Integer.parseInt(tokens[4]);
	}
}
