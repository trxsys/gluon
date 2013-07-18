package test.moth.NASAOK;

import test.common.Atomic;

public class Daemon extends Thread {
	public Cell[] table;
	public Object[] system_state;

	public Daemon(Cell[] table, Object[] system_state) {
		super();
		this.table = table;
		this.system_state = system_state;
	}

	public void run() {

		int N = 42;

		while (true) {
			tryIssueWarning(N);
		}
	}
	@Atomic
	private void tryIssueWarning(int N){
		if (table[N].achieved && system_state[N] != table[N].value)
			issueWarning();
	}
	private void issueWarning() {
		throw new RuntimeException("PANIC!!!");
	}
}