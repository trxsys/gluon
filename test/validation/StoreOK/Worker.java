package test.validation.StoreOK;

import java.util.List;
import java.util.Random;

import test.common.Atomic;
import test.common.Contract;

@Contract(clauses = "hasOrders treatOrder;")
class WorkerAux
{
    public static boolean hasOrders()
    {
        return Store.hasOrders();
    }

	@Atomic
    public static String treatOrder()
    {
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
		return "Sell{"+42+" Client = "+order.getClient()+" | Nº prod = "+order.size()+"\n\n";
    }
}

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

    @Atomic
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
