package test.moth.NASA;

import test.common.Atomic;

public class Task extends Thread {

	public Cell[] table;

	public Task(Cell[] table) {
		super();
		this.table = table;
	}

	public void run() {

		int N = 42;

		Object v = new Object();

		setValue(v,N);
		/* achieve property */
		setAchieved(v,N);
	}
	@Atomic
	private void setValue(Object v,int N){
		table[N].value = v;
	}
	@Atomic
	private void setAchieved(Object v, int N){
		table[N].achieved = true;
	}
}