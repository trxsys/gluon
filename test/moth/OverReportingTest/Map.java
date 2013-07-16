package test.moth.OverReportingTest;

import test.common.Atomic;

public class Map {

	Object[] keys, values;
	volatile boolean init_done = false;

	@Atomic
	private void sets(){
		init_done = true;
		// update keys and values
		keys = new Object[10];
		values = new Object[keys.length];
	}
	@Atomic
	Object get(Object key) {
		Object res = null;
		// read keys and values
		for (int i = 0 ; i < keys.length ; i++) {
			if (key.equals(keys[i])) {
				res = values[i];
				break;
			}
		}
		return res;
	}
	void init() {
		if (!init_done){
			sets();
		}
	}
}