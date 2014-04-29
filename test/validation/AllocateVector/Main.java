package test.validation.AllocateVector;

import java.io.*;

/**
 * class Test: Used to test class AllocationVector.
 */
public class Main {
	/**
	 * Indicates number of threads runs to perform.
	 */
	private static final int runsNum = 1;

	/**
	 * MAIN METHOD.
	 * Gets from command-line: 1. Name of output file.
	 *                         2. Concurrency Parameter (little,average,lot).
	 * @param args command-line arguments as written above.
	 */

	public static void main(String[] args) {
		for (int i=0; i < runsNum; i++) {
			runTest(args);
		}
	}

	/**
	 * Gets from 'args': 1. Name of output file.
	 *                   2. Concurrency Parameter (little,average,lot).
	 * @param args command-line arguments as written above.
	 */
	public static void runTest(String[] args) {
		AllocationVector vector = null;
		TestThread1 Thread1 = null;
		TestThread1 Thread2 = null;
		int[] Thread1Result = null;
		int[] Thread2Result = null;
		FileOutputStream out = null;

		/**
		 * Reading command-line arguments.
		 */
		try {
			if (args.length != 2) {
				throw new Exception();
			}

			// Opening output file with name 'args[0]' for append write.
			out = new FileOutputStream(args[0], false);

			// Checking concurrency parameter correctness.
			if ( (args[1].compareTo("little") != 0) &&
			     (args[1].compareTo("average") != 0) &&
			     (args[1].compareTo("lot") != 0)) {
				throw new Exception();
			}
		} catch (Exception e) {
			System.err.println("Invalid command-line arguments...");
			System.exit(1);
		}

		/**
		 * If here, then command-line arguments are correct.
		 * Therefore, proceeding according to the concurrency parameter value.
		 */
		// Setting threads run configuration according to concurrency parameter.
		if (args[1].compareTo("little") == 0) {
			vector = new AllocationVector(20000);
			Thread1Result = new int[1000];
			Thread2Result = new int[1000];
		} else if (args[1].compareTo("average") == 0) {
			vector = new AllocationVector(10000);
			Thread1Result = new int[2000];
			Thread2Result = new int[2000];
		} else if (args[1].compareTo("lot") == 0) {
			vector = new AllocationVector(5000);
			Thread1Result = new int[5000];
			Thread2Result = new int[5000];
		}

		// Creating threads, starting their run and waiting till they finish.
		Thread1 = new TestThread1(vector,Thread1Result);
		Thread2 = new TestThread1(vector,Thread2Result);
		Thread1.start();
		//for (int i = 0; i < 100000; i++); // "Pause" between threads run to try "hide"
		// the BUG.
		Thread2.start();
		try {
			Thread1.join();
			Thread2.join();
		} catch (InterruptedException e) {
			System.err.println("Error joining threads...");
			System.exit(1);
		}

		// Checking correctness of threads run results and printing the according
		// tuple to output file.
		try {
			if (Thread1Result[0] == -2) {
				out.write("<Test, Thread1 tried to allocate block which is allocated, weak-reality (Two stage access)>\n".getBytes());
				System.out.println("ups");
			} else if (Thread1Result[0] == -3){
				out.write("<Test, Thread1 tried to free block which is free, weak-reality (Two stage access)>\n".getBytes());
				System.out.println("ups");
			} else if (Thread2Result[0] == -2) {
				out.write("<Test, Thread2 tried to allocate block which is allocated, weak-reality (Two stage access)>\n".getBytes());
				System.out.println("ups");
			} else if (Thread2Result[0] == -3){
				out.write("<Test, Thread2 tried to free block which is free, weak-reality (Two stage access)>\n".getBytes());
				System.out.println("ups");
			} else {
				out.write("<Test, correct-run, none>\n".getBytes());
			}
		} catch (IOException ex) {
			System.err.println("Error writing to output file...");
			System.exit(1);
		}
	}
}