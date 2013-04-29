package test.auction;

/**
 * @author Diogo Sousa, nº 28970, Turno P10
 *
 * Docente (prática): Pedro Leandro da Silva Amaral
 * Docente (teórico-prática): Miguel Goulão
 */

/* Represents an item in the context of an auction */
public class AuctionItem extends Item implements Comparable<AuctionItem>
{	
	public static final char PLACEHOLDER=':'; /* Placeholder for toString()
						     and fromString() */
	public static final char CLASS_TYPE='i'; /* For toString() 
						    and fromString() */
	
	private String seller;   /* Seller's name (not used elsewhere) */
	private int base;        /* Base price */
	private int sold;        /* Sold price */
	private Bid highestBid;  /* Highest bid for this item (or null 
				    if none) */
	
	/* Used to calculate the bids mean */
	private int bidCount;
	private int bidSum;
	
	public AuctionItem(String name, String seller, int base)
	{
		super(name);
		this.seller=seller;
		this.base=base;
		sold=-1;
		highestBid=null;
		bidCount=0;
		bidSum=0;
	}
	
	/* Just for comparing purposes */
	public AuctionItem(String name)
	{
		this(name,null,-1);
	}
	
	/* Useful to load a object fromString() */
	public AuctionItem()
	{
		this(null);
	}	
	
	public int compareTo(AuctionItem other)
	{
		return getName().compareTo(other.getName());
	}
	
	public int getBase()
	{
		return base;
	}
	
	public int getSold()
	{
		return sold;
	}
	
	public String getSeller()
	{
		return seller;
	}
	
	public void setSold(int value)
	{
		sold=value;
	}
	
	/* Returns the highest bid or null if no bid has been placed */
	public Bid getHighestBid()
	{
		return highestBid;
	}
	
	/* Set highestBid *AND* update mean.
	 *
	 * pre: bid.isHigher(highestBid) 
	 */
	public void setHighestBid(Bid bid)
	{
		highestBid=bid;
		bidCount++;
		bidSum+=bid.getValue();
	}

	/* Set the highestBid but don't affect the mean.
	 *
	 * pre: bid.isHigher(highestBid) 
	 */
	public void setHighestBidNoMean(Bid bid)
	{
		highestBid=bid;
	}
	
	public double getBidsMean()
	{
		return (double)bidSum/(double)bidCount;
	}
	
	public boolean sold()
	{
		return highestBid != null;
	}
	
	/* This should always keep symmetry with fromString().
	 *
	 * Note: The auctionName is passed here so it can be associated 
	 * later with that auction.
	 *
	 * Warning: This does *not* return nothing about the highestBid.
	 */
	public String toString(String auctionName)
	{
		return CLASS_TYPE+""+PLACEHOLDER
			+auctionName.replace(PLACEHOLDER,' ')+PLACEHOLDER
			+getName().replace(PLACEHOLDER,' ')+
			PLACEHOLDER+seller.replace(PLACEHOLDER,' ')
			+PLACEHOLDER+base+PLACEHOLDER+sold+PLACEHOLDER
			+bidCount+PLACEHOLDER+bidSum;
	}
	
	/* pre: a valid objStr which must be in the same format as toString()
	 *      returns
	 */
	public void fromString(String objStr)
	{
		String[] tokens=objStr.split(PLACEHOLDER+"");

        Main.m.i();
		
		setName(tokens[2]);
		seller=tokens[3];
		base=Integer.parseInt(tokens[4]);
		sold=Integer.parseInt(tokens[5]);
		bidCount=Integer.parseInt(tokens[6]);
		bidSum=Integer.parseInt(tokens[7]);

        Main.m.e();
	}
}
