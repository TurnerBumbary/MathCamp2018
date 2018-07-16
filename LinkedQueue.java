package linkedQueue;

import java.util.concurrent.atomic.AtomicReference;
import java.util.Enumeration;
import java.util.Hashtable;

public class LinkedQueue <E> {
	StringBuffer trace = new StringBuffer();
	Hashtable<String, Integer> structMap = new Hashtable<String, Integer>();
	
	private final Node<E> dummy = new Node<E>(null, null);
	private final AtomicReference<Node<E>> head = new AtomicReference<Node<E>>(dummy);
	private final AtomicReference<Node<E>> tail = new AtomicReference<Node<E>>(dummy);
	
	public static class Node <E> {
		public final E item;
		public final AtomicReference<Node<E>> next;
		
		public Node(E item, Node<E> next) {
			this.item = item;
			this.next = new AtomicReference<Node<E>>(next);
		}
	}
	public void clearTrace(){
		trace.delete(0, trace.length());
	}
	public String getTrace(){
		return trace.toString();
	}
	
	public Node<E> getHead()
	{
		return head.get();
	}
	
	public LinkedQueue(){
		structMap.put("Head", 1);
		structMap.put("Tail", 0);
		structMap.put("Dummy", 0);
	}
	
	// Check structural and data integrity
	public boolean isConsistent(){
		Enumeration names; 
		Integer curSize = 0;
		Node<E> curNode = head.get().next.get();

		try {
				// Display Map contents
//				names = structMap.keys(); 
//				while(names.hasMoreElements()) { 
//					String str = (String) names.nextElement(); 
//					String value = structMap.get(str).toString();
//					System.out.println(str + ": " + value); 
//				} 
//				System.out.println();
				
				// Gather data from actual queue
				while(curNode.next.get() != null){
//					System.out.println("Current item:" + curNode.item);
					// If an item was meant to be recorded but was not
					if (!structMap.containsKey(curNode.item.toString())){
						System.out.println("Fail by missing item:" + curNode.item);
						return false;
					}
					curSize++;
					curNode = curNode.next.get();
				}
				// If the sizes of suitable items are different
				if ((curSize + 1) != (structMap.size() - 3)){
					System.out.println("Fail by Missing data: CurSize" + curSize + " & struct Size: " + structMap.size());
					return false;
				}
				// If tail is not at the last node (as recorded by previous traversal)
				if (structMap.get("Tail") == 0 || structMap.get("Tail") != curNode.item){
					System.out.println("Fail by Misdirected Tail pointer");
					return false;
				}
		return true;

		}catch(Exception e){
			throw new RuntimeException(e);
		}
	}
	
	
	public boolean put(E item) {
		Node<E> newNode = new Node<E>(item, null);
		Integer success = 0;
		
		while(true) {
			Node<E> curTail = tail.get();
			Node<E> tailNext = curTail.next.get();
			
			if(curTail == tail.get()) {
				if(tailNext != null) {
					// advance tail
					tail.compareAndSet(curTail, tailNext);
				} else {
					// try inserting new node
					if(curTail.next.compareAndSet(null, newNode)) {
						if(newNode.item instanceof Integer) {
							String tName = Thread.currentThread().getName();
							trace.append("put inserted " + (Integer)newNode.item +
									" by " + tName + "\n");
							success = 1;
							structMap.put(newNode.item.toString(), success);
						}
						// insertion succeeded, try advancing tail
						tail.compareAndSet(curTail, newNode);
						structMap.remove("Tail");
						structMap.put("Tail", (Integer)newNode.item);
						return true;
					}
				}
			}
		}
	}
}
