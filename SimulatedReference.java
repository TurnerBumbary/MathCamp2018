package linkedQueue;

import java.lang.ref.Reference;
public class SimulatedReference<E> {
		private Reference<E> value;

		public synchronized Reference<E> get() {return value;}

		public synchronized Reference<E> compareAndSwap(Reference<E> expectedValue, Reference<E> newValue) {

			Reference<E> oldValue = value;
			if (oldValue == expectedValue) value = newValue;
			return oldValue;

		}

		public synchronized boolean compareAndSet(Reference<E> expectedValue, Reference<E> newValue) {

			return (expectedValue == compareAndSwap(expectedValue, newValue));

		}
}
