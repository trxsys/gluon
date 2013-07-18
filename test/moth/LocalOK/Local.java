package test.moth.LocalOK;

import test.common.Atomic;
import test.common.Contract;

@Contract(clauses = "getValue setValue;"
          +"setValue getValue;")
class Cell {
    private int n = 0;
    
    @Atomic
    int getValue() {
        return n;
    }
    @Atomic
    void setValue(int x) {
        n = x;
    }
}

public class Local extends Thread {
    
	static Cell x = new Cell();
    
	public static void main(String[] args) {
		new Local().start();
		new Local().start();
	}
    
    @Atomic
	public void run() {
		int tmp;
		tmp = x.getValue();
		tmp++;
		x.setValue(tmp);
	}
}
