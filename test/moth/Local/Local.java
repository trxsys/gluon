package test.moth.Local;

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
}
