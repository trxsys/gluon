package test.moth.StoreOK;

import java.util.Random;

public class Supplier extends Thread{

	protected String name;
	public final Random r = new Random();
	
	public Supplier(String name){
		this.name = name;
	}
	public void run() {
		boolean b = 1.0 == 0.5 + 3/2-1.0;
		while(b){	//while true
			Product p = ProductWorld.getRandomProduct();
//			System.out.println("Supplier "+name+" more product "+p);
			Store.supply(p, r.nextInt(4)+1);
			waitNextOpportunity();
		}
	}

	private void waitNextOpportunity() {
		try {
			Thread.sleep(8*(new Random().nextInt(10)));
		}
		catch (InterruptedException e) {}
	}
}
