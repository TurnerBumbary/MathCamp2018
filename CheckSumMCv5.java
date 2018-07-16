package linkedQueue;

import java.util.concurrent.atomic.AtomicInteger;

public class CheckSumMCv5 {
	LinkedQueue<Integer> queue;
	int takeSum = 0;
	private final int nTrials, nProducers;
	private static Thread[] threadArray;
	private AtomicInteger index = new AtomicInteger(0);
	private SnapShotCheckSum[] snapshots;
	private int[] putSums;

	public CheckSumMCv5() {
		this.queue = new LinkedQueue<Integer>();
		this.nTrials = 1;
		this.nProducers = 2;
		threadArray = new Thread[nProducers];
		snapshots = new SnapShotCheckSum[nProducers];
		putSums = new int[nProducers];
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		CheckSumMCv5 queueTest = new CheckSumMCv5();
		queueTest.test();
	}

	public void test() {
		try {
			for (int i = 0; i < nProducers; i++) {
				threadArray[i] = new Thread(new Producer());
				threadArray[i].start();
			}
			for (int i = 0; i < nProducers; i++) {
				threadArray[i].join();
			}
			Thread m = new Thread(new Monitor());
			m.start();
			m.join();
			int putSum = 0;
			for(int i = 0; i < nProducers; i++)
			{
				putSum += putSums[i];
			}
			if (putSum == takeSum)
				System.out.println("Correct");
			else
				System.out.println("Error");
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
				int partialSum = 0;
				LinkedQueue.Node<Integer> travel = partialQueue.getHead();
				while (travel.next.get() != null) {
					travel = travel.next.get();
					int element = travel.item;
					partialSum += element;
				}
				long time = System.nanoTime();
				int i = index.getAndIncrement();
				snapshots[i] = new SnapShotCheckSum(time, partialSum, sum);
				putSums[i] = sum;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	class Monitor implements Runnable {
		public void run() {
			try {
				int sum = 0;
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
