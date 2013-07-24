package test.validation.Coord03OK;



public class T4 extends Thread{
	
	/*
	 * x4 = c.getX();
	 * use(x4);
	 * d4 = c.getXY();
	 * x4 = d4.getX();
	 * y4 = d4.getY();
	 * use(x4,y4);
	 */
	Vars v;
	public T4(Vars v){
		this.v = v;
	}

	@Override
	public void run() {
		System.out.println("Estou no run do t4");
		int x4 = v.getX();
		use(x4);
		Pair<Integer,Integer> v2 = v.getXY();
		x4 = v2.getFirst();
		int y4 = v2.getSecond();
		use(x4,y4);
	}

	private void use(int x4, int y4) {
		// Do something...
	}

	private void use(int x4) {
		// Do something...
	}
}