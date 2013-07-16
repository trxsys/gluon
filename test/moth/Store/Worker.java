package test.moth.Store;

import java.util.List;
import java.util.Random;

import test.common.Atomic;


public class Worker extends Thread implements Person{

	//Person attributes
	protected String name;
	protected String address;
	protected int age;
	
	//Worker attributes
	protected int num_employer;
	protected int salary;
	protected int yearsOfWork;
	
	
	public Worker(String name, String address, int age, int num_employer, int salary, int yearsOfWork){
		this.name = name;
		this.address = address;
		this.age = age;
		this.num_employer = num_employer;
		this.salary = salary;
		this.yearsOfWork = yearsOfWork;
	}

	public void run() {
		int tres = 3;
		boolean b = tres == 3;
		while(b){	//while true
//			System.out.println("Worker "+name+" has orders?");
			if(Store.hasOrders()){
				String log = treateOrder();
				Store.addLog(log);
			}
			waitClients();
		}
	}

	@Atomic
	private String treateOrder() {
		Order order = Store.getOrder();
		int total = 0;
		List<Pair<Product,Integer>> list = order.getList();
		while(list.size() > 0){
			Pair<Product, Integer> pair = list.remove(0);
			Product p = pair.getFirst();
			int n = pair.getSecond();
			total += Store.getPrice(p);
			Store.sell(p,n);
		}
		return "Sell{"+num_employer+" Client = "+order.getClient()+" | Nº prod = "+order.size()+"\n\n";
	}

	/**
	 * Wait for the entrance of more clients..
	 */
	private void waitClients() {
		try {
			Thread.sleep((new Random()).nextInt(10)*5);
		} catch (InterruptedException e) {
			// Wake up... more clients?
		}
	
	}

	public String getPersonName() {
		return name;
	}

	@Override
	public int getAge() {
		return age;
	}

	@Override
	public String getAddress() {
		return address;
	}
}
