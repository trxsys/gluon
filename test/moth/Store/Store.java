package test.moth.Store;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import test.common.Atomic;

public class Store {
	
	// init is done?
	protected static boolean inited = false;
	// All store's products
	protected static List<StoreProduct> products;
	// Log with all sells
	protected static String log;
	// List with all orders
	protected static List<Order> orders;
	
	public static void init(){
		inited = true;
		products = new ArrayList<StoreProduct>();
		orders = new ArrayList<Order>();
		log = "";
		
		Random r = new Random();
		for(Product p : ProductWorld.getProducts()){
			int quantity = r.nextInt(5);
			StoreProduct sp = new StoreProduct(p, r.nextInt(30)+5, quantity, quantity == 0);
			products.add(sp);
		}
	}


	public static void addLog(int num_employer, Client client,
			List<Pair<Product, Integer>> iterator, int total) {
		if(!inited){
			return;
		}
		log += "Sell{"+num_employer+" Client = "+client+" | Products = "+iterator+"\n\n";		
	}
	public static void addLog(String string) {
		if(!inited){
			return;
		}
		log += string;		
	}
//	public static void checkInited(String where){
//		if(!inited){
//			System.err.println("STORE: ERROR - NOT INITED ("+where+")");
//		}
//	}

	public static int getPrice(Product p) {
		if(!inited){
			return -1;
		}
		StoreProduct sp = getStoreProduct(p);
		return sp.getPrice();
	}

	private static StoreProduct getStoreProduct(Product p) {
		if(!inited){
			return null;
		}
		for(StoreProduct sp : products){
			if(p.equals(sp.getProduct())){
				return sp;
			}
		}
		return null;
	}

//	public static boolean hasProduct(Product p, int n) {
//		checkInited("hasProduct");
//		for(StoreProduct sp : products){
//			if(sp.getProduct().equals(p)){
//				return sp.canBeSeld(n);
//			}
//		}
//		return false;
//	}

	@Atomic
	public synchronized static boolean hasOrders() {
		if(!inited){
			return false;
		}
		return orders.size()>0;
	}

	public synchronized static Order getOrder() {
		if(!inited){
			return null;
		}
		if(orders.size() > 0){
			// Sort "orders" so we can remove the oldest
			orders = sortOrders();
		}
		
		// Return the oldest order
		return orders.size() == 0? null :orders.remove(0);
	}


	private static List<Order> sortOrders() {
		int size = orders.size();
		List<Order> result = new ArrayList<Order>();
		if(size == 0){
			return result;
		}		
		boolean[] visited = new boolean[size];
		Order[] aOrders = toArray();
		
		// We get always the oldest order and put it first
		for(int i = 0; i < size; i++){
			Order newest = aOrders[0];
			for(int j = 1; j < size ; j++){
				if(!visited[j] && aOrders[j].getDate().compareTo(newest.getDate())<0){
					newest = aOrders[j];
					visited[j] = true;
				}
			}
			aOrders[i] = newest;
		}
		
		return result;
	}


	private static Order[] toArray() {
		Order[] result = new Order[orders.size()];
		int count = 0;
		for(Order o : orders){
			result[count] = o;
			count++;
		}
		return result;
	}


	public static void sell(Product p, int n) {
		if(!inited){
			return;
		}
		StoreProduct sp = getStoreProduct(p);
		if(sp.isSoldOut() || sp.getNumberUnits() < n){
//			System.err.println("ERROR: Store - This product is not for sale or has no enough units");
		}
		sp.decProduct(n);
	}

	public static void buyProducts(Order order) {
		if(!inited){
			return;
		}
		orders.add(order);
	}


	public static void supply(Product p, int aux) {
		if(!inited){
			return;
		}
		StoreProduct sp = getStoreProduct(p);
		if(sp == null){
//			System.err.println("Store : shouldn't happen (Product doesn't exist)");
		}
		if(!sp.isConsistent()){
//			System.err.println("Store : ERROR! Inconsistency between 'soldOut' and number");
		}
		sp.incProduct(aux);
	}
}
