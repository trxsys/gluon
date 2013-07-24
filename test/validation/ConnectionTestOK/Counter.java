package test.validation.ConnectionTestOK;

import test.common.Atomic;

public class Counter {

	int n = 0;

	@Atomic
	public void reset() {
		n = 0;
	}

	public void increment() {
		n++;
	}
}