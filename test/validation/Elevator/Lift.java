package test.validation.Elevator;

/*
 * Copyright (C) 2000 by ETHZ/INF/CS
 * All rights reserved
 *
 * @version $Id$
 * @author Roger Karrer
 */

import java.util.*;

// class that implements the elevator threads
class Lift extends Thread {

	// used for assigning unique ids to the elevators
	private static int count = 0;

	public static final int IDLE = 0;
	public static final int UP = 1;
	public static final int DOWN = 2;

	private int travelDir; // one of IDLE, UP, or DOWN
	private int currentFloor;
	// holds the number of people who want to get off on each floor
	private int[] peopleFor;
	// Values in pickupOn can be IDLE, UP, DOWN, and UP|DOWN, which indicate
	// which calls the elevator should respond to on each floor. IDLE means
	// don't pick up on that floor
	private int[] pickupOn;
	private int firstFloor, lastFloor;
	// reference to the shared control object
	private Controls controls;

	// Create a new elevator that can travel from floor 1 to floor numFloors.
	// The elevator starts on floor 1, and is initially idle with no passengers.
	// c is a reference to the shared control object
	// The thread starts itself
	public Lift(int numFloors, Controls c) {
		super("Lift " + count++);
		controls = c;
		firstFloor = 1;
		lastFloor = numFloors;
		travelDir = IDLE;
		currentFloor = firstFloor;
		pickupOn = new int[numFloors + 1];
		peopleFor = new int[numFloors + 1];
		for (int i = 0; i <= numFloors; i++) {
			pickupOn[i] = IDLE;
			peopleFor[i] = 0;
		}
		start();
	}

	// Body of the thread. If the elevator is idle, it checks for calls
	// every tenth of a second. If it is moving, it takes 1 second to
	// move between floors.
	public void run() {
		while (true) {
			if (travelDir == IDLE) {
				try {
					sleep(100);
				} catch (InterruptedException e) {
				}
				doIdle();
			} else {
				try {
					sleep(1000);
				} catch (InterruptedException e) {
				}
				doMoving();
			}
		}
	}

	// IDLE
	// First check to see if there is an up or down call on what ever floor
	// the elevator is idle on. If there isn't one, then check the other floors.
	private void doIdle() {
		boolean foundFloor = false;
		int targetFloor = -1;

		if (claimUp(controls, getName(), currentFloor)) {
			// System.out.println("Lift::doIdle - could claim upcall on current floor");
			// // CARE
			foundFloor = true;
			targetFloor = currentFloor;
			travelDir = UP;
			addPeople(controls.getUpPeople(currentFloor));
		} else if (claimDown(controls, getName(), currentFloor)) {
			// System.out.println("Lift::doIdle - could claim downcall on current floor");
			// // CARE
			foundFloor = true;
			targetFloor = currentFloor;
			travelDir = DOWN;
			addPeople(controls.getDownPeople(currentFloor));
		}

		// System.out.println("Lift::doIdle - lookuing for calls on other floors");
		// // CARE
		for (int floor = firstFloor; !foundFloor && floor <= lastFloor; floor++) {
			// System.out.println("Lift::doIdle - checking floor " + floor); //
			// CARE
			if (claimUp(controls, getName(), floor)) {
				// System.out.println("Lift::doIdle - success with claimUp " +
				// floor); // CARE
				foundFloor = true;
				targetFloor = floor;
				pickupOn[floor] |= UP;
				travelDir = (targetFloor > currentFloor) ? UP : DOWN;
			} else if (claimDown(controls, getName(), floor)) {
				// System.out.println("Lift::doIdle - success with claimDown " +
				// floor); // CARE
				foundFloor = true;
				targetFloor = floor;
				pickupOn[floor] |= DOWN;
				travelDir = (targetFloor > currentFloor) ? UP : DOWN;
			}
		}

		if (foundFloor) {
//			System.out.println(getName() + " is now moving "
//					+ ((travelDir == UP) ? "UP" : "DOWN"));
		}
	}

	// MOVING
	// First change floor (up or down as appropriate)
	// Drop off passengers if we have to
	// Then pick up passengers if we have to
	private void doMoving() {
		currentFloor += (travelDir == UP) ? 1 : -1;
		int oldDir = travelDir;

		if (travelDir == UP && currentFloor == lastFloor)
			travelDir = DOWN;
		if (travelDir == DOWN && currentFloor == firstFloor)
			travelDir = UP;
//		System.out.println(getName() + " now on " + currentFloor);

		if (peopleFor[currentFloor] > 0) {
//			System.out.println(getName() + " delivering "
//					+ peopleFor[currentFloor] + " passengers on "
//					+ currentFloor);
			peopleFor[currentFloor] = 0;
		}

		// Pickup people who want to go up if:
		// 1) we previous claimed an up call on this floor, or
		// 2) we are travelling up and there is an unclaimed up call on this
		// floor
		if (((pickupOn[currentFloor] & UP) != 0)
				|| (travelDir == UP &&
                    claimUp(controls, getName(), currentFloor))) {
			addPeople(controls.getUpPeople(currentFloor));
			pickupOn[currentFloor] &= ~UP;
		}

		// Pickup people who want to go down if:
		// 1) we previous claimed an down call on this floor, or
		// 2) we are travelling down and there is an unclaimed down call on this
		// floor
		if (((pickupOn[currentFloor] & DOWN) != 0)
            || (travelDir == DOWN && claimDown(controls, getName(),
                                                currentFloor))) {
			addPeople(controls.getDownPeople(currentFloor));
			pickupOn[currentFloor] &= ~DOWN;
		}

		if (travelDir == UP) {
			// If we are travelling up, and there are people who want to get off
			// on a floor above this one, continue to go up.
			if (stopsAbove())
				;
			else {
				// If we are travelling up, but no one wants to get off above
				// this
				// floor, but they do want to get off below this one, start
				// moving down
				if (stopsBelow())
					travelDir = DOWN;
				// Otherwise, no one is the elevator, so become idle
				else
					travelDir = IDLE;
			}
		} else {
			// If we are travelling down, and there are people who want to get
			// off
			// on a floor below this one, continue to go down.
			if (stopsBelow())
				;
			else {
				// If we are travelling down, but no one wants to get off below
				// this
				// floor, but they do want to get off above this one, start
				// moving up
				if (stopsAbove())
					travelDir = UP;
				// Otherwise, no one is the elevator, so become idle
				else
					travelDir = IDLE;
			}
		}

		// Print out are new direction
		if (oldDir != travelDir) {
//			System.out.print(getName());
			if (travelDir == IDLE){
//				System.out.println(" becoming IDLE");
			}
			else if (travelDir == UP){
//				System.out.println(" changing to UP");
			}
			else if (travelDir == DOWN){
//				System.out.println(" changing to DOWN");
			}
		}
	}

	// Returns true if there are passengers in the elevator who want to stop
	// on a floor above currentFloor, or we claimed a call on a floor below
	// currentFloor
	private boolean stopsAbove() {
		boolean above = false;
		for (int i = currentFloor + 1; !above && i <= lastFloor; i++)
			above = (pickupOn[i] != IDLE) || (peopleFor[i] != 0);
		return above;
	}

	// Returns true if there are passengers in the elevator who want to stop
	// on a floor below currentFloor, or we claiemda call on a floor above
	// currentFloor
	private boolean stopsBelow() {
		boolean below = false;
		for (int i = currentFloor - 1; !below && (i >= firstFloor); i--)
			below = (pickupOn[i] != IDLE) || (peopleFor[i] != 0);
		return below;
	}

	// Updates peopleFor based on the Vector of destination floors received
	// from the control object
	private void addPeople(Vector people) {
//		System.out.println(getName() + " picking up " + people.size()
//				+ " passengers on " + currentFloor);
		for (Enumeration e = people.elements(); e.hasMoreElements();) {
			int toFloor = ((Integer) e.nextElement()).intValue();
			peopleFor[toFloor] += 1;
		}
	}

	// An elevator calls this if it wants to claim an up call
	// Sets the floor's upFlag to true if he has not already been set to true
	// Returns true if the elevator has successfully claimed the call, and
	// False if the call was already claimed (upFlag was already true)
	public boolean claimUp(Controls c, String lift, int floor) {
		if (c.checkUp(floor)) {
            c.claimUp(floor);
            return true;
		}
		return false;
	}


	// An elevator calls this if it wants to claim an down call
	// Sets the floor's downFlag to true if he has not already been set to true
	// Returns true if the elevator has successfully claimed the call, and
	// False if the call was already claimed (downFlag was already true)
	public boolean claimDown(Controls c, String lift, int floor) {
		if (c.checkDown(floor)) {
            c.claimDown(floor);
            return true;
		}
		return false;
	}
}
