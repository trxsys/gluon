package test.moth.AccountTest;

import java.util.Random;

public class Update extends Thread {
	public void run() {
		while(true){
			Random r = new Random();
			int n = r.nextInt();
			//Example.a.update(123);
			Main.a.update(n);
		}
	}
}