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
	private final int nTrials, nPairs;

	public CheckSumMC() {
		this.queue = new LinkedQueue<Integer>();
		this.nTrials = 4;
		this.nPairs = 1;
		this.barrier = new CyclicBarrier(nPairs * 2 + 1);
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		CheckSumMC queueTest = new CheckSumMC();
		queueTest.test();
		pool.shutdown();
	}

	public void test() {
		try {
			for (int i = 0; i < nPairs; i++) {
			pool.execute(new Producer());
			pool.execute(new Consumer());
			}
			System.out.println("Passed instantiation");
			System.out.println();
			barrier.await(); // wait for threads to be ready
			System.out.println();
			System.out.println("Currently Executing");
			System.out.println();
			barrier.await(); // wait for threads to be finished
			int sum1 = putSum.get();
			int sum2 = takeSum.get();
			System.out.println();
			System.out.println("Put Sum: " + sum1);
			System.out.println("Take Sum: " + sum2);
			System.out.println();
			if (sum1 == sum2)
				System.out.println("Put Sum & Get Sum are the same!");
			else
				System.out.println("Put Sum & Get Sum are different!");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	class Producer implements Runnable {
		public void run() {
			try {
				System.out.println("Producer is Running!");
				int seed = this.hashCode();
				int sum = 0;
				for (int i = nTrials; i > 0; --i) {
					queue.put(seed);
					System.out.println("Put Queue: " + seed);
					sum += seed;
					// seed = xorShift(seed);
				}
				barrier.await();
				putSum.getAndAdd(sum);
				barrier.await();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	class Consumer implements Runnable {
		public void run() {
			try {
				System.out.println("Consumer is Running!");
				int sum = 0;
				barrier.await();
				LinkedQueue.Node<Integer> travel = queue.getHead();
				while (travel.next.get() != null) {
					travel = travel.next.get();
					int element = travel.item;
					System.out.println("Get Queue: " + element);
					sum += element;
				}
				takeSum.getAndAdd(sum);
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
