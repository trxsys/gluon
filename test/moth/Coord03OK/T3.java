package test.moth.Coord03OK;

import test.common.Atomic;

public class T3 extends Thread{
	
	/*
	 * x3 = c.getX();
	 * y3 = c.getY();
	 * use(x3,y3);
	 */
	Vars v;
	public T3(Vars v){
		this.v = v;
	}

    @Atomic
	public void run() {
		System.out.println("Estou no run do t3");
		int x3 = v.getX();
		int y3 = v.getY();
		use(x3,y3);
	}

	private void use(int x3, int y3) {
		// Do something...
	}
}
