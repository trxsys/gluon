package test.validation.Store;

import java.util.Random;

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
			if(WorkerAux.hasOrders()){
				String log = WorkerAux.treatOrder();
				Store.addLog(log);
			}
			waitClients();
		}
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
