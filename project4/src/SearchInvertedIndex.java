import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.log4j.Logger;


public class SearchInvertedIndex extends InvertedIndex {
	
	private static Logger log = Logger.getLogger(SearchInvertedIndex.class);
	private ArrayList<ArrayList<String>> queryList;
	private HashMap<String, TreeSet<SearchResult>> results;
	private MultiReaderLock resultsLock;
	private WorkQueue workers;
	private int pending;
	
	/**
	 * Invokes the constructor of its parent class InvertedIndex
	 * and creates a private instance its private HashMap results.
	 */
	public SearchInvertedIndex(WorkQueue workers) {
		super();
		this.results = new HashMap<String, TreeSet<SearchResult>>();
		this.resultsLock = new MultiReaderLock();
		this.workers = workers;
		this.pending = 0;
	}
	
	/**
	 * Stores the query list to be searched and creates a SearchWorker that adds 
	 * a query to be searched by the worker threads. 
	 * @param queryList list of queries to be searched
	 */
	public void callSearchIndex(ArrayList<ArrayList<String>> queryList) {
		this.queryList = queryList;
		for(ArrayList<String> subList: queryList) {
			workers.execute(new SearchWorker(subList));
		}
		
		while(getPending() > 0) {
			log.debug("Still searching index...");

			synchronized(this) {
				try {
					wait(1500);
				} catch (InterruptedException ex) {
					log.warn("Interrupted while waiting.", ex);
				}
			}
		}
		
		log.debug("Searching index complete.");
	}
	
	/**
	 * Adds the partial results of the worker thread to the shared HashMap of 
	 * results. Synchronized by a write lock.
	 * @param query full query that was searched
	 * @param set the results of the search query
	 */
	private void addPartialResults(String query, TreeSet<SearchResult> set) {
		this.resultsLock.acquireWriteLock();
		this.results.put(query, set);
		this.resultsLock.releaseWriteLock();
	}
	
	/**
	 * Iterates through the HashMap of stored results and writes them to
	 * the file searchresults.txt. Returns true if it successful writes to 
	 * the file, else writes a error message and returns false. Synchronized 
	 * by a read lock.
	 * @return boolean true if write is successful
	 */
	public boolean writeSearchResults() {
		BufferedWriter writer = null;
		TreeSet<SearchResult> set;
		
		try {
			writer = new BufferedWriter(new FileWriter("searchresults.txt"));
			resultsLock.releaseReadLock();
			
			for(ArrayList<String> subList: this.queryList) {
				String query = subList.get(0);
				writer.write(query + "\n");
				set = results.get(query);
			
				if(set == null) {
					writer.write("\n");
					continue;
				}
				
				for(SearchResult r: set) {
					writer.write(r.toString());
				}
				
				writer.write("\n");
			}
			
		
		} catch (IOException e) {
			System.out.println("Writer failed. Could not Write the Search " +
					"Results to the output file. Try running again.");
			return false;
		
		} finally {
			
			try {
				resultsLock.releaseReadLock();
				writer.flush();
				writer.close();
			} catch (IOException e) {
				log.debug("Failed to flush and close search results writer.");
			}
		}
		return true;
	}
	

	/**
	 * Used by the main thread to check if there is any pending work to be 
	 * done in the work queue. 
	 * @return pending an int of current work in the work queue
	 */
	private synchronized int getPending() {
		return pending;
	}
	
	/**
	 * Updates the current amount of work in the work queue. Uses synchronized
	 * declaration to ensure updates of shared variable. Notifies all if 
	 * pending becomes less than or equal to 0.
	 * @param amount the change to be added to pending
	 */
	private synchronized void updatePending(int amount) {
		pending += amount;

		if(pending <= 0) {
			notifyAll();
		}
	}
	
	/**
	 * Private class SearchWorker implements Runnable in order to 
	 * implement the work to be done by the threads in the work queue. 
	 * 
	 * @author Alejandro Zepeda
	 *
	 */
	private class SearchWorker implements Runnable {
		private ArrayList<String> queryList;
		private TreeSet<SearchResult> set;
		
		/**
		 * Constructor of the SearchWorker.
		 * @param querylist list of queries to be searched
		 */
		public SearchWorker(ArrayList<String> querylist) {
			this.queryList = querylist;
			this.set = new TreeSet<SearchResult>();
			updatePending(1);
		}

		/**
		 * Overrides the run function of Runnable and calls functions to
		 * search the inverted index for the queries.
		 */
		@Override
		public void run() {
			log.debug("Starting to search for queries \"" + queryList.get(0) + "\"");
			searchIndex(queryList);
			addPartialResults(queryList.get(0), this.set);
			updatePending(-1);
			log.debug("Finished searching for queries \"" + queryList.get(0) + "\"");
		}
		
		/**
		 * Called by run method to search the index for words that start with 
		 * each query in the query list. TreeSet<SearchResults> that store 
		 * the file locations, frequency, and initial position of each query.
		 * Synchronized by the inverted index's read lock.
		 * @param queries list of queries to be searched
		 */
		private void searchIndex(ArrayList<String> queries) {
			
			for(int i = 1; i < queries.size(); i++) {
				String query = queries.get(i);
				indexLock.acquireReadLock();
				
				for(String cur = invertedIndex.ceilingKey(query); cur != null;
						cur = invertedIndex.higherKey(cur)) {
						
					if(cur.startsWith(query)) {

						TreeMap<String, ArrayList<Integer>> map = invertedIndex.get(cur);
								
						for(String file: map.keySet()) {
							addResult(file, map.get(file));
						}
						
					} else {
							break;
					}
				}
				indexLock.releaseReadLock();
			}
		}
		
		/**
		 * Checks to see if the current file is in the SearchResult set, if 
		 * so, updates the results of the query to the SearchResult. Else, 
		 * creates a new SearchResult, updates the results and adds it to
		 * the list.
		 * @param file that was searched
		 * @param positions list of locations of the query in the file
		 */
		public void addResult(String file, ArrayList<Integer> positions) {
			
			for(SearchResult r: set) {	
				
				if(r.getFile().equals(file)) {
					set.remove(r);
					r.setFrequency(positions.size());
					r.setInitialPosition(positions.get(0));
					set.add(r);
					return;
				}
			}

			SearchResult r = new SearchResult(file);
			r.setFrequency(positions.size());
			r.setInitialPosition(positions.get(0));
			set.add(r);
		}
	}	
}