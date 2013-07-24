package test.validation.ArithmeticDatabase;

public class Pair<K extends Comparable<K>, V> {

	public K k;
	public V v;
	
	public Pair(K k, V v) {
		super();
		this.k = k;
		this.v = v;
	}
	
	public Pair(Pair<K, V> p) {
		if(p != null){
			this.k = p.k;
			this.v = p.v;
		}
	}

	public K getk() {
		return k;
	}
	public void setk(K k) {
		this.k = k;
	}
	public V getv() {
		return v;
	}
	public void setv(V v) {
		this.v = v;
	}
	
	public String toString(){
		if(k == null || v == null)
			return "";
		return "("+k.toString()+","+v.toString()+")";
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((k == null) ? 0 : k.hashCode());
		result = prime * result + ((v == null) ? 0 : v.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Pair<?,?>))
			return false;
		Pair<?, ?> other = (Pair<?,?>) obj;
		if (k == null) {
			if (other.k != null)
				return false;
		} else if (!k.equals(other.k))
			return false;
		if (v == null) {
			if (other.v != null)
				return false;
		} else if (!v.equals(other.v))
			return false;
		return true;
	}
}