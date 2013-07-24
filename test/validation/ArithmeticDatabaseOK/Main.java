package test.validation.ArithmeticDatabaseOK;

public class Main
{
	public static final int CLIENTS=5;
	private static Table<RPN_Expression,Integer> exp_table;
	private static Table<Integer,Integer> res_table;
	
	private static void debug_print_tables()
	{
		System.out.println("exp table:");
		System.out.println(exp_table);
		System.out.println();
		System.out.println("res table:");
		System.out.println(res_table);
	}
	
	/* The database is consistent if, and only if,
	 * 
	 * 1. For each entry in exp table there is a entry in res
	 *    with the correct expression value.
	 * 2. There is no duplicate res in res table.
	 * 3. Every key in res table is unique.
	 * 4. Every entry in res table is referenced at least by one
	 *    entry in exp table.
	 */
	private static boolean is_db_consistent()
	{
		/* check condition 1 */
		for (Pair<RPN_Expression,Integer> exp_tuple: exp_table)
			for (Pair<Integer,Integer> res_tuple: res_table)
				if (exp_tuple.v == res_tuple.k
				    && exp_tuple.k.evaluate() != res_tuple.v)
					return false;
		
		/* Check condition 2 and 3 */
		for (Pair<Integer,Integer> res1_tuple: res_table)
			for (Pair<Integer,Integer> res2_tuple: res_table)
				if (res1_tuple != res2_tuple
				    && (res1_tuple.k == res2_tuple.k
					|| res1_tuple.v == res2_tuple.v))
					return false;
		
		/* check condition 4 
		 *
		 * This condition will not be violated with our
		 * current program.
		 */
		for (Pair<Integer,Integer> res_tuple: res_table)
		{
			boolean ok=false;
			
			for (Pair<RPN_Expression,Integer> exp_tuple:
				     exp_table)
				if (res_tuple.k == exp_tuple.v)
				{
					ok=true;
					break;
				}
			
			if (!ok)
				return false;
		}
		
		return true;
	}
	
	public static void main(String[] args)
	{
		Client[] clients=new Client[CLIENTS];
		exp_table=new Table<RPN_Expression,Integer>();
		res_table=new Table<Integer,Integer>();	
		
		for (int i=0; i < CLIENTS; i++)
		{
			clients[i]=new Client(exp_table,res_table);
			clients[i].start();
		}
		
		for (Client c: clients)
			try { c.join(); } catch (Exception e) {}
		
		//debug_print_tables();
		if (!is_db_consistent())
			System.out.println("err");
	}
}