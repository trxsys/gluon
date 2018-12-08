package test.validation.AccountTest;

import java.util.Random;

public class Update extends Thread {
    void update (Account account, int a) {
		 synchronized (this) {
			 int tmp = account.getBalance();
			 tmp = tmp + a;
			 account.setBalance(tmp);
		 }
	}

    void update2(Account account, int a) {
        synchronized (this) {
            int tmp = account.getBalance();
            tmp = tmp + a;
            account.setBalance(tmp);
        }
    }

	public void run() {
		while(true){
			Random r = new Random();
			int n = r.nextInt();

			update(Main.a,n);

            update2(Main.a,n);
		}
	}
}
