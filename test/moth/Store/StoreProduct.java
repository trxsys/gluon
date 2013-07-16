package test.moth.Store;

import test.common.Atomic;

/**
 * This class represents a Store's product.
 * 
 * @author Vasco Pessanha
 */
public class StoreProduct {
	// store's product represented by this class
	protected Product product;
	// number of units of that product
	protected int n;
	// (is this product for sale?)
	protected boolean soldOut;
	// Product's price
	protected int price;
	
	public StoreProduct(Product p, int price){
		this(p,price,1,true);		
	}
	
	public StoreProduct(Product p, int price, int numberUnits, boolean soldOut){
		this.product = p;
		this.n = numberUnits;
		this.soldOut = soldOut;
		this.price = price;
	}
	
	public int hashCodeIgnoreCase() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((product == null) ? 0 : product.hashCode());
		return result;
	}

	public boolean equalsIgnoreCase(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		StoreProduct other = (StoreProduct) obj;
		if (product == null) {
			if (other.product != null)
				return false;
		} else if (!product.equals(other.product))
			return false;
		return true;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (soldOut ? 1231 : 1237);
		result = prime * result + n;
		result = prime * result + ((product == null) ? 0 : product.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		StoreProduct other = (StoreProduct) obj;
		if (soldOut != other.soldOut)
			return false;
		if (n != other.n)
			return false;
		if (product == null) {
			if (other.product != null)
				return false;
		} else if (!product.equals(other.product))
			return false;
		return true;
	}
	
	@Atomic
	public synchronized boolean isConsistent(){
		return (soldOut && n == 0) || (n > 0 && !soldOut);
	}
	
	public void incProduct(int aux){
		incNumber(aux);
		// Someone can read this inconsistent state
		// ( We have product but it is sold out)
		setSoldOutA(false);
	}
	
	public void decProduct(int aux){
		decNumber(aux);
		// Someone can read this inconsistent state
		// ( We have product but it is sold out)
		setSoldOut(n == 0);
	}

	@Atomic
	public synchronized void incNumber(int aux) {
		n += aux;
	}	
	
	public synchronized void decNumber(int n2) {
		n = n > n2? n - n2 : 0;		
	}
	
	private synchronized void setSoldOut(boolean soldOut) {
		this.soldOut = soldOut;
	}
	
	@Atomic
	private synchronized void setSoldOutA(boolean soldOut) {
		this.soldOut = soldOut;
	}

	
	@Override
	public String toString(){
		String result = "StoreProduct "+product.getName()+" - " + n;
		if(soldOut)
			return result + "(Sold out)";
		return result;
	}
	
	public int getPrice(){
		return price;
	}

	public Product getProduct() {
		return product;
	}
	
	public boolean isSoldOut(){
		return soldOut;
	}
	public int getNumberUnits(){
		return n;
	}
}
