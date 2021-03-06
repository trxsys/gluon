package test.validation.Jigsaw;

class Runner extends Thread {
    private ResourceStoreManager rsm;

    public Runner(ResourceStoreManager rsm) {
        this.rsm = rsm;
    }

	// not synched!!! HLDR, but we get a false negative!
	ResourceStore loadResourceStore(ResourceStoreManager r) {
		if (r.checkClosed()) return null; // R(closed)
		Entry e = r.lookupEntry(new Object()); // R(entries), W(entries)
		return e.getStore();
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
