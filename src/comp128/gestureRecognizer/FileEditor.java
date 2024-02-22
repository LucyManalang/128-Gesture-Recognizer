package comp128.gestureRecognizer;

import java.io.FileWriter; 
import java.io.IOException;

/**
 * Class that writes to .csv files. Partial credit to W3 schools and Stack Overflow.
 */
public class FileEditor {
    
	/**
	 * Writes to .csv files
	 * @param fileName the name of the file, could possibly not work on other computers but I have no way of testing that
	 * @param inputString the String that is being written
	 */
	public static void WriteFile(String fileName, String inputString) {
		try {
			FileWriter myWriter = new FileWriter("res/" + fileName + ".xml", true);
			myWriter.write(inputString);
			myWriter.close();
		} catch (IOException e) {
			System.out.println("An error occurred.");
			e.printStackTrace();
		}
	}	
}