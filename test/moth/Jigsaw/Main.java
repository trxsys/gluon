package test.moth.Jigsaw;

import java.util.HashMap;
import java.util.Map;

import test.common.Atomic;
import test.common.Contract;

@Contract(clauses = "lookupEntry checkClosed;")
class ResourceStoreManager {
    
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

class Runner extends Thread {
    private ResourceStoreManager rsm;

    public Runner(ResourceStoreManager rsm) {
        this.rsm = rsm;
    }

	// not synched!!! HLDR, but we get a false negative!
	ResourceStore loadResourceStore(ResourceStoreManager r) {
        //		checkClosed(); // R(closed)
		Entry e = r.lookupEntry(new Object()); // R(entries), W(entries)
		return r.checkClosed()? null : e.getStore();
	}

    public void run() {
        loadResourceStore(rsm);
        rsm.shutdown();
    }
}

public class Main
{
	public static void main(String args[]) {
		ResourceStoreManager rsm = new ResourceStoreManager();
		new Runner(rsm).start();
		new Runner(rsm).start();
		new Runner(rsm).start();
	}
}
