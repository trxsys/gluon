package test.validation.Elevator;

/*
 * Copyright (C) 2000 by ETHZ/INF/CS
 * All rights reserved
 * 
 * @version $Id$
 * @author Roger Karrer
 */

import java.util.*;
import java.io.*;

public class Elevator {

	// shared control object
	private Controls controls;
	private Vector events;

	// Initializer for main class, reads the input and initlizes
	// the events Vector with ButtonPress objects
	private Elevator() {
		InputStreamReader reader = new InputStreamReader(System.in);
		StreamTokenizer st = new StreamTokenizer(reader);
		st.lowerCaseMode(true);
		st.parseNumbers();

		events = new Vector();

		int numFloors = 0, numLifts = 0;
		try {
			numFloors = readNum(st);
			numLifts = readNum(st);

			int time = 0, to = 0, from = 0;
			do {
				time = readNum(st);
				if (time != 0) {
					from = readNum(st);
					to = readNum(st);
					events.addElement(new ButtonPress(time, from, to));
				}
			} while (time != 0);
		} catch (IOException e) {
			System.err.println("error reading input: " + e.getMessage());
			e.printStackTrace(System.err);
			System.exit(1);
		}

		// Create the shared control object
		controls = new Controls(numFloors);
		// Create the elevators
		for (int i = 0; i < numLifts; i++)
			new Lift(numFloors, controls);
	}

	// Press the buttons at the correct time
	private void begin() {
		// Get the thread that this method is executing in
		Thread me = Thread.currentThread();
		// First tick is 1
		int time = 1;

		for (int i = 0; i < events.size();) {
			ButtonPress bp = (ButtonPress) events.elementAt(i);
			// if the current tick matches the time of th next event
			// push the correct buttton
			if (time == bp.time) {
				System.out
						.println("Elevator::begin - its time to press a button");
				if (bp.onFloor > bp.toFloor)
					controls.pushDown(bp.onFloor, bp.toFloor);
				else
					controls.pushUp(bp.onFloor, bp.toFloor);
				i += 1;
			}
			// wait 1/2 second to next tick
			try {
				me.sleep(500);
			} catch (InterruptedException e) {
			}
			time += 1;
		}
	}

	private int readNum(StreamTokenizer st) throws IOException {
		int tokenType = st.nextToken();

		if (tokenType != StreamTokenizer.TT_NUMBER)
			throw new IOException("Number expected!");
		return (int) st.nval;
	}

	public static void main(String args[]) {
		Elevator building = new Elevator();
		building.begin();
	}
}
