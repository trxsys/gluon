package test.validation.NASA;

public class Cell {

	public Object value;
	public boolean achieved;
	
	public Cell(){
		value = null;
		achieved = false;
	}
	public Cell(Object value, boolean achieved){
		this.value = value;
		this.achieved = achieved;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (achieved ? 1231 : 1237);
		result = prime * result + ((value == null) ? 0 : value.hashCode());
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
		Cell other = (Cell) obj;
		if (achieved != other.achieved)
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}
}