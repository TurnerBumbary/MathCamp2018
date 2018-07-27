package linkedQueue;

public class LinkedQueuev2<E> {
	private Node<E> head = null;
	private Node<E> tail = null;
	public E get(int index)
	{
		Node<E> pointer = head;
		for(int i = 0; i < index; i++)
		{
			pointer = pointer.getNext();
		}
		return pointer.getData();
	}
	public void add(E newElement)
	{
		Node<E> newNode = new Node<E>(newElement, null);
		if(head == null)
		{
			head = newNode;
			tail = newNode;
		}
		else
		{
			tail.setNext(newNode);
			tail = newNode;
		}
	}
	public int size()
	{
		int size = 0;
		Node<E> pointer = head;
		while(pointer != null)
		{
			pointer = pointer.getNext();
			size++;
		}
		return size;
	}
	class Node<E>
	{
		private E data;
		private Node<E> next;
		public Node(E object, Node<E> initNext)
		{
			data = object;
			initNext = next;
		}
		public Node<E> getNext()
		{
			return next;
		}
		public void setNext(Node<E> newNext)
		{
			next = newNext;
		}
		public E getData()
		{
			return data;
		}
	}
}
