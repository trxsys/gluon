package test.validation.VectorFail;

import test.common.Atomic;
import test.common.Contract;

@Contract(clauses = "getMax getMin;"
                   +"getMin getMax;")
public class Vector{

	public int first;
	public int second;
	public boolean firstIsGreater;
	
	public Vector(int x1, int x2) {
		this.first = x1;
		this.second = x2;
		this.firstIsGreater = x1 > x2;
	}
	
	public int getFirst() {
		return first;		
	}
	public int getSecond() {
		return second;		
	}
	
	@Atomic
	public int getMax() {
		if(firstIsGreater)
			return first;
		return second;		
	}
	
	@Atomic
	public int getMin() {
		if(!firstIsGreater)
			return first;
		return second;		
	}
	
	@Atomic
	public void setElements(int x1, int x2){
		this.first = x1;
		this.second = x2;
	}
	public String toString(){
		return "("+first+","+second+")";
	}

	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + new Integer(first).hashCode();
		result = prime * result + new Integer(second).hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Vector))
			return false;
		Vector other = (Vector) obj;
		return first == other.first && second == other.second;
	}
}
