package test.moth.AccountTestOK;

import test.common.Atomic;
import test.common.Contract;

@Contract(clauses =
         "setBalance getBalance;"
        +"getBalance setBalance;"
        +"update getBalance;"
        +"getBalance update;")
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
	public int getBalance() {
		return balance;
	}

	@Atomic
    public void setBalance(int newValue){
		balance = newValue;
	}
}
