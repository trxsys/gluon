package test.moth.Philo;

import test.common.Atomic;

class Table {
	boolean forks[];
	int eatctr;

	Table() {
		forks = new boolean[Philo.NUM_PHIL];
		for (int i = 0; i < Philo.NUM_PHIL; ++i)
			forks[i] = true;
	}

	@Atomic
	synchronized int getForks(int id) throws InterruptedException {
		int id1 = id;
		int id2 = (id + 1) % Philo.NUM_PHIL;
//		System.out.println(id + " check forks[" + id1 + "]=" + forks[id1]
//				+ " and forks[" + id2 + "]=" + forks[id2]);
		Philo.readObject(id);	//by Vasco
		Philo.readObject(id1);	//by Vasco
		Philo.readObject(id2);	//by Vasco
		Philo.readObject(forks);	//by Vasco
//		System.out.flush();
		while (!(forks[id1] && forks[id2])) {
//			System.out.println(id + " wait for forks");
			Philo.readObject(id);	//by Vasco
			wait();
		}
		forks[id1] = forks[id2] = false;
//		System.out.println(id + " got forks");
		Philo.readObject(id);	//by Vasco
		return eatctr++;
	}
	
	@Atomic
	synchronized void putForks(int id) {
//		System.out.println(id + " putforks");
		Philo.readObject(id);	//by Vasco
		forks[id] = forks[(id + 1) % Philo.NUM_PHIL] = true;
		notify();
//		System.out.println(id + " notify done");
		Philo.readObject(id);	//by Vasco
	}
}

class Philo extends Thread {
	static final int NUM_PHIL = 5;
	static final int MAX_EAT = 12;
	int id;
	Table t;

	Philo(int id, Table t) {
		this.id = id;
		this.t = t;
	}

	public void run() {
		System.out.println(id + " run start");
		try {
			int max_eat = 0;
			while (max_eat < MAX_EAT) {
				System.out.println(id + " let's try to get the forks"); // eat
				max_eat = t.getForks(id);
				System.out.println(id + " have the forks now"); // eat
				long l = (int) (Math.random() * 500) + 20;
				System.gc();
				System.out.println(id + " eating (" + l + ")"); // eat
				sleep(l);
				System.out.println(id + " that was good"); // eat
				t.putForks(id);
			}
		} catch (InterruptedException e) {
			System.out.println(id + " run interrupted");
//			;
		}
	}

	// Created by Vasco to read the objects used in the println's
	public static void readObject(Object x){
		Object y = x;
	}
	public static void main(String args[]) throws Exception {
		Table tab = new Table();
		Philo[] p = new Philo[NUM_PHIL];
		for (int i = 0; i < NUM_PHIL; ++i) {
			p[i] = new Philo(i, tab);
			p[i].start();
		}
		for (int i = 0; i < NUM_PHIL; ++i)
			p[i].join();
		// main should terminate last to have the access ctr correctly.
	}
}
