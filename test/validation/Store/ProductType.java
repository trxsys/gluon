package test.validation.Store;
/**
 * This Class represents the possible types of products
 *
 * @author Vasco Pessanha
 *
 */
public class ProductType {

	public static final int fruitType = 1;
	public static final int drinksType = 2;
	public static final int cerealsType = 3;
	public static final int candyType = 4;
	public static final int pastaType = 5;

	private final String fruitString = "Fruits";
	private final String drinksString = "Drinks";
	private final String cerealsString = "Cereals";
	private final String candyString = "Candies";
	private final String pastaString = "Pastas";

	public static final ProductType fruits = new ProductType(fruitType);
	public static final ProductType drinks = new ProductType(drinksType);
	public static final ProductType cereals = new ProductType(cerealsType);
	public static final ProductType candies = new ProductType(candyType);
	public static final ProductType pastas = new ProductType(pastaType);


	protected int type;
	public ProductType(int type){
		isValid(type);
		this.type = type;
	}
	private void isValid(int inType) {
		if(inType != fruitType && inType != drinksType &&
			inType != cerealsType && inType != candyType && inType != pastaType){

			System.err.println("ERROR (ProductType) - tried to introduced an invalid type ("+inType+")");
			System.exit(-1);
		}
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + type;
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
		ProductType other = (ProductType) obj;
		if (type != other.type)
			return false;
		return true;
	}
	public String toString(){
		if(this.equals(fruits))
			return fruitString;
		else if(this.equals(drinks))
			return drinksString;
		else if(this.equals(cereals))
			return cerealsString;
		else if(this.equals(candies))
			return candyString;
		else if(this.equals(pastas))
			return pastaString;
		else
			return "WRONG TYPE";
	}
	public boolean isFruit(){
		return type == fruitType;
	}
	public boolean isDrink(){
		return type == drinksType;
	}
	public boolean isCereal(){
		return type == cerealsType;
	}
	public boolean isCandy(){
		return type == candyType;
	}
	public boolean isPasta(){
		return type == pastaType;
	}
}
