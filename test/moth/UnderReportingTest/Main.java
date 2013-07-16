package test.moth.UnderReportingTest;

public class Main extends Thread {

	static Counter c;
	
	public static void main(String[] args) {
		c = new Counter();
		new Main().start();
		new Main().start();
	}
	
	public void run() {
		int i = c.inc(0);
		c.inc(i);
	}
}