package test.moth.Coord04;

import test.common.Atomic;

public class CoordMain extends Thread {

	Coord coord;

	public static void main(String[] args) {
		Coord c = new Coord();
		new CoordMain(c).start();
		new CoordMain(c).start();
	}

	public CoordMain(Coord coord) {
		this.coord = coord;
	}

	public void run() {
		swap();
		reset();
	}
	@Atomic
	public void swap() {
		int oldX;
		oldX = coord.x;
		coord.x = coord.y; // swap X
		coord.y = oldX; // swap Y
	}
	
	public void reset() {
		resetX();
		// inconsistent state (0, y)
		resetY();
	}
	@Atomic
	private void resetX(){
		coord.x = 0;
	}
	@Atomic
	private void resetY(){
		coord.y = 0;
	}
	static class Coord {
		int x, y;
	}
}