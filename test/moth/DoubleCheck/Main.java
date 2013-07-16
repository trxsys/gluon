package test.moth.DoubleCheck;

import test.common.Atomic;

public class Main extends Thread {

	static Cell shared;

	public static void main(String[] args) {
		new Main().start();
		new Main().start();
	}

	public void run() {
		do_transaction();
	}

	public void do_transaction() {
		int value, fdata;
		boolean done = false;
		while (!done) {			
			value = getSharedField();
			fdata = f(value); // long computation
			updateSharedField(value, fdata);
		}
	}
	@Atomic
	private int getSharedField(){
		return shared.field;	
	}
	@Atomic
	private boolean updateSharedField(int value, int fdata){
		if (value == shared.field) {
			shared.field = fdata;
			// The usage of the locally computed fdata is safe because
			// the shared value is the same as during the computation.
			// Our algorithm and previous atomicity-based approaches
			// report an error (false positive).
			return true;
		}
		return false;
	}
	
	int f(int x) {
		return x * x;
	}

	static class Cell {
		public int field;
	}
}