//Note this implementation will only support each thread inserting exactly one node into the linkedqueue
package linkedQueue;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.*;
import java.io.*;
import java.util.concurrent.CyclicBarrier;

public class CheckSumMCv18 {
	LinkedQueue<Integer> queue; // stores all data
	private final int nElements, nProducers; // constants defined in constructor
	private static Thread[] threadArray;
	private AtomicInteger putSums = new AtomicInteger(0);
	private AtomicInteger counter = new AtomicInteger(0);
	private ConcurrentHashMap<Integer, String> toThread = new ConcurrentHashMap<Integer, String>();
	private ConcurrentHashMap<String, Integer> toPutSum = new ConcurrentHashMap<String, Integer>();
	private int takeSum = 0;
	CyclicBarrier barrier = null;
	private SnapShotCheckSum[] snapshots; // record snapshots
	// ------------The following constant are all sued by the monitor
	// thread-------------
	Thread m; // monitor thread
	private int index = 0; // used by monitor thread
	LinkedQueue.Node<Integer> travel; // used by monitor thread
	private String[] array;

	public CheckSumMCv18() {
		this.queue = new LinkedQueue<Integer>();
		this.nElements = 1;
		this.nProducers = 2;
		threadArray = new Thread[nProducers];
		snapshots = new SnapShotCheckSum[nProducers * nElements];
		array = new String[nProducers * nElements];	
		barrier = new CyclicBarrier(nProducers);
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		CheckSumMCv18 queueTest = new CheckSumMCv18();
		queueTest.test();
	}

	public void test() {
		try {
			for (int i = 0; i < nProducers; i++) {
				threadArray[i] = new Thread(new Producer("T" + i, i));
				threadArray[i].start();
			}
			for (int i = 0; i < nProducers; i++) {
				threadArray[i].join();
			}
			if (putSums.get() == takeSum) {
				System.out.println("Correct");
			} else
				System.out.println("Error");
			File file = null;
			int i = 1;
			do {
				file = new File("output" + i + ".txt");
				i++;
			} while (file.exists());
			file.createNewFile();
			BufferedWriter out = new BufferedWriter(new FileWriter(file));
			for (SnapShotCheckSum element : snapshots) {
				out.write(element.toString());
				out.newLine();
			}
			out.close();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	class Producer implements Runnable {
		private int priority = 0;
		private String myName;

		public Producer(String initName, int initIndex) {
			myName = initName;
			priority = initIndex;
		}

		public void run() {
			try { 
				for (int i = 0; i < nElements; i++) {
					Integer seed = counter.getAndIncrement();
					seed = xorShift(seed);
					queue.put(seed);
					putSums.getAndAdd(seed);
					toThread.putIfAbsent(seed, myName);
					toPutSum.putIfAbsent(myName, seed);
				}
				barrier.await();
				// ------------Beginning of the monitor
				// invocation--------------------
				for (int i = 0; i < priority; i++) {
					threadArray[i].join();
				}
				m = new Thread(new Monitor());
				m.start();
				m.join();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	class Monitor implements Runnable {
		private int partialTakeSum = 0;
		public void run() {
			try {
				if (index == 0) {
					travel = queue.getHead().next.get();
				} else {
					travel = travel.next.get();
				}
				array[index] = toThread.get(travel.item);
				takeSum += travel.item;
				partialTakeSum = travel.item;
				int partialPutSum = toPutSum.get(array[index]);
				SnapShotCheckSum element = new SnapShotCheckSum(partialPutSum,
						partialTakeSum, array);
				snapshots[index] = element;
				index++;
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
		private String[] array;
		private int partialPutSum;
		private int partialTakeSum;

		public SnapShotCheckSum(int initPartialPutSum, int initPartialTakeSum,
				String[] initArray) {
			array = initArray;
			partialPutSum = initPartialPutSum;
			partialTakeSum = initPartialTakeSum;
		}

		public String[] getArray() {
			return array;
		}

		public String arrayToString() {
			String toReturn = "[";
			for (int i = 0; i < array.length - 1; i++) {
				toReturn += array[i] + ", ";
			}
			toReturn += array[array.length - 1] + "]";
			return toReturn;
		}

		public String toString() {
			String print = "[" + partialPutSum + ", " + partialTakeSum + ", "
					+ arrayToString() + "]";
			if (partialPutSum == partialTakeSum) {
				print += " true";
			} else {
				print += " false";
			}
			return print;
		}
	}

}
