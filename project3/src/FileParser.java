import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import org.apache.log4j.Logger;


public class FileParser {

	private static Logger log = Logger.getLogger(FileParser.class);
	private DirectoryTraverser directory;
	private InvertedIndex index;
	private WorkQueue workers;
	private int pending;

	/**
	 * Constructor of the FileParser.
	 * @param index inverted index to store the words found in the files
	 * @param directory contains the list of files to be parsed
	 * @param workers the work queue to perform parsing of files
	 */
	public FileParser (SearchInvertedIndex index, DirectoryTraverser directory,
			WorkQueue workers) {
		this.directory = directory;
		this.index = index;
		this.workers = workers;
		this.pending = 0;
	}
	
	/**
	 * Adds all of the files that need to be parsed to the work queue
	 * and then waits for work to be completed.
	 */
	public void parseFiles() {
		for(int i = 0; i < this.directory.getSize(); i++) {
			workers.execute(new FileParserWorker(this.directory.getFile(i)));
		}

		while(getPending() > 0) {
			log.debug("Still working on file parsing...");

			synchronized(this) {
				try {
					wait(1500);
				} catch (InterruptedException ex) {
					log.warn("Interrupted while waiting.", ex);
				}
			}
			log.debug("File parsing work complete.");
		}

	}


	/**
	 * Parses the file passed in line by line, stores the results and 
	 * returns them as an ArrayList<String>. 
	 * @param file the query file to be parsed
	 * @return ArrayList<String> list of parsed queries
	 */
	public ArrayList<ArrayList<String>> parseQueryFile(String file) {

		Scanner scan = null;
		ArrayList<ArrayList<String>> queryList = new ArrayList<ArrayList<String>>();

		try {
			scan = new Scanner(new BufferedReader(new FileReader(file)));

			while(scan.hasNext()) {
				String line = scan.nextLine();
				ArrayList<String> subQueries = new ArrayList<String>();
				String[] queries = line.split(" ");
				subQueries.add(line);

				for(String query: queries) {
					query = query.trim();
					query = query.replaceAll("\\W", "");
					query = query.replaceAll("_", "");
					query = query.toLowerCase();
					subQueries.add(query);
				}

				queryList.add(subQueries);
			}
		}  catch (FileNotFoundException e) {
			System.out.println("Query File: " + file + "Not Found!");
			System.exit(0);
		}
		scan.close();

		if(queryList.isEmpty()) {
			System.out.println("Query file did not contain any queries. Please " +
					"ensure that query file contains values and try again.");
			System.exit(0);
		}
		return queryList;
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
	 * Private class FileParserWorker implements Runnable in order to 
	 * implement the work to be done by the threads in the work queue. 
	 * 
	 * @author Alejandro Zepeda
	 *
	 */
	private class FileParserWorker implements Runnable {
		private File file;
		private HashMap<String, ArrayList<Integer>> partialIndex;

		/**
		 * Constructor of the FileParserWorker.
		 * @param file the file to parsed
		 */
		public FileParserWorker(File file) {
			this.file = file;
			this.partialIndex = new HashMap<String, ArrayList<Integer>>();
			updatePending(1);
		}

		/**
		 * Overrides the run function of Runnable and calls functions to
		 * parse the file.
		 */
		@Override
		public void run() {
			log.debug("Starting to parse \"" + file + "\"");
			parseFile(file);
			index.addPartialResults(file.getAbsolutePath(), partialIndex);
			updatePending(-1);
			log.debug("Finished parsing \"" + file + "\"");
		}

		/**
		 * Reads the file, removes unwanted characters and spaces, then 
		 * calls addWord to add the word and its location into the HashMap.
		 * @param file to be read and parsed
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
						addWord(word, count);
						count++;
					}
				}
			}  catch (FileNotFoundException e) {
				System.out.println("File: " + file.getName() + "Not Found!");
			}
			scan.close();
		}

		/**
		 * Checks to see if the HashMap contains an ArrayList for the word 
		 * if so, adds the current location of the word found. If not, creates 
		 * a new ArrayList and stores the location in the file and adds it to 
		 * the HashMap.
		 * @param word to be added into the HashMap
		 * @param count word's location in the file
		 */
		public void addWord(String word, int count) {
			if(!this.partialIndex.containsKey(word)) {
				ArrayList<Integer> wordLocations = new ArrayList<Integer>();
				wordLocations.add(count);
				partialIndex.put(word, wordLocations);

			} else {
				ArrayList<Integer> wordLocations = this.partialIndex.get(word);
				wordLocations.add(count);
			}
		}

	}

}