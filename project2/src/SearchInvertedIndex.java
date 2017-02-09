import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.TreeSet;


public class SearchInvertedIndex extends InvertedIndex {
	
	private ArrayList<ArrayList<String>> queryList;
	private HashMap<String, TreeSet<SearchResult>> results;
	
	/**
	 * Invokes the constructor of its parent class InvertedIndex
	 * and creates a private instance its private HashMap results.
	 */
	public SearchInvertedIndex() {
		super();
		this.results = new HashMap<String, TreeSet<SearchResult>>();
	}
	
	/**
	 * Stores the query list to be searched and calls its helper method to 
	 * search the index for words that start with each query in the query list. 
	 * Creates a HashMap<String, TreeSet<SearchResults> that store the file 
	 * locations, frequency, and initial position of each query.
	 * @param queryList
	 */
	public void callSearchIndex(ArrayList<ArrayList<String>> queryList) {
		this.queryList = queryList;
		for(ArrayList<String> subList: queryList) {
			searchIndex(subList);
		}
	}
	
	/**
	 * Helper method to callSearchIndex()
	 * @param queries
	 */
	private void searchIndex(ArrayList<String> queries) {
		String entireQuery = queries.get(0);
		
		for(int i = 1; i < queries.size(); i++) {
			String query = queries.get(i);
			
			for(String cur = this.invertedIndex.ceilingKey(query); cur != null;
					cur = this.invertedIndex.higherKey(cur)) {
					
				if(cur.startsWith(query)) {

					TreeMap<String, ArrayList<Integer>> map = getInnerMap(cur);
							
					for(String file: map.keySet()) {
						addResult(entireQuery, file, map.get(file));
					}
					
				} else {
						break;
				}
			}
		}
	}
	
	/**
	 * Adds the file, frequency, and initial location of the query found
	 * in the index. If there is already an instance of the word in the 
	 * query, it updates the initial location and frequency.
	 * @param query
	 * @param file
	 * @param positions
	 */
	public void addResult(String query, String file, ArrayList<Integer> positions) {
		TreeSet<SearchResult> set = this.results.get(query);

		if(set == null) {
			set = new TreeSet<SearchResult>();
			SearchResult r = new SearchResult(file);
			r.setFrequency(positions.size());
			r.setInitialPosition(positions.get(0));
			set.add(r);
			results.put(query, set);
		} else {

			for(SearchResult r: set) {	
			
				if(r.getFile().equals(file)) {
					set.remove(r);
					r.setFrequency(positions.size());
					r.setInitialPosition(positions.get(0));
					set.add(r);
					return;
				}
			}
			/* Only reaches this code if no instance of the file is in results */
			
			SearchResult r = new SearchResult(file);
			r.setFrequency(positions.size());
			r.setInitialPosition(positions.get(0));
			set.add(r);
		}
	}
	
	/**
	 * Iterates through the HashMap of stored results and writes them to
	 * the file searchresults.txt. Returns true if it successful writes to 
	 * the file, else writes a error message and returns false.
	 * @return boolean
	 */
	public boolean writeSearchResults() {
		BufferedWriter writer;
		TreeSet<SearchResult> set;
		
		try {
			writer = new BufferedWriter(new FileWriter("searchresults.txt"));
			
			for(ArrayList<String> subList: this.queryList) {
				String word = subList.get(0);
				writer.write(word + "\n");
				set = results.get(word);
			
				if(set == null) {
					writer.write("\n");
					continue;
				}
				
				for(SearchResult r: set) {
					writer.write(r.toString());
				}
				writer.write("\n");
			}
			writer.flush();
			writer.close();
		
		} catch (IOException e) {
			System.out.println("Writer failed. Could not Write the Search " +
					"Results to the output file. Try running again.");
			return false;
		}
		return true;
	}
	
	/**
	 * Returns the inner TreeMap<String, ArrayList<Integer>> stored 
	 * in the index that is linked to the given key if one exists. 
	 * If none is found, it returns null.
	 * @param key
	 * @return TreeMap<String, ArrayList<Integer>>
	 */
	private TreeMap<String, ArrayList<Integer>> getInnerMap(String key) {
		TreeMap<String, ArrayList<Integer>> map = this.invertedIndex.get(key);
		return map;
	}
	
}
