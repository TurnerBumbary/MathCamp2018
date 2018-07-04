package linkedQueue;

import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

//import junit.framework.TestCase;

public class CheckSumMC {
	LinkedQueue<Integer> queue;
	private static final ExecutorService pool = Executors.newCachedThreadPool();
	private final AtomicInteger putSum = new AtomicInteger(0);
	private final AtomicInteger takeSum = new AtomicInteger(0);
	private final CyclicBarrier barrier;
	private final int nTrials, nProducers;
	private final AtomicInteger numThreads = new AtomicInteger(1);

	public CheckSumMC() {
		this.queue = new LinkedQueue<Integer>();
		this.nTrials = 100;
		this.nProducers = 100;
		this.barrier = new CyclicBarrier(nProducers + 2);
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		CheckSumMC queueTest = new CheckSumMC();
		queueTest.test();
		pool.shutdown();
	}

	public void test() {
		try {
			for (int i = 0; i < nProducers; i++) {
				pool.execute(new Producer());
			}
			pool.execute(new Monitor());
			barrier.await(); // the main method waits for all of the other methods to execute
			barrier.await(); // wait for threads to be finished
			int sum1 = putSum.get();
			int sum2 = takeSum.get();
			System.out.println();
			System.out.println("Producer Sum: " + sum1);
			System.out.println("Monitor Sum: " + sum2);
			System.out.println();
			if (sum1 == sum2)
				System.out.println("Producer Sum & Monitor Sum are the same!");
			else
				System.out.println("Producer Sum & Monitor Sum are different!");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	class Producer implements Runnable {
		public void run() {
			try {
				numThreads.getAndIncrement();
				System.out.println("Producer is Running!");
				Integer temp = 0;
				int sum = 0;
				for (int i = 0; i < nTrials; i++)
				{
					Integer seed = temp.hashCode();
					seed = xorShift(seed);
					queue.put(seed);
					sum += seed;
					temp++;
				}
				barrier.await();
				putSum.getAndAdd(sum);
				System.out.println("Current Put Sum: " + sum);
				System.out.println("Partial Put Sum: " + putSum.get());
				barrier.await();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	class Monitor implements Runnable {
		public void run() {
			try {
				numThreads.getAndIncrement();
				System.out.println("Monitor is Running!");
				int sum = 0;
				barrier.await();
				LinkedQueue.Node<Integer> travel = queue.getHead();
				while (travel.next.get() != null) {
					travel = travel.next.get();
					int element = travel.item;
					sum += element;
				}
				takeSum.set(sum);
				barrier.await();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	public static int xorShift(int y) {
		y ^= (y << 6);
		y ^= (y >>> 21);
		y ^= (y << 7);
		return y;
	}
}
