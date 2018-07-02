package linkedQueue;


import gov.nasa.jpf.jvm.Verify;
import java.util.*;

public class TestLinkedQueue {
	

	static class Producer implements Runnable {
		private String name;
		private LinkedQueue<Integer> queue;
		private int[] content;
		public Producer(String name, LinkedQueue<Integer> queue, int[] content){
			this.name = name;
			this.queue = queue;
			this.content = content;
		}
		public String getName() {return new String(name);}
		public void run() {
			try {
				for(int i=0; i<content.length; i++){
					//System.out.println("Thread " + name + " putting " + i);
					queue.put(content[i]);
				}
				
				//System.out.println("\n Thread " + name + " finished\n");
			}catch(Exception e){
				throw new RuntimeException(e);
			}
		}
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		//TestLinkedQueue test = new TestLinkedQueue();
		Verify.resetCounter(0);
		LinkedQueue<Integer> queue = new LinkedQueue<Integer>();
		
		// Generation of varying-size inputs
		int index = 0;
		int total_items = 2; // Total random numbers per thread
		int[] p1 = new int[total_items];
		int[] p2 = new int[total_items];
		
		while (index < total_items){
			p1[index] = index; 
			index++;
		}

		for (int i = 0; i < total_items;i++){
			p2[i] = i + index; 
		}
		
		Producer prod1 = new Producer("T1", queue, p1);
		Producer prod2 = new Producer("T2", queue, p2);
		Thread t1 = new Thread(prod1);
		Thread t2 = new Thread(prod2);
		t1.setName(prod1.getName());
		t2.setName(prod2.getName());
		t1.start();
		t2.start();
		try {
			t1.join();
			t2.join();
			Verify.incrementCounter(0);
			int verifCounter = Verify.getCounter(0);
			System.out.println(queue.getTrace());
			
			// Consistency (Well-formedness) check
			if(queue.isConsistent()){
				System.out.println("\n Successful Check finished\n");
			}
			System.out.println("\n VerifyCounter = " + verifCounter + "\n");			
		}catch(InterruptedException ex){
				ex.printStackTrace();
		}
		
	}

}
