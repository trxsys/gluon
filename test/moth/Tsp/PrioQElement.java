package test.moth.Tsp;

/*
 * Copyright (C) 2000 by ETHZ/INF/CS
 * All rights reserved
 * 
 * @version $Id$
 * @author Florian Schneider
 */

public class PrioQElement {
	protected int index;
	protected int priority;
	
	public int getIndex() {
		return index;
	}
	public void setIndex(int index) {
		this.index = index;
	}
	public int getPriority() {
		return priority;
	}
	public void setPriority(int priority) {
		this.priority = priority;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + index;
		result = prime * result + priority;
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
		PrioQElement other = (PrioQElement) obj;
		if (index != other.index)
			return false;
		if (priority != other.priority)
			return false;
		return true;
	}
}
