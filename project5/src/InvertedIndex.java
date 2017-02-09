import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import java.util.TreeMap;

import org.apache.log4j.Logger;


public class InvertedIndex {
	
	private static Logger log = Logger.getLogger(SearchInvertedIndex.class);
	protected TreeMap<String, TreeMap<String, ArrayList<Integer>>> invertedIndex;
	protected MultiReaderLock indexLock;
	
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
		BufferedWriter writer = null;
		TreeMap<String, ArrayList<Integer>> innerTreeMap;
		ArrayList<Integer> wordLocation;
		
		try {
			writer = new BufferedWriter(new FileWriter("invertedindex.txt"));
			indexLock.acquireReadLock();
			
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
				writer.flush();
			}
		
		} catch (IOException e) {
			System.out.println("Writer failed. Could not Write the Inverted " +
					"Index to the output file. Try running again.");
			return false;
		} finally {
			
			try {
				indexLock.releaseReadLock();
				writer.flush();
				writer.close();
				
			} catch (IOException e) {
				log.debug("Failed to flush and close search results writer.");
			}
		}
		
		return true;
	}
	
	/**
	 * Used in the addPartialIndex() method to retrieve the key set 
	 * of the InvertedIndex.
	 * @return a key set of the InvertedIndex
	 */
	private Set<String> getIndexKeySet() {
		return this.invertedIndex.keySet();
	}
	
	/**
	 * Returns the inner TreeMap<String, ArrayList<Integer>> stored 
	 * in the index that is linked to the given key if one exists. 
	 * If none is found, it returns null.
	 * @param key key of index to be retrieved
	 * @return TreeMap<String, ArrayList<Integer>> index of files and word locations
	 */
	private TreeMap<String, ArrayList<Integer>> getInnerMap(String key) {
		TreeMap<String, ArrayList<Integer>> map = this.invertedIndex.get(key);
		return map;
	}
	
	/**
	 * Adds all of the results stored in the partial inverted index into the 
	 * main inverted index. Synchronization is protected by a write lock.
	 * @param word string to be added
	 * @param filepath file the word is located in
	 * @param count location of the word in the file
	 */
	public void addPartialIndex(String filepath, InvertedIndex fileResults) {
		TreeMap<String, ArrayList<Integer>> indexInnerTreeMap, fileInnerTreeMap;
		indexLock.acquireWriteLock();
		
		for(String word: fileResults.getIndexKeySet()) {
			fileInnerTreeMap = fileResults.getInnerMap(word);
			
			if(!this.invertedIndex.containsKey(word)) {
				this.invertedIndex.put(word, fileInnerTreeMap);
	
			} else {
				indexInnerTreeMap = this.invertedIndex.get(word);
				indexInnerTreeMap.put(filepath, fileInnerTreeMap.get(filepath));	
				
			}
		}
		indexLock.releaseWriteLock();
	}
	
	/**
	 * Prints all of the words, file paths, and word locations in the
	 * inverted index.
	 */
	public void printEntireIndex() {
		TreeMap<String, ArrayList<Integer>> innerTreeMap;
		ArrayList<Integer> wordLocation;
		
		indexLock.acquireReadLock();
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
		
		indexLock.releaseReadLock();
	}
	
	/**
	 * Checks for the word in the InvertedIndex. If the word is exists, it gets
	 * the treemap and adds the filepath and the location the word is found. 
	 * Else it creates a new inner map to store the word and its locations in 
	 * the file.
	 * @param word string found in file
	 * @param filename the file it was found in
	 * @param count the word index in the file
	 */
	public void addWord(String word, String filename, int count) {
		indexLock.acquireWriteLock();
		
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

		indexLock.releaseWriteLock();
	}

}
