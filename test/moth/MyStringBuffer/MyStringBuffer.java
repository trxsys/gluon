package test.moth.MyStringBuffer;

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
