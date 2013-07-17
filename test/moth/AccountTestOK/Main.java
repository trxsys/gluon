package test.moth.AccountTestOK;

public class Main {
	static Account a;

	public static void main(String[] args) {
		//this will be accessed by both threadsd
		a = new Account(0, "Account name");
		new Update().start();
		new Update().start();
	}
}
