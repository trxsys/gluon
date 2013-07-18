package test.moth.Coord04;

import test.common.Atomic;
import test.common.Contract;

@Contract(clauses = "resetX resetY;"
                   +"resetY resetX;")
class Coord {
    private int x, y;

    public Coord()
    {
        x=y=0;
    }
    
    @Atomic
    public void swap() {
        int oldX = x;
        x = y; // swap X
        y = oldX; // swap Y
    }
    
    @Atomic
    public void resetX(){
        x = 0;
    }

    @Atomic
    public void resetY(){
        y = 0;
    }
}


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
		coord.swap();
		reset();
	}

    public void reset() {
        coord.resetX();
        // inconsistent state (0, y)
        coord.resetY();
    }
}
