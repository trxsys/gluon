package test.moth.ArithmeticDatabase;

import java.util.List;
import java.util.Stack;
import java.util.LinkedList;

public class RPN_Expression 
	implements Comparable<RPN_Expression>
{
	private class RPN_Node
	{
		public boolean operator;
		public char op;
		public int integer;

		public RPN_Node(char op)
		{
			operator=true;
			this.op=op;
		}

		public RPN_Node(int integer)
		{
			operator=false;
			this.integer=integer;
		}
	};

	private List<RPN_Node> exp;
	
	public RPN_Expression()
	{
		exp=new LinkedList<RPN_Node>();
	}

	public void push(char op)
	{
		exp.add(new RPN_Node(op));
	}

	public void push(int integer)
	{
		exp.add(new RPN_Node(integer));
	}

	// pre: !stack.empty()
	public int evaluate()
	{
		Stack<Integer> stack=new Stack<Integer>();

		for (RPN_Node node: exp)
			if (node.operator)
			{
				int o2=stack.pop();
				int o1=stack.pop();

				switch (node.op)
				{
				case '+': stack.push(o1+o2); break;
				case '-': stack.push(o1-o2); break;
				case '*': stack.push(o1*o2); break;
				case '/': stack.push(o1/o2); break;
				case '%': stack.push(o1%o2); break;
				}
			}
			else
				stack.push(node.integer);

		return stack.pop();
	}

	public boolean isFinite()
	{
		try
		{
			evaluate();
		}
		catch (ArithmeticException e)
		{
			return false;
		}

		return true;
	}

	public int compareTo(RPN_Expression other)
	{
		return -1; // dummy
	}

	public String toString()
	{
		String r="";
		int i=0;

		for (RPN_Node node: exp)
		{
			r+=(i == 0 ? "" : ", ");

			if (node.operator)
				r+=node.op;
			else
				r+=node.integer;

			i++;
		}

		return r;
	}
}