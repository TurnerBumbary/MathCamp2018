package linkedQueue;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.CyclicBarrier;
//import junit.framework.TestCase;

public class SimpleTest {
	LinkedQueue<Integer> queue;
	private final int nTrials, nProducers;
	private AtomicInteger curNumber = new AtomicInteger(0);
	private static Thread[] threadArray;
	CyclicBarrier barrier;
	
	public SimpleTest() {
		this.queue = new LinkedQueue<Integer>();
		this.nTrials = 1;
		this.nProducers = 2;
		threadArray = new Thread[nProducers];
		barrier = new CyclicBarrier(nProducers);
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		SimpleTest queueTest = new SimpleTest();
		queueTest.test();
		System.out.println();
	}

	public void test() {
		try {
			for (int i = 0; i < nProducers; i++) {
				threadArray[i] = new Thread(new Producer());
				threadArray[i].start();
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	class Producer implements Runnable {
		public void run() {
			try {
				for(int i = 0; i < nTrials; i++){
					int a = curNumber.getAndIncrement();
					queue.put(a);
					System.out.println(a);
				}
				barrier.await();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}
}
