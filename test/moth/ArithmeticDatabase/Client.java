package test.moth.ArithmeticDatabase;

import java.util.Random;

import test.common.Atomic;

public class Client
	extends Thread
{
	public static final int EXPS=5;
	public static final int EXP_OPS=10;
	
	Table<RPN_Expression,Integer> exp_table;
	Table<Integer,Integer> res_table;
	
	public Client(Table<RPN_Expression,Integer> exp_table,
		      Table<Integer,Integer> res_table)
	{
		this.exp_table=exp_table;
		this.res_table=res_table;
	}
	
	private RPN_Expression create_expression()
	{
		Random rnd=new Random();
		RPN_Expression exp=new RPN_Expression();
		
		/* We will not care if the expression overflows,
		 * since the results, although arithmetically wrong,
		 * are consistent.
		 */
		
		for (int i=0; i <= EXP_OPS; i++)
			exp.push(rnd.nextInt()%100);
		
		for (int i=0; i < EXP_OPS; i++)
			exp.push("+-*/%".charAt(Math.abs(rnd.nextInt())%5));
		
		return exp;
	}
	
	/* Returns -1 if no key have the pretended result */
	@Atomic
	private int get_key_by_result(int result)
	{
		for (Pair<Integer,Integer> t: res_table)
			if (t.v == result)
				return t.k;
		return -1;
	}
	
	private void insert_new_expression(RPN_Expression exp)
	{
		Integer foreign_key=null;
				
		/* If there is a entry with exp.evaluate() in
		 * exp_table use that entry instead of inserting
		 * a new one 
		 */
		if ((foreign_key = get_key_by_result(exp.evaluate())) < 0)
		{
			foreign_key = res_table.get_max_key();
			foreign_key = (foreign_key == null) ? 0 : foreign_key+1;
			res_table.insert(foreign_key,exp.evaluate());
		}
		exp_table.insert(exp,foreign_key);
	}
	
	public void run()
	{
		for (int i=0; i < EXPS; i++)
		{
			RPN_Expression exp;
			
			do
				exp=create_expression();
			while (!exp.isFinite());
			
			/*
			  System.out.println(exp.evaluate());
			*/
			
			insert_new_expression(exp);
		}
	}
}
