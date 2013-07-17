package test.moth.AccountTest;

import java.util.Random;

public class Update extends Thread {
	void update (Account account, int a) {
		int tmp = account.getBalance();
		tmp = tmp + a;
		account.setBalance(tmp);
	}

	public void run() {
		while(true){
			Random r = new Random();
			int n = r.nextInt();

			update(Main.a,n);
		}
	}
}
