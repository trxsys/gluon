package test.simple.auction;

/**
 * @author Diogo Sousa, nº 28970, Turno P10
 *
 * Docente (prática): Pedro Leandro da Silva Amaral
 * Docente (teórico-prática): Miguel Goulão
 */

public class Auction implements Comparable<Auction>
{
	enum Status
	{
		CREATED, OPENED, CLOSED;

		/* pre: enumStr must be valid */
		public static Status fromString(String enumStr)
		{
			if (enumStr.equals(CREATED.toString()))
				return CREATED;
			else if (enumStr.equals(OPENED.toString()))
				return OPENED;
			else
				return CLOSED;
		}
	}

	public static final char PLACEHOLDER=':'; /* Placeholder for toString()
						     and fromString() */
	public static final char CLASS_TYPE='a'; /* For toString()
						    and fromString() */

	private String name;       /* Auction's name */
	private Status status;     /* Auction's status */
	private ArraySearchable<AuctionItem> items; /* Auction's items */

	public Auction(String name)
	{
		this.name=name;
		items=new ArraySearchable<AuctionItem>();
		status=Status.CREATED;
	}

	/* Useful to load a object fromString() */
	public Auction()
	{
		this(null);
	}

	public String getName()
	{
		return name;
	}

	/* Can the auction be opened? */
	public boolean canOpen()
	{
		return status == Status.CREATED;
	}

	public boolean canTerminate()
	{
		return status == Status.OPENED;
	}

	/* pre: canTerminate() */
	public void terminate()
	{
		status=Status.CLOSED;
	}

	/* pre: canOpen() */
	public void open()
	{
		status=Status.OPENED;
	}

	public boolean isCreated()
	{
		return status == Status.CREATED;
	}

	public boolean isOpened()
	{
		return status == Status.OPENED;
	}

	public boolean isTerminated()
	{
		return status == Status.CLOSED;
	}

	public boolean canAddItem()
	{
		return isCreated();
	}

	/* pre: canAddItem() */
	public void addItem(AuctionItem item)
	{
		items.append(item);
	}

	public boolean hasItem(AuctionItem item)
	{
		return items.has(item);
	}

	public AuctionItem getItem(AuctionItem item)
	{
		return items.search(item);
	}

	public boolean canPlaceBid()
	{
		return isOpened();
	}

	public Iterator<AuctionItem> getItemIterator()
	{
		return items.getIterator();
	}

	public int numberOfItems()
	{
		return items.size();
	}

	public void sortItems()
	{
        Main.m.j();
		items.sort(new SorterQuickSort<AuctionItem>(new AuctionItemComparator()));
	}

	public int compareTo(Auction other)
        {
                return name.compareToIgnoreCase(other.name);
        }

	/* This should always keep symmetry with fromString()
	 *
	 * Warning: This does *not* return nothing about items
	 */
	public String toString()
	{
        Main.m.i();
		return CLASS_TYPE+""+PLACEHOLDER+name.replace(PLACEHOLDER,' ')
			+PLACEHOLDER+status.toString();
	}

	/* pre: a valid objStr which must be in the same format as toString()
	 *      returns
	 */
	public void fromString(String objStr)
	{
		String[] tokens=objStr.split(PLACEHOLDER+"");

        Main.m.l();

		name=tokens[1];
		status=Status.fromString(tokens[2]);
	}
}
