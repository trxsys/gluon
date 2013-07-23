package test.moth.Jigsaw;

import java.util.HashMap;
import java.util.Map;

import test.common.Atomic;
import test.common.Contract;

@Contract(clauses = "lookupEntry checkClosed;")
public class ResourceStoreManager {
    
	boolean closed = false;
	Map entries = new HashMap();
    
    //	@Atomic
    //	void checkClosed() {
    //		if (closed)
    //			throw new RuntimeException();
    //	}
	//We rewrite the method without exceptions
	@Atomic
	boolean checkClosed() {
		return closed;
	}
        
	@Atomic
	Entry lookupEntry(Object key) {
		Entry e = (Entry) entries.get(key);
		if (e == null) {
			e = new Entry(key.toString());
			entries.put(key, e);
			entries=null;
		}
		return e;
	}
    
	@Atomic
	void shutdown() {
		entries.clear();
		closed = true;
	}    
}
