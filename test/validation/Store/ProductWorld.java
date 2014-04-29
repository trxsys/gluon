package test.validation.Store;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class ProductWorld {
	//fruit
	protected static Product apple;
	protected static Product banana;
	protected static Product strawberry;
	protected static Product pineapple;
	protected static Product fig;

	//pasta
	protected static Product spaguetti;
	protected static Product noodles;

	//cereals
	protected static Product chocapic;
	protected static Product chococrispies;
	protected static Product snacks;

	//candies
	protected static Product smarties;
	protected static Product kitkat;
	protected static Product mms;
	protected static Product snickers;


	//drinks
	protected static Product martini;
	protected static Product vodka;
	protected static Product cocacola;
	protected static Product icetea;

	public static void init() {
		apple = new Product("Apple", 30 , "23/01/2011", 1, 1 /*fruit*/);
		banana = new Product("banana", 25 , "25/01/2011", 3, 1 /*fruit*/);
		strawberry = new Product("StrawBerry", 21 , "13/01/2011", 1, 1 /*fruit*/);
		pineapple = new Product("Pineapple", 22 , "23/05/2011", 2, 1 /*fruit*/);
		fig = new Product("Fig", 15 , "3/02/2011", 3, 1 /*fruit*/);

		martini = new Product("martini", 30 , "23/01/2011", 1, 2 /*drinks*/);
		vodka = new Product("vodka", 30 , "23/01/2011", 1, 2 /*drinks*/);
		cocacola = new Product("cocacola", 30 , "23/01/2011", 1, 2 /*drinks*/);
		icetea = new Product("icetea", 30 , "23/01/2011", 1, 2 /*drinks*/);

		chocapic = new Product("chocapic", 30 , "23/01/2011", 1, 3 /*cereals*/);
		chococrispies = new Product("chococrispies", 30 , "23/01/2011", 1,3 /*cereals*/);
		snacks = new Product("snacks", 30 , "23/01/2011", 1, 3 /*cereals*/);

		smarties = new Product("smarties", 30 , "23/01/2011", 1, 4 /*candies*/);
		kitkat = new Product("kitkat", 30 , "23/01/2011", 1, 4 /*candies*/);
		mms = new Product("mms", 30 , "23/01/2011", 1, 4 /*candies*/);
		snickers = new Product("snickers", 30 , "23/01/2011", 1, 4 /*candies*/);

		spaguetti = new Product("Spaguetti", 30 , "23/01/2011", 1, 5 /*pasta*/);
		noodles = new Product("Noodles", 25 , "25/01/2011", 3, 5 /*pasta*/);
	}

	public static Product getRandomProduct(){
		Random r = new Random();
		int num = r.nextInt(17);

		switch(num){
			case 0:
				return apple;
			case 1:
				return banana;
			case 2:
				return strawberry;
			case 3:
				return pineapple;
			case 4:
				return fig;
			case 5:
				return spaguetti;
			case 6:
				return noodles ;
			case 7:
				return chocapic;
			case 8:
				return chococrispies;
			case 9:
				return snacks;
			case 10:
				return smarties;
			case 11:
				return kitkat;
			case 12:
				return mms;
			case 13:
				return snickers;
			case 14:
				return martini;
			case 15:
				return vodka;
			case 16:
				return cocacola;
			default:
				return icetea;
		}
	}

	public static List<Product> getProducts() {
		List<Product> result = new ArrayList<Product>();
		result.add(apple);result.add(banana);
		result.add(strawberry);result.add(pineapple);
		result.add(fig);result.add(spaguetti);
		result.add(noodles);result.add(chocapic);
		result.add(chococrispies);result.add(snacks);
		result.add(smarties);result.add(kitkat);
		result.add(mms);result.add(snickers);
		result.add(martini);result.add(vodka);
		result.add(cocacola);result.add(icetea);

		return result;
	}
}
