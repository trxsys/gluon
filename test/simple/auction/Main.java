package test.simple.auction;

/**
 * @author Diogo Sousa, nº 28970, Turno P10
 *
 * Docente (prática): Pedro Leandro da Silva Amaral
 * Docente (teórico-prática): Miguel Goulão
 */

import java.util.Scanner;
import java.io.PrintWriter;
import java.io.FileReader;
import java.io.File;

public class Main
{
    public static Module m=new Module();

	private static final String PROMPT="> ";
	
	private static final String CMD_ADD_CUSTOMER="AC";
	private static final String CMD_REPORT_CUSTOMERS="RC";
	private static final String CMD_CREDIT_CUSTOMER="CC";
	private static final String CMD_CREATE_AUCTION="CA";
	private static final String CMD_OPEN_AUCTION="OA";
	private static final String CMD_TERMINATE_AUCTION="TA";
	private static final String CMD_ADD_ITEM="AI";
	private static final String CMD_PLACE_BID="PB";
	private static final String CMD_REPORT_AUCTION="RA";
	private static final String CMD_HELP="H";
	private static final String CMD_QUIT="Q";
	
	private static final String MESSAGE_OK="OK"
		+System.getProperty("line.separator");
	
	/* Comment in a possible script file */
	private static final String COMMENT_PREFIX="#";
	
	private static final String SAVE_FILEPATH="auctions.txt";
	
	private static ArraySearchable<Customer> customers;
	private static ArraySearchable<Auction> auctions;
	
	private static void interpreterAddCustomer(Scanner in)
	{
		String name;
		int reputation;
		
		name=in.nextLine().trim();
		reputation=in.nextInt();
		
		in.nextLine();
		
		if (customers.has(new Customer(name)))
			System.err.println("Customer "+name+" already created");
		else
		{
			customers.append(new Customer(name,reputation));
			System.out.print(MESSAGE_OK);
		}
	}
	
	private static void reportCustomers()
	{
		if (customers.size() == 0)
			System.out.println("There is no customer registered");
		else 
			System.out.println("Name\tBalance\tReputation\t"+
					   "Highest bid");
		
		for (Iterator<Customer> it=customers.getIterator();
		     !it.end();
		     it.next())
		{
			Customer customer=it.get();
            Main.m.c();			
			System.out.println(customer.getName()+"\t"
					   +customer.getBalance()+"\t"
					   +customer.getReputation()+"\t\t"
					   +customer.getHighestBid());
		}
	}
	
	private static void interpreterCreditCustomer(Scanner in)
	{
		String name;
		int credit;
		Customer customer;
		
		name=in.nextLine().trim();
		credit=in.nextInt();
		
		in.nextLine();
		
		customer=customers.search(new Customer(name));
		
		if (customer == null)
			System.err.println("Customer "+name+" not registered");
		else
		{
			customer.credit(credit);
			System.out.print(MESSAGE_OK);
		}
	}
	
	private static void interpreterCreateAuction(Scanner in)
	{
		String name;
		
		name=in.nextLine().trim();
		
		if (auctions.has(new Auction(name)))
			System.err.println("Auction "+name+" already created");
		else
		{
			auctions.append(new Auction(name));
			System.out.print(MESSAGE_OK);
		}
	}
	
	private static void interpreterOpenAuction(Scanner in)
	{
		String name;
		Auction auction;
		
		name=in.nextLine().trim();
		
		auction=auctions.search(new Auction(name));
		
		if (auction == null || !auction.canOpen())
			System.err.println("Auction "+name+" cannot be opened");
		else
		{
			auction.open();
			System.out.print(MESSAGE_OK);
		}
	}
	
	private static void interpreterTerminateAuction(Scanner in)
	{
		String name;
		Auction auction;
		
		name=in.nextLine().trim();
		
		auction=auctions.search(new Auction(name));
		
		if (auction == null || !auction.canTerminate())
			System.err.println("Auction "+name+" not open");
		else
		{
			auction.terminate();
			System.out.print(MESSAGE_OK);
		}
	}
	
	private static void interpreterAddItem(Scanner in)
	{
		Auction auction;
		String auctionName;
		String itemName;
		String seller;
		int basePrice;
		
		auctionName=in.nextLine().trim();
		itemName=in.nextLine().trim();
		seller=in.nextLine().trim();
		basePrice=in.nextInt();
		in.nextLine();
		
		auction=auctions.search(new Auction(auctionName));
		
		if (auction == null)
			System.err.println("Auction "+auctionName
					   +" not created");
		else if (!auction.canAddItem())
			System.err.println("Cannot insert item in running "
					   +"or terminated auction");
		else if (auction.hasItem(new AuctionItem(itemName)))
			System.err.println("Item already in this auction");
		else 
		{
			auction.addItem(new AuctionItem(itemName,seller,
							basePrice));
			System.out.print(MESSAGE_OK);			
		}

        Main.m.d();
	}	
	
	private static void interpreterPlaceBid(Scanner in)
	{
		Customer customer;
		Auction auction;
		AuctionItem item;
		String auctionName;
		String itemName;
		String customerName;
		int bidValue;
		
		auctionName=in.nextLine().trim();
		itemName=in.nextLine().trim();
		customerName=in.nextLine().trim();
		bidValue=in.nextInt();
		in.nextLine();
		
		auction=auctions.search(new Auction(auctionName));
		customer=customers.search(new Customer(customerName));
		item=auction.getItem(new AuctionItem(itemName));
		
		if (auction == null || !auction.canPlaceBid())
			System.err.println("Auction "+auctionName
					   +" not created or open");
		else if (item == null)
			System.err.println("Item "+itemName+" "
					   +"not for sale in this auction");
		else if (customer == null)
			System.err.println("Customer "+customerName+" "
					   +"not registered");
		else if (!customer.isCredible())
			System.err.println("Customer "+customerName+" does "
					   +"not have enough credibility");
		else if (customer.getBalance() < bidValue)
		{
			customer.reputationPenalty();
			System.err.println("Customer "+customerName+" does "
					   +"not have enough credit");
		}
		else if (item.getHighestBid() != null
			 && !item.getHighestBid().isHigher(new Bid(bidValue)))
			System.err.println("Customer "+customerName+" "
					   +"does not cover the current "
					   +"bidding of "
					   +item.getHighestBid().getValue()+" "
					   +"euros");
		else if (bidValue < item.getBase())
			System.err.println("Customer "+customerName
					   +" does not cover the base"
					   +" bidding price of "
					   +item.getBase()+" "
					   +"euros");
		else
		{
			Bid bid=new Bid(bidValue,customer);

            Main.m.e();			
			/* Returns the money to the previous highest bidder */
			if (item.getHighestBid() != null)
				item.getHighestBid().getCustomer().credit(item.getHighestBid().getValue());
			
			customer.pay(bidValue);
			customer.setHighestBidIfHigher(bidValue);
			
			item.setHighestBid(bid);
			
			System.out.print(MESSAGE_OK);
		}
	}
	
	/* pre: auction.isTerminated() */
	private static void reportAuction(Auction auction)
	{
		if (auction.numberOfItems() == 0)
			System.out.println("There is no items in auction "
					   +auction.getName());
		else 
			System.out.println("Name\tSeller\tBuyer\t"+
					   "Sold price\tBids average");
		
		auction.sortItems();
		
		for (Iterator<AuctionItem> it=auction.getItemIterator();
		     !it.end();
		     it.next())
		{
			AuctionItem item=it.get();
			
			System.out.print(item.getName()+"\t"
					 +item.getSeller());

            Main.m.f();
			
			if (item.sold())
            {
				System.out.print("\t"
						 +item.getHighestBid().getCustomer().getName()+"\t"
						 +item.getHighestBid().getValue()+"\t\t"
						 +item.getBidsMean());
                Main.m.d();
			}

			System.out.println();
		}
		
	}
	
	private static void interpreterReportAuction(Scanner in)
	{
		String auctionName;
		Auction auction;
		
		auctionName=in.nextLine().trim();
		
		auction=auctions.search(new Auction(auctionName));
		
		if (auction == null || !auction.isTerminated())
			System.err.println("Auction "+auctionName+" "
					   +"not created or not closed");
		else
			reportAuction(auction);
	}
	
	private static void printHelp()
	{
		System.out.println("Commands:");
		System.out.println();
		System.out.println("  "+CMD_ADD_CUSTOMER+", Add customer "
				   +"(name, credit)");
		System.out.println("  "+CMD_REPORT_CUSTOMERS
				   +", Report customer");
		System.out.println("  "+CMD_CREDIT_CUSTOMER
				   +", Credit customer (name, credit)");
		System.out.println("  "+CMD_CREATE_AUCTION
				   +", Create auction (name)");
		System.out.println("  "+CMD_OPEN_AUCTION
				   +", Open auction (name)");
		System.out.println("  "+CMD_TERMINATE_AUCTION
				   +", Terminate auction (name)");
		System.out.println("  "+CMD_ADD_ITEM
				   +", Add item to an auction "
				   +"(auction name, item name, sellers name, "
				   +"base price)");
		System.out.println("  "+CMD_PLACE_BID
				   +", Place a bid on an item of an auction "
				   +"(auction name, item name,");
		
		/* To make sure the new line is align */
		for (int i=0; i < CMD_PLACE_BID.length()+4; i++)
			System.out.print(" ");
		System.out.println("customer name, bid)");
		
		System.out.println("  "+CMD_REPORT_AUCTION+", Report auction "+
				   "(name)");
		System.out.println("  "+CMD_HELP+", Print this message");
		System.out.println("  "+CMD_QUIT+", Quit the program");
	}
	
	private static void interpreter()
		throws Exception
	{
		String cmd;
		Scanner in=new Scanner(System.in);
		
		do
		{
			System.out.print(PROMPT);
			
			/* This should be checked to prevent a exception if the
			 * input stream is not stdin (in case of a pipe, for
			 * example) or if the user inputs an EOF (C-D in 
			 * unix-line systems).
			 */
			if (in.hasNextLine())
			{
				cmd=in.nextLine().trim();
				cmd=cmd.toUpperCase();
			}
			else
				cmd=CMD_QUIT;

            Main.m.h();
			
			if (cmd.equals(CMD_HELP))
				printHelp();
			else if (cmd.equals(CMD_ADD_CUSTOMER))
				interpreterAddCustomer(in);
			else if (cmd.equals(CMD_REPORT_CUSTOMERS))
				reportCustomers();
			else if (cmd.equals(CMD_CREDIT_CUSTOMER))
				interpreterCreditCustomer(in);
			else if (cmd.equals(CMD_CREATE_AUCTION))
				interpreterCreateAuction(in);
			else if (cmd.equals(CMD_OPEN_AUCTION))
				interpreterOpenAuction(in);
			else if (cmd.equals(CMD_TERMINATE_AUCTION))
				interpreterTerminateAuction(in);
			else if (cmd.equals(CMD_ADD_ITEM))
				interpreterAddItem(in);
			else if (cmd.equals(CMD_PLACE_BID))
				interpreterPlaceBid(in);
			else if (cmd.equals(CMD_REPORT_AUCTION))
				interpreterReportAuction(in);
			else if (cmd.equals(CMD_QUIT))
				save(SAVE_FILEPATH);
			else if (cmd.length() > 0
				 && !cmd.startsWith(COMMENT_PREFIX))
				System.err.println(cmd+": Unknown command");
		} while (!cmd.equals(CMD_QUIT));
		
		in.close();
	}
	
	private static void init()
	{
		customers=new ArraySearchable<Customer>();
		auctions=new ArraySearchable<Auction>();
	}
	
	private static void saveCustomers(PrintWriter o)
	{
		for (Iterator<Customer> it=customers.getIterator();
		     !it.end();
		     it.next())
			o.println(it.get().toString());
	}
	
	private static void saveAuctionItems(PrintWriter o, Auction auction)
	{
		for (Iterator<AuctionItem> it=auction.getItemIterator();
		     !it.end();
		     it.next())
		{
			AuctionItem item=it.get();
			Bid b=item.getHighestBid();
			
			o.println(item.toString(auction.getName()));

            Main.m.e();
			
			if (b != null)
				o.println(b.toString(auction.getName(),
						     item.getName()));
		}
	}
	
	private static void saveAuctions(PrintWriter o)
	{
		for (Iterator<Auction> it=auctions.getIterator();
		     !it.end();
		     it.next())
		{
			Auction auction=it.get();
			
			o.println(auction.toString());
			
			saveAuctionItems(o,auction);
		}
	}
	
	private static void save(String filepath)
		throws Exception
	{
		PrintWriter o=new PrintWriter(filepath);
		
		/* Warning: don't change this order, see loadBid() */ 
		saveCustomers(o);
		saveAuctions(o);
		
		o.close();
	}
	
	private static void loadCustomer(String objStr)
	{
		Customer c=new Customer();
		
		c.fromString(objStr);
		customers.append(c);
	}
	
	private static void loadAuction(String objStr)
	{
		Auction a=new Auction();
		
		a.fromString(objStr);
		auctions.append(a);
	}
	
	private static void loadAuctionItem(String objStr)
	{
		AuctionItem i=new AuctionItem();
		Auction a;
		String auctionName=objStr.split(AuctionItem.PLACEHOLDER+"")[1];
		
		i.fromString(objStr);
		a=auctions.search(new Auction(auctionName));
		
		/* As said in load() we trust the input, and if we do 
		 * the previous search will always succeed, because of 
		 * the order everything is saved
		 */
		a.addItem(i);
	}
	
	private static void loadBid(String objStr)
	{
		Bid b=new Bid();
		Auction a;
		AuctionItem i;
		String auctionName=objStr.split(Bid.PLACEHOLDER+"")[1];
		String itemName=objStr.split(Bid.PLACEHOLDER+"")[2];
		String costumerName=objStr.split(Bid.PLACEHOLDER+"")[3];
		
		b.fromString(objStr);
		
		/* The comment in loadAuctionItem() also holds here */
		
		a=auctions.search(new Auction(auctionName));
		i=a.getItem(new AuctionItem(itemName));
		
		/* The customers are also saved before everything else */
		
		b.setCustomer(customers.search(new Customer(costumerName)));
		
		i.setHighestBidNoMean(b);
	}
	
	private static void load(String filepath)
		throws Exception
	{
		FileReader fIn=new FileReader(filepath);
		Scanner in=new Scanner(fIn);
		
		while (in.hasNextLine())
		{
			String line=in.nextLine();
			
			/* We trust the file was created by this program
			 * and have not been changed, so we don't check
			 * here if the line is empty (in that case we could 
			 * not access index 0, and the program will terminate
			 * with a exception instead of a nice error message)
			 * nor if the data is consistent.
			 *
			 * Of course this is a bug, but we also don't check,
			 * if the file opening goes ok, because we didn't lean
			 * try catch in IP, so I guess this is ok.
			 */
			switch (line.charAt(0))
			{
			case Customer.CLASS_TYPE: loadCustomer(line); break;
			case Auction.CLASS_TYPE: loadAuction(line); break;
			case AuctionItem.CLASS_TYPE: loadAuctionItem(line); break;
			case Bid.CLASS_TYPE: loadBid(line); break;
			}
		}
		
		in.close();
		fIn.close();
	}
	
	public static void main(String[] args)
		throws Exception
	{
		File savedFile=new File(SAVE_FILEPATH);
		
		init();
		
		if (savedFile.isFile())
			load(SAVE_FILEPATH);

        Main.m.g();
		
		interpreter();

        Main.m.h();
	}
}
