package test.moth.KnightOK;

import java.awt.Point;
import java.util.Random;


public class Main
{
	public static void main(String[] args)
	{
		Random rnd=new Random();
		Point knight=new Point(Math.abs(rnd.nextInt())%KnightMoves.WIDTH,Math.abs(rnd.nextInt())%KnightMoves.WIDTH);
		Point prey=new Point(Math.abs(rnd.nextInt())%KnightMoves.WIDTH,Math.abs(rnd.nextInt())%KnightMoves.WIDTH);

		KnightMoves km=new KnightMoves(knight,prey);
		int s;
		int cs;

		System.out.println("KnightOK @ "+knight.toString());
		System.out.println("Prey @ "+prey.toString());

		km.solve();

		System.out.println("Moves: "+(s=km.get_moves()));

		km.solve_correct();

		System.out.println("Correct Moves: "+(cs=km.get_moves()));

		if (cs != s)
			System.out.println("err");
	}
}