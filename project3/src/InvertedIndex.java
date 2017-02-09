import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;


public class InvertedIndex {
	
	protected TreeMap<String, TreeMap<String, ArrayList<Integer>>> invertedIndex;
	protected static MultiReaderLock indexLock;
	
	/**
	 * Constructor to the InvertedIndex.
	 */
	public InvertedIndex() {
		this.invertedIndex = new TreeMap<String,TreeMap<String, ArrayList<Integer>>>();
		indexLock = new MultiReaderLock();
	}

	/**
	 * Reads through the entire inverted index by calling the toString() 
	 * method and writes the result out to invertedindex,txt file. Returns 
	 * true if successful and false otherwise.
	 * @return true if successfully wrote the results to file
	 */
	public boolean writeResults() {
		BufferedWriter writer;
		TreeMap<String, ArrayList<Integer>> innerTreeMap;
		ArrayList<Integer> wordLocation;
		
		try {
			writer = new BufferedWriter(new FileWriter("invertedindex.txt"));
			writer.write(toString());
			writer.flush();
			writer.close();
		
		} catch (IOException e) {
			System.out.println("Writer failed. Could not Write the Inverted " +
					"Index to the output file. Try running again.");
			return false;
		}
		return true;
	}
	
	/**
	 * Checks for the word in the InvertedIndex. If the word is exists, it gets
	 * the TreeMap and adds the filepath and the locations the word is found. 
	 * If it doesn't exist, it creates a new TreeMap and adds the results into 
	 * the inverted index. Synchronization is protected by a write lock.
	 * @param word string to be added
	 * @param filepath file the word is located in
	 * @param count location of the word in the file
	 */
	public void addPartialResults(String filepath, HashMap<String, ArrayList<Integer>> fileResults) {
		indexLock.acquireWriteLock();
		for(String word: fileResults.keySet()) {
			if(!this.invertedIndex.containsKey(word)) {
				TreeMap<String, ArrayList<Integer>> innerTreeMap =
						new TreeMap<String, ArrayList<Integer>>();
				innerTreeMap.put(filepath, fileResults.get(word));
				this.invertedIndex.put(word, innerTreeMap);
	
			} else {
				TreeMap<String, ArrayList<Integer>> innerTreeMap =
						this.invertedIndex.get(word);
				innerTreeMap.put(filepath, fileResults.get(word));	
			}
		}
		indexLock.releaseWriteLock();
	}
	
	/**
	 * Returns a string all of the words, file paths, and word locations in the
	 * inverted index
	 * @return output string of contents in inverted index
	 */
	public String toString() {
		StringBuilder output = new StringBuilder();
		TreeMap<String, ArrayList<Integer>> innerTreeMap;
		ArrayList<Integer> wordLocation;
		
		indexLock.acquireReadLock();
		for(String word: this.invertedIndex.keySet()) {
			output.append(word + "\n");
			innerTreeMap = this.invertedIndex.get(word);
			
			for (String file : innerTreeMap.keySet()) {
				wordLocation = innerTreeMap.get(file);
				String index = wordLocation.toString();
				index = index.substring(1, index.length() - 1);
				output.append('"' + file + '"' + ", " + index + "\n");
			}
			output.append("\n");
		}
		
		indexLock.releaseReadLock();
		return output.toString();
	}
}
