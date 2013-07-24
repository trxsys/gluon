package test.validation.Coord04;

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
