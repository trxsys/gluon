package test.validation.UnderReportingTestOK;

import test.common.Atomic;
import test.common.Contract;

@Contract(clauses = "inc inc;")
public class Counter {

	int i;
	
	@Atomic
	int inc(int a) {
		i += a;
		return i;
	}
}
