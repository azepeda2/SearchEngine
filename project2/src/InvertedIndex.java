import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.TreeMap;


public class InvertedIndex {
	
	protected TreeMap<String, TreeMap<String, ArrayList<Integer>>> invertedIndex;
	
	public InvertedIndex() {
		this.invertedIndex = new TreeMap<String,TreeMap<String, ArrayList<Integer>>>();
	}

	/**
	 * Attempts to write the results stored in the InvertedIndex to the 
	 * invertedindex.txt file. Returns true if it was successful
	 * otherwise returns false.
	 * @return true if successful writes to file
	 */
	public boolean writeResults() {
		BufferedWriter writer;
		TreeMap<String, ArrayList<Integer>> innerTreeMap;
		ArrayList<Integer> wordLocation;
		
		try {
			writer = new BufferedWriter(new FileWriter("invertedindex.txt"));
			
			for(String word: this.invertedIndex.keySet()) {
				writer.write(word + "\n");
				innerTreeMap = this.invertedIndex.get(word);
				
				for (String file : innerTreeMap.keySet()) {
					wordLocation = innerTreeMap.get(file);
					String index = wordLocation.toString();
					index = index.substring(1, index.length() - 1);
					writer.write('"' + file + '"' + ", " + index + "\n");
				}
				writer.write("\n");
			}
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
	 * the treemap and adds the filepath and the location the word is found.
	 * @param word the word to be added to index
	 * @param filename file word was found in
	 * @param count the index of the word in the file
	 */
	public void addWord(String word, String filename, int count) {
		
		if(!this.invertedIndex.containsKey(word)) {
			ArrayList<Integer> wordLocation = new ArrayList<Integer>();
			wordLocation.add(count);
			TreeMap<String, ArrayList<Integer>> innerTreeMap = 
					new TreeMap<String, ArrayList<Integer>>();
			innerTreeMap.put(filename, wordLocation);
			invertedIndex.put(word, innerTreeMap);
			

		} else {
			TreeMap<String, ArrayList<Integer>> innerTreeMap =
					this.invertedIndex.get(word);
			
			if(innerTreeMap.containsKey(filename)) {
				innerTreeMap.get(filename).add(count);
			
			} else {
				ArrayList<Integer> wordLocation = new ArrayList<Integer>();
				wordLocation.add(count);
				innerTreeMap.put(filename, wordLocation);
			}
		}

	}
	
	/**
	 * Prints all of the words, file paths, and word locations in the
	 * inverted index.
	 */
	public void printEntireIndex() {
		TreeMap<String, ArrayList<Integer>> innerTreeMap;
		ArrayList<Integer> wordLocation;
		
		
		for(String word: this.invertedIndex.keySet()) {
			System.out.print(word + "\n");
			innerTreeMap = this.invertedIndex.get(word);
			
			for (String file : innerTreeMap.keySet()) {
				wordLocation = innerTreeMap.get(file);
				String index = wordLocation.toString();
				index = index.substring(1, index.length() - 1);
				System.out.print('"' + file + '"' + ", " + index + "\n");
			}
			System.out.print("\n");
		}
	}
}
