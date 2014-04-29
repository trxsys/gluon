package test.validation.AllocateVector;

import test.common.Atomic;
import test.common.Contract;

/**
 * class AllocationVector: Used to manage allocation and freeing of blocks.
 * BUG DOCUMENTATION: There is a synchronization GAP between the methods
 * "getFreeBlockIndex" and "markAsAllocatedBlock", in which anything can be done.
 */
@Contract(clauses ="getFreeBlockIndex markAsAllocatedBlock;")
public class AllocationVector {
	/**
	 * Character vector which holds information about allocated and free blocks,
	 * in the following way:
	 * if vector[i] == 'F' -> i-th block is free.
	 * if vector[i] == 'A' -> i-th block is allocated.
	 */
	private char[] vector = null;

	/**
	 * Constructor: Constructs AllocationVector for 'size' blocks, when all blocks
	 * are free.
	 * @param size Size of AllocationVector.
	 */
	public AllocationVector(int size) {
		// Allocating vector of size 'size',
		// when all blocks are assigned to free.
		vector = new char[size];
		for (int i=0; i < size; i++) {
			vector[i] = 'F';
		}
	}

	/**
	 * Returns index of free block, if such exists.
	 * If no free block, then -1 is returned.
	 * @return Index of free block if such exists, else -1.
	 */
	@Atomic
	public int getFreeBlockIndex() {
		int i;
		int count;
		int startIndex;
		int interval;
		int searchDirection;
		double randomValue;
		int[] primeValues = { 1, 2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41, 43, 47, 53 };
		randomValue = Math.random();

		// Choosing randomly start entry for search.
		startIndex = (int)Math.floor((vector.length - 1) * randomValue);

		// Choosing randomly increment/decrement prime value.
		interval = primeValues[(int)Math.floor((primeValues.length - 1) * randomValue)];

		// Choosing randomly search direction and starting the search from randomly
		// choosen start entry in that direction with the randomly choosen interval.
		if (randomValue > 0.5) {
			// Searching forward.
			for (i = startIndex,count = 0; (count < vector.length) &&
				     (vector[i] != 'F')
				     ; i = i + interval, i %= vector.length, count++);
		} else {
			// Searching backward.
			for (i = startIndex,count = 0; (count < vector.length) &&
				     (vector[i] != 'F')
					     ; count++) {
				i = i - interval;
				if (i < 0) {
					i = i + vector.length;
				}
			}
		}

		if (count == vector.length) {
			return -1; // Indicates "no free block".
		}
		else {
			return i; // Returns the index of the found free block.
		}
	}

	/**
	 * Marks i-th block as allocated.
	 * @param i Index of block to allocate.
	 * NOTE: If allocating already allocated block, then Exception is thrown.
	 */
	@Atomic
	public void markAsAllocatedBlock(int i) /*throws Exception */{
		if (vector[i] != 'A') {
			vector[i] = 'A'; // Allocates i-th block.
		} else {
//			throw new Exception("Allocation");
		}
	}

	/**
	 * Marks i-th block as free.
	 * @param i Index of block to free.
	 * NOTE: If freeing already free block, then Exception is thrown.
	 */
	@Atomic
	public void markAsFreeBlock(int i)/* throws Exception*/ {
		if (vector[i] != 'F') {
			vector[i] = 'F'; // Frees i-th block.
		} else {
//			throw new Exception("Freeing");
		}
	}
}
