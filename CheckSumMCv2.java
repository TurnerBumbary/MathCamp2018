package linkedQueue;

import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;

//import junit.framework.TestCase;

public class CheckSumMCv2 {
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

	public CheckSumMCv2() {
		this.queue = new LinkedQueue<Integer>();
		this.nTrials = 3;
		this.nProducers = 2;
		this.barrier = new CyclicBarrier(nProducers + 2);
		threadArray = new Thread[nProducers];
		snapshoots = new SnapShotCheckSum[nProducers];
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		CheckSumMCv2 queueTest = new CheckSumMCv2();
		queueTest.test();
	}

	public void test() {
		try {
			for (int i = 0; i < nProducers; i++) {
				System.out.println("Producer: " + i + " invoked");
				threadArray[i] = new Thread(new Producer(i + ""));
				threadArray[i].start();
			}
			System.out.println("Monitor invoked");
			Thread m = new Thread(new Monitor());
			m.start();
			System.out.println("Test has reacheed barrier 1");
			barrier.await(); // the main method waits for all of the other
								// methods to execute
			System.out.println("Test has passed barrier 1");
			System.out.println("Test has reached barrier 2");
			barrier.await(); // wait for threads to be finished
			System.out.println("Test has passed barrier 2");
			int sum1 = putSum.get();
			int sum2 = takeSum.get();
			System.out.println("Producer Sum: " + sum1);
			System.out.println("Monitor Sum: " + sum2);
			if (sum1 == sum2)
				System.out.println("Producer Sum & Monitor Sum are the same!");
			else
				System.out.println("Producer Sum & Monitor Sum are different!");
			System.out.println("");
			for (SnapShotCheckSum element : snapshoots) {
				System.out.println(element.toString());
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	class Producer implements Runnable {
		private String myName = "";
		public Producer(String name)
		{
			myName = name;
		}
		public void run() {
			try {
				System.out.println("Producer "+ myName + " is running");
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
				System.out.println("Producer "+ myName + " finished putting");
				System.out.println("Producer "+ myName + " is at barrier 1");
				barrier.await();
				System.out.println("Producer "+ myName + " passsed barrier 1");
				// This section adds the data to the ArrayList
				CyclicBarrier partialBarrier = new CyclicBarrier(2);
				PartialMonitor mon = new PartialMonitor(partialQueue, partialBarrier, myName);
				Thread monitor = new Thread(mon);
				System.out.println("Producer "+ myName + " has finished instantiating the partial monitor");
				monitor.start();
				System.out.println("Producer "+ myName + " has reached the partial barrier");
				partialBarrier.await();
				System.out.println("Producer "+ myName + " passed the partial barrier");
				long time = System.nanoTime();
				int i = index.getAndIncrement();
				snapshoots[i] = new SnapShotCheckSum(time, mon.getPartialSum(), sum);
				// --------------------------
				putSum.getAndAdd(sum);
				System.out.println("Producer "+ myName + " has reached barrier 2");
				barrier.await();
				System.out.println("Producer "+ myName + " has finished");
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	class PartialMonitor implements Runnable {
		private LinkedQueue<Integer> partialQueue;
		private int partialSum = 0;
		CyclicBarrier partialBarrier;
		private String myName = "";

		public PartialMonitor(LinkedQueue<Integer> initPartialQueue, CyclicBarrier initPartialBarrier, String name) {
			partialQueue = initPartialQueue;
			this.partialBarrier = initPartialBarrier;
			this.myName = name;
		}

		public void run() {
			try {
				System.out.println("Parital monitor "+ myName + " has started running");
				LinkedQueue.Node<Integer> travel = partialQueue.getHead();
				while (travel.next.get() != null) {
					travel = travel.next.get();
					int element = travel.item;
					partialSum += element;
				}
				System.out.println("Partial Monitor "+ myName + " has finished calculating the partial checksum");
				System.out.println("Partial Monitor "+ myName + " has reached the partial barrier");
				this.partialBarrier.await();
				System.out.println("Partial Monitor "+ myName + " has passed the partial barrier");
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
				System.out.println("Monitor has started running");
				numThreads.getAndIncrement();
				int sum = 0;
				System.out.println("Monitor has reached the first barrier");
				barrier.await();
				System.out.println("Monitor has passed the first barrier");
				LinkedQueue.Node<Integer> travel = queue.getHead();
				while (travel.next.get() != null) {
					travel = travel.next.get();
					int element = travel.item;
					sum += element;
				}
				takeSum.set(sum);
				System.out.println("Monitor has calculated complete checksum");
				System.out.println("Monitor has reached the second barrier");
				barrier.await();
				System.out.println("Monitor has finished");
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
