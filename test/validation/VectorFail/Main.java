package test.validation.VectorFail;

public class Main{
	public int x;
	public int y;
	public Main (int x , int y){
		this.x = x;
		this.y = y;
	}
	public static void main(String[] args) {
		Vector pair = new Vector(1,2);
		
		for(int i = 0; i < 10; i++){
			new MyThread(pair).start();
		}
	}
}
