import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.TreeMap;


public class InvertedIndex {
	
	private TreeMap<String, TreeMap<String, ArrayList<Integer>>> invertedIndex;
	
	public InvertedIndex() {
		this.invertedIndex = new TreeMap<String,TreeMap<String, ArrayList<Integer>>>();
	}

	/**
	 * Looks through the files in the directory and sub-directory and 
	 * prints the absolute file path of the file with the specified word
	 * and the locations in that file. Returns true if it was successful
	 * otherwise returns false.
	 * @return
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
	 * @param word
	 * @param filename
	 * @param count
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
	 * Returns a string all of the words, file paths, and word locations in the
	 * inverted index
	 * @return output
	 */
	public String toString() {
		StringBuilder output = new StringBuilder();
		TreeMap<String, ArrayList<Integer>> innerTreeMap;
		ArrayList<Integer> wordLocation;
		
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
		return output.toString();
	}
}
