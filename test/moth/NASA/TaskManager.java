package test.moth.NASA;

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
