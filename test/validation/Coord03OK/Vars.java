package test.validation.Coord03OK;

import test.common.Atomic;
import test.common.Contract;

@Contract(clauses = "getX getY;"
                   +"getY getX;"
                   +"setX setY;"
                   +"setY setX;")
public class Vars {
	int x = 0;
	int y = 0;
	
	public Vars (int x, int y){
		this.x = x;
		this.y = y;
	}
	public Vars(){
		this(0,0);
	}
	
	@Atomic
	public int getX() {
		return x;
	}
	
	@Atomic
	public void setX(int x) {
		this.x = x;
	}
	
	@Atomic
	public int getY() {
		return y;
	}
	
	@Atomic
	public void setY(int y) {
		this.y = y;
	}
	
	@Atomic
	public Pair<Integer,Integer> getXY() {
		return new Pair<Integer,Integer>(x,y);
	}
	
	@Atomic
	public void setXY(int x1, int x2) {
		x = x1;
		y = x2;
	}
}
