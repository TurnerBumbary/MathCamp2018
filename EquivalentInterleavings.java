/*
 * This code checks for duplicates
 */
package output;

import java.util.ArrayList;
import java.util.ListIterator;
import java.util.Scanner;
import java.io.*;


public class EquivalentInterleavings {

	/**
	 * @param args
	 */
	private int nElements;
	private int nThreads;
	private int nInterleavings;
	private int size;
	
	public EquivalentInterleavings() {
		nElements = 2;
		nThreads = 2;
		size = nElements * nThreads;
		nInterleavings = 286;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		EquivalentInterleavings reduction = new EquivalentInterleavings();
		reduction.reduce();
	}

	public void reduce() {
		try {
			Scanner infile = new Scanner(new File("output.txt"));
			ArrayList<String> interleavings = new ArrayList<String>();
			for (int i = 0; i < nInterleavings; i++) { // adds the interleavings
														// to an array list
				String interleaving = "";
				for (int a = 0; a < size; a++) {
					interleaving += infile.nextLine() + "\n";
				}
				interleavings.add(interleaving);
			}
			infile.close();
			if (interleavings.size() == nInterleavings)
				System.out.println("Correctly added to array list");
			else
				System.out.println("Error with adding to array list");
			for (int i = 0; i < nInterleavings; i++) {
				if (interleavings.size() > i) {
					String originalInterleavings = interleavings.get(i);
					ListIterator<String> it = interleavings.listIterator();
					while (it.hasNext()) {
						if (it.next().equals(originalInterleavings))
						{
							it.remove();
						}
					}
					interleavings.add(i, originalInterleavings);
				}
			}
			// This section of code writes the updated interleavings to file
			File file = new File("reducedOutput.txt");
			file.createNewFile();
			BufferedWriter out = new BufferedWriter(new FileWriter(file));
			out.write("" + interleavings.size());
			out.newLine();
			for (String interleaving : interleavings) {
				String[] lines = interleaving.split("\n");
				for(int i = 0; i < lines.length; i++)
				{
					out.write(lines[i]);
					out.newLine();
				}
			}
			out.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

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
			int putSum = Integer.parseInt(putSumString);
			int takeSum = Integer.parseInt(takeSumString);
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
			if (o.partialPutSum == this.partialPutSum
					&& o.partialTakeSum == this.partialTakeSum) {
				for (int i = 0; i < (nElements * nThreads); i++) {
					if (o.array[i] != this.array[i]) {
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
