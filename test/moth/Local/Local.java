package test.moth.Local;

import test.common.Atomic;

public class Local extends Thread {

	static Cell x = new Cell();

	public static void main(String[] args) {
		new Local().start();
		new Local().start();
	}

	public void run() {
		int tmp;
		tmp = x.getValue();
		tmp++;
		x.setValue(tmp);
	}
	
	static class Cell {
		int n = 0;

		@Atomic
		int getValue() {
			return n;
		}
		@Atomic
		void setValue(int x) {
			n = x;
		}
	}
}