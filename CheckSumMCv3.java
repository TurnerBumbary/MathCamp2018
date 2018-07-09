package linkedQueue;

import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;

//import junit.framework.TestCase;

public class CheckSumMCv3 {
	LinkedQueue<Integer> queue;
	private final AtomicInteger putSum = new AtomicInteger(0);
	private final AtomicInteger takeSum = new AtomicInteger(0);
	private final CyclicBarrier barrier;
	private final int nTrials, nProducers;
	private final AtomicInteger numThreads = new AtomicInteger(1);
	private Integer curNumber = 0;
	private static Thread[] threadArray;
	private AtomicInteger index = new AtomicInteger(0);
	private SnapShotCheckSum[] snapshoots;

	public CheckSumMCv3() {
		this.queue = new LinkedQueue<Integer>();
		this.nTrials = 3;
		this.nProducers = 2;
		this.barrier = new CyclicBarrier(nProducers + 2);
		threadArray = new Thread[nProducers];
		snapshoots = new SnapShotCheckSum[nProducers];
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		CheckSumMCv3 queueTest = new CheckSumMCv3();
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
			barrier.await(); // wait for threads to be finished
			int sum1 = putSum.get();
			int sum2 = takeSum.get();
			if (sum1 == sum2)
				System.out.println("Correct");
			else
				System.out.println("Error");
			for (SnapShotCheckSum element : snapshoots) {
				System.out.println(element.toString());
			}
			System.out.println();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	class Producer implements Runnable {
		public void run() {
			try {
				numThreads.getAndIncrement();
				int sum = 0;
				LinkedQueue<Integer> partialQueue = new LinkedQueue<Integer>();
				for (int i = 0; i < nTrials; i++) {
					Integer seed = curNumber.hashCode();
					seed = xorShift(seed);
					queue.put(seed);
					partialQueue.put(seed);
					sum += seed;
					curNumber++;
				}
				barrier.await();
				// This section adds the data to the ArrayList
				CyclicBarrier partialBarrier = new CyclicBarrier(2);
				PartialMonitor mon = new PartialMonitor(partialQueue, partialBarrier);
				Thread monitor = new Thread(mon);
				monitor.start();
				partialBarrier.await();
				long time = System.nanoTime();
				int i = index.getAndIncrement();
				snapshoots[i] = new SnapShotCheckSum(time, mon.getPartialSum(), sum);
				// --------------------------
				putSum.getAndAdd(sum);
				barrier.await();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	class PartialMonitor implements Runnable {
		private LinkedQueue<Integer> partialQueue;
		private int partialSum = 0;
		CyclicBarrier partialBarrier;
		
		public PartialMonitor(LinkedQueue<Integer> initPartialQueue, CyclicBarrier initPartialBarrier) {
			partialQueue = initPartialQueue;
			this.partialBarrier = initPartialBarrier;
		}

		public void run() {
			try {
				LinkedQueue.Node<Integer> travel = partialQueue.getHead();
				while (travel.next.get() != null) {
					travel = travel.next.get();
					int element = travel.item;
					partialSum += element;
				}
				this.partialBarrier.await();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		public int getPartialSum() {
			return partialSum;
		}
	}

	class Monitor implements Runnable {
		public void run() {
			try {
				numThreads.getAndIncrement();
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
