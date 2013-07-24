package test.validation.NASA;

public class Task extends Thread {

	public TaskManager tm;

	public Task(Cell[] table) {
		super();
        tm=new TaskManager(table);
	}

	public void run() {

		int N = 42;

		Object v = new Object();

		tm.setValue(v,N);
		/* achieve property */
		tm.setAchieved(v,N);
	}
}
