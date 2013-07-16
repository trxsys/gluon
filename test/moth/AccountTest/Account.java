package test.moth.AccountTest;

import test.common.Atomic;

public class Account {

	protected int balance;
	protected String name;
	
	public Account(){
		this.balance = 0;
		this.name = "";
	}
	
	public Account(int balance, String name){
		this.balance = balance;
		this.name = name;
	}
	
	public String getName(){
		return name;
	}
	
	@Atomic
	int getBalance() {
		return balance;
	}

	@Atomic
	private void setBalance(int newValue){
		balance = newValue;
	}
	
	void update (int a) {
		int tmp = getBalance();
		tmp = tmp + a;
		setBalance(tmp);
	}
}