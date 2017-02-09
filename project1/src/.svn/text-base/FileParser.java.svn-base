import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Scanner;


public class FileParser {

	private DirectoryTraverser directory;
	private InvertedIndex index;
	

	public FileParser (InvertedIndex index, DirectoryTraverser directory) {
		this.directory = directory;
		this.index = index;
		
	}
	/**
	 * Calls Helper method parseFiles for each file in the 
	 * ArrayList created by DirectoryTraverser
	 */
	public void parseFiles() {
		for(int i = 0; i < this.directory.getSize(); i++) {
			parseFile(this.directory.getFile(i));
		}
	}
	
	/**
	 * Reads the input file and parses the words in the text, while adding 
	 * them to the inverted index.
	 * @param file
	 */
	public void parseFile(File file) {

		Scanner scan = null;

		try {
			scan = new Scanner(new BufferedReader(new FileReader(file)));
			Integer count = 1;
			
			while(scan.hasNext()) {
				String line = scan.nextLine();
				String words[] = line.split(" ");
				
				for(String word: words) {
					word = word.trim();
					word = word.replaceAll("\\W", "");
					word = word.replaceAll("_", "");
					word = word.toLowerCase();

					if(word.equals("")) {
						continue;
					}
					index.addWord(word, file.getAbsolutePath(), count);
					count++;
				}
			}
		}  catch (FileNotFoundException e) {
			System.out.println("File: " + file.getName() + "Not Found!");
		}
		scan.close();
	}

}