package test.validation.Store;

public class Main {

	public static void main(String[] args) {
		ProductWorld.init();
		Store.init();
		
		Client c1 = new Client("Vasco", "Estrada bla bla bla", 23, 1, 200);
		Client c2 = new Client("Joao", "Rua bla bla bla", 22, 2, 400);
		Client c3 = new Client("Pedro", "Avenida bla bla bla", 41, 3, 450);
		
		Worker w1 = new Worker("worker1", "blabla1",19,1,1000,5);
		Worker w2 = new Worker("worker2", "blabla2",22,2,1500,7);
		Worker w3 = new Worker("worker3", "blabla3",31,3,1200,6);
		
		Supplier s1 = new Supplier("Supplier1");
		Supplier s2 = new Supplier("Supplier2");
		Supplier s3 = new Supplier("Supplier3");
		
		//Start Clients
		c1.start();
		c2.start();
		c3.start();
		
		//Start Workers
		w1.start();
		w2.start();
		w3.start();
		
		//Start Supliers
		s1.start();
		s2.start();
		s3.start();
	}
}
