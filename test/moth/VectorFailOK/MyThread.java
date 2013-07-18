package test.moth.VectorFailOK;

import java.util.Random;
import test.common.Atomic;

public class MyThread extends Thread{
    
	Vector vector;
	public MyThread(Vector v){
		this.vector = v;
	}
    
    @Atomic
    private void foo()
    {
		Random r = new Random();
        int val = r.nextInt(10);
        vector.setElements(val, val*2);
		
        int max = vector.getMax();
        int min = vector.getMin();
		
        assert(max == 2*min);
        //			if(max != 2*min)
        //				System.err.println("ERROR: MAX ("+max+") != 2.MIN (2."+min+")");
    }
    
	public void run() {
		while(true)
            foo();
	}
}
