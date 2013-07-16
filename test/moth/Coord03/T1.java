package test.moth.Coord03;

public class T1 extends Thread{
	
   /*
	* d1 = new Coord(1,2);
	* c.setXY(d1);
	*/
	
	Vars v;
	public T1(Vars v){
		this.v = v;
	}

	public void run() {
		System.out.println("Estou no run do t1");
		v.setXY(1,2);
	}

}