package test.validation.VectorFail;

import java.util.Random;

public class MyThread extends Thread{
	
	Vector vector;
	public MyThread(Vector v){
		this.vector = v;
	}

	public void run() {
		while(true){
			Random r = new Random();
			int val = r.nextInt(10);
			vector.setElements(val, val*2);
			
			int max = vector.getMax();
			int min = vector.getMin();
			
			assert(max == 2*min);
//			if(max != 2*min)
//				System.err.println("ERROR: MAX ("+max+") != 2.MIN (2."+min+")");
		}
	}
}
