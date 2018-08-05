package output;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.ListIterator;
import java.util.Scanner;
import java.io.*;

import output.EquivalentInterleavingsV3.SnapShotCheckSum;

public class Condition1 {

	/**
	 * @param args
	 */
	private int nElements;
	private int nThreads;
	private int nInterleavings;
	private int size;
	private ArrayList<SnapShotCheckSum[]> inverseInterleavings;

	public Condition1() {
		nElements = 1;
		nThreads = 2;
		size = nElements * nThreads;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Condition1 reduction = new Condition1();
		reduction.reduce();
	}

	public void reduce() {
		try {
			ArrayList<SnapShotCheckSum[]> interleavings = new ArrayList<SnapShotCheckSum[]>();
			Scanner infile = new Scanner(new File("reducedOutput.txt"));
			nInterleavings = Integer.parseInt(infile.nextLine());
			for (int i = 0; i < nInterleavings; i++) {
				interleavings.add(parseInfo(infile));
			} // adds all of the interleavings to an ArrayList
			inverseInterleavings = (ArrayList<SnapShotCheckSum[]>) interleavings
					.clone();
			for (int a = 0; a < nInterleavings; a++) {
				inverseInterleavings.set(
						a,
						Arrays.copyOf(interleavings.get(a),
								interleavings.get(a).length));
			}
			for (SnapShotCheckSum[] snapshot : inverseInterleavings) {
				for (SnapShotCheckSum element : snapshot) {
					String[] array = element.getArray();
					for (int a = 0; a < array.length; a++) {
						if (array[a].trim().equals("T0"))
							array[a] = "T1";
						else if (array[a].trim().equals("T1"))
							array[a] = "T0";
					}
					System.out.println(element);
				}
			}
			System.out.println("Beginning Condition 1");
			System.out.println("--------Break--------");
			for (int i = 0; i < nInterleavings; i++) {
				if (interleavings.size() > i) {
					SnapShotCheckSum[] original = inverseInterleavings.get(i);
					ListIterator<SnapShotCheckSum[]> it = interleavings
							.listIterator();
					while (it.hasNext()) {
						boolean equals = true;
						SnapShotCheckSum[] current = it.next();
						for (int a = 0; a < size; a++) {
							if (!current[a].equals(original[a]))
								equals = false;
						}
						if (equals == true) {
							it.remove();
							System.out.println("Removed");
						}
					}
					System.out.println("------Break------");
				}
			}
			File file = new File("condition1.txt");
			file.createNewFile();
			BufferedWriter out = new BufferedWriter(new FileWriter(file));
			out.write("" + interleavings.size());
			out.newLine();
			for (SnapShotCheckSum[] interleaving : interleavings) {
				for (SnapShotCheckSum snapshot : interleaving) {
					out.write(snapshot.toString());
					out.newLine();
				}
			}
			out.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void condition1() {

	}

	public void condition2() {

	}

	public SnapShotCheckSum[] parseInfo(Scanner infile) {
		SnapShotCheckSum[] array = new SnapShotCheckSum[size];
		for (int i = 0; i < size; i++) {
			String line = infile.nextLine();
			String putSumString = line.substring(line.indexOf('[') + 1,
					line.indexOf(','));
			line = line.substring(line.indexOf(',') + 1);
			String takeSumString = line.substring(0, line.indexOf(','));
			line = line.substring(line.indexOf('[') + 1);
			String arrayString = line.substring(0, line.indexOf(']'));
			int putSum = Integer.parseInt(putSumString.trim());
			int takeSum = Integer.parseInt(takeSumString.trim());
			String[] stringArray = arrayString.split(", ");
			array[i] = new SnapShotCheckSum(putSum, takeSum, stringArray);
		}
		return array;
	}

	class SnapShotCheckSum {
		private String[] array;
		private int partialPutSum;
		private int partialTakeSum;
		private boolean correct;

		public SnapShotCheckSum(int initPartialPutSum, int initPartialTakeSum,
				String[] initArray) {
			array = initArray;
			partialPutSum = initPartialPutSum;
			partialTakeSum = initPartialTakeSum;
			correct = partialPutSum == partialTakeSum;
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

		public boolean equals(SnapShotCheckSum o) {
			if (this.correct == o.correct) {
				for (int i = 0; i < this.array.length; i++) {
					if (!o.array[i].trim().equals(this.array[i].trim())) {
						return false;
					}
				}
				return true;
			} else {
				return false;
			}
		}
	}

}
