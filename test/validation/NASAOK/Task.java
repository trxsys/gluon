package test.validation.NASAOK;

import test.common.Atomic;
import test.common.Contract;

@Contract(clauses = "setValue setAchieved;")
class TaskManager extends Thread {

	public Cell[] table;

	public TaskManager(Cell[] table) {
		super();
		this.table = table;
	}

	@Atomic
	public void setValue(Object v,int N){
		table[N].value = v;
	}

	@Atomic
	public void setAchieved(Object v, int N){
		table[N].achieved = true;
	}
}


public class Task extends Thread {

	public TaskManager tm;

	public Task(Cell[] table) {
		super();
        tm=new TaskManager(table);
	}

    @Atomic
	public void run() {

		int N = 42;

		Object v = new Object();

		tm.setValue(v,N);
		/* achieve property */
		tm.setAchieved(v,N);
	}
}
