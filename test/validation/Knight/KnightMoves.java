package test.validation.Knight;

import java.awt.Point;

import test.common.Atomic;
import test.common.Contract;

@Contract(clauses = "get_solution set_solution;")
public class KnightMoves
{
	public static final int WIDTH=8;
	private Point knight;
	private Point prey;
	private int solution[][];

	public KnightMoves(Point knight, Point prey)
	{
		solution=new int[WIDTH][WIDTH];
		this.knight=knight;
		this.prey=prey;
	}

	private void reset_solution()
	{
		for (int i=0; i < WIDTH; i++)
			for (int j=0; j < WIDTH; j++)
				solution[i][j]=Integer.MAX_VALUE;
	}

	public void solve()
	{
		/* This would be much faster with breadth first search */
		reset_solution();
		Solver s=new Solver(this,(Point)knight.clone(),0);

		s.start();
		try { s.join(); } catch (Exception e) {}
	}

	public Point get_knight()
	{
		return knight;
	}

	public Point get_prey()
	{
		return prey;
	}

	@Atomic
	public int get_solution(Point p)
	{
		return solution[p.x][p.y];
	}
	
	@Atomic
	public void set_solution(Point p, int m)
	{
		solution[p.x][p.y]=m;
	}

	public int get_moves()
	{
		return get_solution(prey);
	}
	
	public void solve_correct()
	{
		reset_solution();
		new SolverCorrect(this,(Point)knight.clone(),0).go();
	}
}
