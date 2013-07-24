package test.validation.StoreOK;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;



public class Order {
	// Client who ordered
	protected Client client;
	// Date of the order
	protected String date;
	// Maximum number of products
	protected int max_number;	
	// Products
	protected List<Product> products;
	// Number of products of each type
	protected List<Integer> quantities;
	
	public Order(Client c, String d,int max){
		this(c,d,max,new ArrayList<Product>(),new ArrayList<Integer>());
	}
	public Order(Client c, String d, int max, List<Product> products, List<Integer> numbers){
		if(products.size() != numbers.size()){
			System.err.println("ERROR! Order has to have the same products/quantities");
			System.exit(-1);
		}
		this.client = c;
		this.date = d;
		this.max_number = max;
		this.products = products;
		this.quantities = numbers;
	}
	public void add(Product p, int n) {
		if(!products.contains(p)){
			products.add(p);
			quantities.add(n);
		}
	}
	
	public List<Pair<Product, Integer>> getList(){
		List<Pair<Product,Integer>> result = new ArrayList<Pair<Product,Integer>>();
		Iterator<Integer> i = quantities.iterator();
		for(Product p : products){
			result.add(new Pair<Product,Integer>(p, i.next()));
		}
		return result;
	}
	
	public String getDate(){
		return date;
	}
	
	public int size(){
		return products.size();
	}
	public Client getClient() {
		return client;
	}
	public boolean contains(Product p) {
		return products.contains(p);
	}
	@Override
	public Order clone(){
		return new Order(client, date, max_number, products, quantities);
	}
}
