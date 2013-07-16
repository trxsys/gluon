package test.moth.UnderReportingTest;

import test.common.Atomic;

public class Counter {

	int i;
	
	@Atomic
	int inc(int a) {
		i += a;
		return i;
	}
}