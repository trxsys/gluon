package test.moth.MyStringBuffer;

public class Main
{
	public static void main(String args[]) {
		MyStringBuffer ham = new MyStringBuffer("ham");
		MyStringBuffer burger = new MyStringBuffer("burger");

		append(ham, burger);
		System.out.println(ham);
	}

	public static MyStringBuffer append(MyStringBuffer t, MyStringBuffer other) {

		int len = other.length();	//read other.length
		
		// ...other threads may change sb.length(),
		// ...so len does not reflect the length of 'other'
		
		char[] value = new char[len];
		other.getChars(0, len, value, 0);
		
		return t;
	}
}
