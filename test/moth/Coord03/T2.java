package test.moth.Coord03;


public class T2 extends Thread{
	Vars v;
	public T2(Vars v){
		this.v = v;
	}
	
	private void use(int a){
		// Do something...
	}

	public void run() {
		System.out.println("Estou no run do t2");
		int z = v.getX();
		use(z);
	}
}