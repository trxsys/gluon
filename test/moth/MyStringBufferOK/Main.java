package test.moth.MyStringBufferOK;

import test.common.Atomic;
import test.common.Contract;

/*
 * Simulates java.lang.StringBuffer
 */
@Contract(clauses = "length getChars;")
class MyStringBuffer {
	private java.lang.StringBuffer buffer;
	
	public MyStringBuffer(String string) {
		this.buffer = new StringBuffer(string);
	}

	@Atomic
	public int length() {
		return this.buffer.length();	//Read buffer
	}

	@Atomic
	public void getChars(int srcBegin, int srcEnd, char[] dst, int dstBegin) {
		this.buffer.getChars(srcBegin, srcEnd, dst, dstBegin);	//Read buffer
	}
}

public class Main
{
	public static void main(String args[]) {
		MyStringBuffer ham = new MyStringBuffer("ham");
		MyStringBuffer burger = new MyStringBuffer("burger");

		append(ham, burger);
		System.out.println(ham);
	}

    @Atomic
	public static MyStringBuffer append(MyStringBuffer t, MyStringBuffer other) {

		int len = other.length();	//read other.length
		
		// ...other threads may change sb.length(),
		// ...so len does not reflect the length of 'other'
		
		char[] value = new char[len];
		other.getChars(0, len, value, 0);
		
		return t;
	}
}
