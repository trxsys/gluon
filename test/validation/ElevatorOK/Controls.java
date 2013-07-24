package test.validation.ElevatorOK;

/*
 * Copyright (C) 2000 by ETHZ/INF/CS
 * All rights reserved
 * 
 * @version $Id$
 * @author Roger Karrer
 */

import java.util.*;

import test.common.Atomic;
import test.common.Contract;

// class of the shared control object
@Contract(clauses = "checkUp claimUp;"
                   +"checkDown claimDown;")
class Controls {
	private Floor[] floors;

	public Controls(int numFloors) {
		floors = new Floor[numFloors + 1];
		for (int i = 0; i <= numFloors; i++)
			floors[i] = new Floor();
	}

	// this is called to inform the control object of a down call on floor
	// onFloor
//	@Atomic
	public void pushDown(int onFloor, int toFloor) {
		// synchronized(floors[onFloor]) {
//		System.out.println("*** Someone on floor " + onFloor
//				+ " wants to go to " + toFloor);
		floors[onFloor].downPeople.addElement(new Integer(toFloor));
		if (floors[onFloor].downPeople.size() == 1)
			floors[onFloor].downFlag = false;
		// }
	}

	// this is called to inform the control object of an up call on floor
	// onFloor
//	@Atomic
	public void pushUp(int onFloor, int toFloor) {
		// synchronized(floors[onFloor]) {
//		System.out.println("*** Someone on floor " + onFloor
//				+ " wants to go to " + toFloor);
		floors[onFloor].upPeople.addElement(new Integer(toFloor));
		if (floors[onFloor].upPeople.size() == 1)
			floors[onFloor].upFlag = false;
		// }
	}

	// Added by Vasco Pessanha
	@Atomic
	public boolean claimUp(int floor) {
		if (!floors[floor].upFlag) {
			floors[floor].upFlag = true;
			return true;
		}
		return false;
	}

	// Added by Vasco Pessanha
	@Atomic
	public boolean claimDown(int floor) {
		if (!floors[floor].downFlag) {
			floors[floor].downFlag = true;
			return true;
		}
		return false;
	}

	// An elevator calls this to see if an up call has occured on the given
	// floor. If another elevator has already claimed the up call on the
	// floor, checkUp() will return false. This prevents an elevator from
	// wasting its time trying to claim a call that has already been claimed
	@Atomic
	public boolean checkUp(int floor) {
		// synchronized(floors[floor]) {
		boolean ret = floors[floor].upPeople.size() != 0;
		ret = ret && !floors[floor].upFlag;
		return ret;
		// }
	}

	// An elevator calls this to see if a down call has occured on the given
	// floor. If another elevator has already claimed the down call on the
	// floor, checkUp() will return false. This prevents an elevator from
	// wasting its time trying to claim a call that has already been claimed
	@Atomic
	public boolean checkDown(int floor) {
		// synchronized(floors[floor]) {
		boolean ret = floors[floor].downPeople.size() != 0;
		ret = ret && !floors[floor].downFlag;
		return ret;
		// }
	}

	// An elevator calls this to get the people waiting to go up. The
	// returned Vector contains Integer objects that represent the floors
	// to which the people wish to travel. The floors vector and upFlag
	// are reset.
	@Atomic
	public Vector getUpPeople(int floor) {
		// synchronized(floors[floor]) {
		Vector temp = floors[floor].upPeople;
		floors[floor].upPeople = new Vector();
		floors[floor].upFlag = false;
		return temp;
		// }
	}

	// An elevator calls this to get the people waiting to go down. The
	// returned Vector contains Integer objects that represent the floors
	// to which the people wish to travel. The floors vector and downFlag
	// are reset.
	@Atomic
	public Vector getDownPeople(int floor) {
		// synchronized(floors[floor]) {
		Vector temp = floors[floor].downPeople;
		floors[floor].downPeople = new Vector();
		floors[floor].downFlag = false;
		return temp;
		// }
	}
}
