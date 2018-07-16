package linkedQueue;

public class TEST {

	/**
	 * @param args
	 * @throws InterruptedException

	 */
	private static Thread t1 = new Thread(new Obj());
	public static void main(String[] args) throws InterruptedException {
		t1.start();
		Thread[] threads = new Thread[10];
		for(int i = 0; i < threads.length; i++)
		{
			threads[i] = new Thread(new Obj2());
			threads[i].start();
		}
		System.out.println(t1.getState());
	}
	static class Obj2 implements Runnable {
		public void run()
		{
			System.out.println()
			t1.run();
			t1.run();
		}
	}

	static class Obj implements Runnable {
		public void run() {
			for (int i = 0; i < 10; i++) {
				System.out.println("I am running");
			}
			System.out.println();
		}
	}

}
