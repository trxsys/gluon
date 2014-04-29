package test.validation.Store;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;


public class Client extends Thread implements Person {
	// Persons attributes
	protected String name;
	protected String address;
	protected int age;

	// Client attributes
	protected int num_client;
	protected int money;

	public Client(String name, String address, int age, int num_client,int money){
		this.name = name;
		this.age = age;
		this.address = address;
		this.num_client = num_client;
		this.money = money;
	}

	public String toString(){
		return "Client "+name+ ", "+age+"years old, from "+address+"(nº = "+num_client+")";
	}

	public void run(){
		int tres = 3;
		boolean b = tres == 3;
		while(b){	//while true
//			System.out.println("Client "+name+" lets buy?");

			Random r = new Random();

			DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			Order order = new Order(this, dateFormat.format(new Date()), 5);

			int num_products = r.nextInt(3)+1;
			for(int i = 0; i < num_products; i++){
				boolean found = false;
				Product p = null;
				while(!found){
					// get a random product
					p = ProductWorld.getRandomProduct();
					int n = r.nextInt(5);//max 5 of each
//					if(Store.hasProduct(p,n) && !order.contains(p)){
						order.add(p,n);
						found = true;
//					}
				}
			}
			// Buy product
			Store.buyProducts(order);

			waitAbit();
		}
	}

	private void waitAbit() {
		try {
			Thread.sleep(7*(new Random().nextInt(10)));
		} catch (InterruptedException e) {}
	}

	@Override
	public int getAge() {
		return age;
	}

	@Override
	public String getAddress() {
		return address;
	}

	@Override
	public String getPersonName() {
		return name;
	}
}
