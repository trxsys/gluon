package test.moth.Store;
/**
 * This class represents a given product (that can be selled by our store)
 * @author Vasco Pessanha
 *
 */
public class Product {
	protected String name;
	protected int weight;
	protected String date;
	protected int validity; //in months
	protected ProductType type;
	
	public Product(String name, int weight, String date, int validity, int type){
		this.name = name;
		this.weight = weight;
		this.date = date;
		this.validity = validity;
		this.type = new ProductType(type);		
	}
	
	public String toString(){
		return "Product "+name+"("+type+"), "+weight+" kg, valid for "+validity+" months after "+date;
	}
	
	public boolean isFruit(){
		return type.isFruit();
	}
	public boolean isDrink(){
		return type.isDrink();
	}
	public boolean isCereal(){
		return type.isCereal();
	}
	public boolean isCandy(){
		return type.isCandy();
	}
	public boolean isPasta(){
		return type.isPasta();
	}
	public String getName(){
		return name;
	}
}
