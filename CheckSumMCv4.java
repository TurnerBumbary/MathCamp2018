package linkedQueue;

import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;
//import junit.framework.TestCase;

public class CheckSumMCv4 {
	LinkedQueue<Integer> queue;
	private final AtomicInteger putSum = new AtomicInteger(0);
	int takeSum = 0;
	private final CyclicBarrier barrier;
	private final int nTrials, nProducers;
	private static Thread[] threadArray;
	private AtomicInteger index = new AtomicInteger(0);
	private SnapShotCheckSum[] snapshoots;

	public CheckSumMCv4() {
		this.queue = new LinkedQueue<Integer>();
		this.nTrials = 10;
		this.nProducers = 2;
		this.barrier = new CyclicBarrier(nProducers + 2);
		threadArray = new Thread[nProducers];
		snapshoots = new SnapShotCheckSum[nProducers];
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		CheckSumMCv4 queueTest = new CheckSumMCv4();
		queueTest.test();
	}

	public void test() {
		try {
			for (int i = 0; i < nProducers; i++) {
				threadArray[i] = new Thread(new Producer());
				threadArray[i].start();
			}
			Thread m = new Thread(new Monitor());
			m.start();
			barrier.await(); // the main method waits for all of the other
							// methods to execute
			for (int i = 0; i < nProducers; i++) {
				threadArray[i].join();
			}
			m.join();
			if (putSum.get() == takeSum)
				System.out.println("Correct");
			else
				System.out.println("Error");
			for(SnapShotCheckSum element : snapshoots)
			{
				System.out.println(element);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	class Producer implements Runnable {
		public void run() {
			try {
				int sum = 0;
				LinkedQueue<Integer> partialQueue = new LinkedQueue<Integer>();
				for (int i = 0; i < nTrials; i++) {
					Integer seed = (int)(Math.random()*10 + 1);
					seed = xorShift(seed);
					queue.put(seed);
					partialQueue.put(seed);
					sum += seed;
				}
				barrier.await();
				// This section adds the data to the ArrayList
				int partialSum = 0;
				LinkedQueue.Node<Integer> travel = partialQueue.getHead();
				while (travel.next.get() != null) {
					travel = travel.next.get();
					int element = travel.item;
					partialSum += element;
				}
				long time = System.nanoTime();
				int i = index.getAndIncrement();
				snapshoots[i] = new SnapShotCheckSum(time, partialSum, sum);
				// --------------------------
				putSum.getAndAdd(sum);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	class Monitor implements Runnable {
		public void run() {
			try {
				int sum = 0;
				barrier.await();
				LinkedQueue.Node<Integer> travel = queue.getHead();
				while (travel.next.get() != null) {
					travel = travel.next.get();
					int element = travel.item;
					sum += element;
				}
				takeSum = sum;
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

	class SnapShotCheckSum {
		private final long time;
		private final int monitorSum;
		private final int producerSum;
		public SnapShotCheckSum(long initTime, int initMonitorSum,
				int initProducerSum) {
			time = initTime;
			monitorSum = initMonitorSum;
			producerSum = initProducerSum;
		}
		public long getTime() {
			return time;
		}
		public int getMonitorSum() {
			return monitorSum;
		}
		public int getProducerSum() {
			return producerSum;
		}
		public String toString() {
			return "[" + time + ", " + monitorSum + ", " + producerSum + "]";
		}
	}
}
