package test.moth.MyStringBuffer;

import test.common.Atomic;

/*
 * Simulate java.lang.StringBuffer
 */
public final class MyStringBuffer {

	private java.lang.StringBuffer buffer;
	
	public MyStringBuffer(String string) {
		this.buffer = new StringBuffer(string);
	}

	public static void main(String args[]) {
		MyStringBuffer ham = new MyStringBuffer("ham");
		MyStringBuffer burger = new MyStringBuffer("burger");
		ham.append(burger);
		System.out.println(ham);
	}

	public MyStringBuffer append(MyStringBuffer other) {

		int len = other.length();	//read other.length
		
		// ...other threads may change sb.length(),
		// ...so len does not reflect the length of 'other'
		
		char[] value = new char[len];
		other.getChars(0, len, value, 0);
		
		//added by Vasco
		delete(0);
		//added by Vasco		
		return this;
	}
	@Atomic
	public int length() {
		return this.buffer.length();	//Read buffer
	}
	@Atomic
	public void getChars(int srcBegin, int srcEnd, char[] dst, int dstBegin) {
		this.buffer.getChars(srcBegin, srcEnd, dst, dstBegin);	//Read buffer
	}
	
	//added by Vasco
	@Atomic
	public void delete(int pos){
		this.buffer.deleteCharAt(pos);
	}
}