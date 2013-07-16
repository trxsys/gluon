package test.moth.OverReportingTest;

public class MapClient extends Thread {
	static Map m;

	public static void main(String args[]) {
		m = new Map();
		new MapClient().start();
		new MapClient().start();
	}

	public void run() {
		// lazy initialization
		m.init();
		m.get(new Object());
		// ...
	}
}