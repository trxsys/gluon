package test.moth.Jigsaw;


public class Entry {
	protected ResourceStore store;
	
	public Entry(String name){
		store = new ResourceStore(name);
	}
	
	public ResourceStore getStore() {
		return store;
	}
	
	public String getName(){
		return store.getName();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((store == null) ? 0 : store.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Entry other = (Entry) obj;
		if (store == null) {
			if (other.store != null)
				return false;
		} else if (!store.equals(other.store))
			return false;
		return true;
	}
}