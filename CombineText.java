package output;
import java.util.Scanner;
import java.io.*;

public class CombineText {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			File file = new File("output.txt");
			if(!file.exists())
				file.createNewFile();
			BufferedWriter out = new BufferedWriter(new FileWriter(file));
			for(int i = 1; i <= 80; i++)
			{
				System.out.println("Starting file: output" + i + ".txt");
				Scanner infile = new Scanner(new File("output" + i + ".txt"));
				while(infile.hasNextLine())
				{
					String print = infile.nextLine();
					out.write(print);
					out.newLine();
				}
				infile.close();
			}
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// TODO Auto-generated method stub

	}

}
