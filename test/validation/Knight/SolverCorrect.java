package test.validation.Knight;

import java.awt.Point;

public class SolverCorrect
{
	private KnightMoves km;
	private Point me;
	private int moves;

	public SolverCorrect(KnightMoves km, Point me, int moves)
	{
		this.km=km;
		this.me=me;
		this.moves=moves;
	}

	public void go()
	{
		Point []next=new Point[8];
		int c=0;
		int m[]={1,-1};

		/* Check if other thread has a better solution */
		if (km.get_solution(me) <= moves)
			return;

		km.set_solution(me,moves);

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
				new SolverCorrect(km,n,moves+1).go();
			}
	}
}