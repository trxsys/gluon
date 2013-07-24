package test.validation.AccountTestOK;

import java.util.Random;
import test.common.Atomic;

public class Update extends Thread {
	void update (Account account, int a) {
		int tmp = account.getBalance();
		tmp = tmp + a;
		account.setBalance(tmp);
	}

    @Atomic
	public void run() {
		while(true){
			Random r = new Random();
			int n = r.nextInt();

			update(Main.a,n);
		}
	}
}
