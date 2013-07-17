package test.moth.Coord03OK;

public class Main{
	public int x;
	public int y;
	public Main (int x , int y){
		this.x = x;
		this.y = y;
	}
	public static void main(String[] args) {
		Vars v = new Vars();
		for(int i = 0;i<10;i++){
			new T1(v).start();
			new T2(v).start();
			new T3(v).start();
			new T4(v).start();
		}
	}

}