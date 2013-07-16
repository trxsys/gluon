package test.moth.NASA;

public class Main {

	public static void main(String[] args) {
		Cell[] table = new Cell[100];
		Object[] system_state = new Object[100];
		
		new Daemon(table, system_state).start();
		
		new Task(table).start();
		new Task(table).start();
		new Task(table).start();
		
	}
}