package test.validation.Knight;

import java.awt.Point;
import java.lang.Thread;
import java.util.Random;

public class Solver
	extends Thread
{
	private KnightMoves km;
	private Point me;
	private int moves;

	public Solver(KnightMoves km, Point me, int moves)
	{
		this.km=km;
		this.me=me;
		this.moves=moves;
	}

	private int check_and_set_solution()
	{
		Random rnd=new Random();

		/* Check if other thread has a better solution */
		if (km.get_solution(me) <= moves)
			return -1;

		if (Math.abs(rnd.nextInt())%2 == 0)
			try { Thread.sleep(50+(Math.abs(rnd.nextInt())%100)); } catch (Exception e) {}

		km.set_solution(me,moves);

		return 0;
	}

	public void run()
	{
		Point []next=new Point[8];
		int c=0;
		int m[]={1,-1};
		Solver []solver=new Solver[8];
		int s_c=0;

		if (check_and_set_solution() < 0)
			return;

		if (me.equals(km.get_prey()))
			return;

		for (int i=1; i <= 2; i++)
			for (int j=1; j <= 2; j++)
				if (i != j)
					for (int k=0; k < 2; k++)
						for (int l=0; l < 2; l++)
							next[c++]=new Point(i*m[k]+me.x,j*m[l]+me.y);

		for (Point n: next)
			if (n.x >= 0 && n.y >= 0
			    && n.x < KnightMoves.WIDTH
			    && n.y < KnightMoves.WIDTH)
			{
				//System.out.println(n.toString());
				solver[s_c]=new Solver(km,n,moves+1);
				solver[s_c++].start();
			}

		for (int i=0; i < s_c; i++)
			try { solver[i].join();} catch (Exception e) {}
	}
}