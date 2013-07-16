package test.moth.BufferTest;

import test.common.Atomic;

public class BufferMain extends Thread {
	Buffer buffer;

	public BufferMain(Buffer buffer) {
		super();
		this.buffer = buffer;
	}

	public static void main(String[] args) {
		Buffer buffer = new Buffer();
		new BufferMain(buffer).start();
		new BufferMain(buffer).start();
		new BufferMain(buffer).start();
	}

	public void run() {
		work();
	}

	public void work() {
		int value, fdata;
		
		// This simulates while(true)
		int x = 4;
		boolean b = x ==4;
		
		while (b) {
			value = buffer.next();
			fdata = f(value); // long computation
			buffer.add(fdata);
			// However, the program is correct because
			 // the buffer protocol ensures that the
		} // returned data remains thread-local.
	}

	private int f(int x) {
		return x * x;
	}

	static class Buffer {
		// I know it doesn't make sense, we're just trying to simulate reads and writes
		int cell = 0;
		int head = 0;
		int tail = 0;
		
		@Atomic
		public int next() {
			int res;
			head = head + 1;
			res = cell;
			return res;
		}

		@Atomic
		public void add(int x) {
			tail = tail + 1;
			cell = x;
		}
	}
}
