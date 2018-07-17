package linkedQueue;

import java.util.concurrent.atomic.AtomicInteger;
import java.io.*;

public class CheckSumMCv10 {
	LinkedQueue<Integer> queue;
	private final int nElements, nProducers;
	private static Thread[] threadArray;
	private AtomicInteger putSums = new AtomicInteger(0);
	private SnapShotCheckSum[] snapshots;
	Thread m;
	private int index = 0;
	LinkedQueue.Node<Integer> travel;
	private int[] array;

	public CheckSumMCv10() {
		this.queue = new LinkedQueue<Integer>();
		this.nElements = 1;
		this.nProducers = 2;
		threadArray = new Thread[nProducers];
		snapshots = new SnapShotCheckSum[nProducers * nElements];
		array = new int[nProducers * nElements];
		Runnable monitor = new Monitor();
		m = new Thread(monitor);
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		CheckSumMCv10 queueTest = new CheckSumMCv10();
		queueTest.test();
	}

	public void test() {
		try {
			for (int i = 0; i < nProducers; i++) {
				threadArray[i] = new Thread(new Producer(i));
				threadArray[i].start();
			}
			for (int i = 0; i < nProducers; i++) {
				threadArray[i].join();
			}
			int[] array = snapshots[0].getArray();
			int takeSum = 0;
			for (int i = 0; i < array.length; i++) {
				takeSum += array[i];
			}
			int putSum = putSums.get();
			if (putSum == takeSum)
				System.out.println("Correct");
			else
				System.out.println("Error");
			File file = new File("output.txt");
			if(!file.exists())
				file.createNewFile();
			FileWriter fw = new FileWriter(file, true);
			BufferedWriter out = new BufferedWriter(fw);
			for (SnapShotCheckSum element : snapshots)
			{
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

		public Producer(int initPriority) {
			priority = initPriority;
		}

		public void run() {
			try {
				int sum = 0;
				for (int i = 0; i < nElements; i++) {
					Integer seed = (int) (Math.random() * 10 + 1);
					seed = xorShift(seed);
					queue.put(seed);
					sum += seed;
				}
				putSums.getAndAdd(sum);
				// Beginning of the monitor invocation
				for (int i = 0; i < priority; i++) {
					threadArray[i].join();
				}
				m = new Thread(new Monitor());
				m.start();
				m.join();
				/*
				 * try { m.join(); } catch (InterruptedException e) {
				 * e.printStackTrace(); }
				 * System.out.print("Monitor state after join: " +
				 * m.getState());
				 */
				// End of the monitor invocation
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	class Monitor implements Runnable {
		public void run() {
			try {
				if (index == 0) {
					travel = queue.getHead().next.get();
				} else {
					travel = travel.next.get();
				}
				array[index] = travel.item;
				SnapShotCheckSum element = new SnapShotCheckSum(
						System.nanoTime(), array);
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
		private final long time;
		private int[] array;

		public SnapShotCheckSum(long initTime, int[] initArray) {
			time = initTime;
			array = initArray;
		}

		public long getTime() {
			return time;
		}

		public int[] getArray() {
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
			return "[" + time + ", " + arrayToString() + "]";
		}
	}
}
