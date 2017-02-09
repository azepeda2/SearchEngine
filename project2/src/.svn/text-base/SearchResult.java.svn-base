

public class SearchResult implements Comparable<SearchResult>{
	
	private int frequency;
	private int initial_position;
	private String file;

	/**
	 * Constructor that takes in a String value in order to set
	 * the file that this SearchResult Object represents
	 * @param file
	 */
	public SearchResult(String file) {
		this.frequency = 0;
		this.initial_position = 0;
		this.file = file;
	}
	
	/**
	 * Returns the frequency of the words that begin with the query 
	 * in the file.
	 * @return
	 */
	public int getFrequency() {
		return frequency;
	}
	
	/**
	 * Adds the given frequency of the words that begin with the query
	 * to the current frequency.
	 * @param newFrequency
	 */
	public void setFrequency(int newFrequency) {
		frequency += newFrequency;
	}
	
	/**
	 * Returns the first position of a word that matches the query
	 * in the file.
	 * @return
	 */
	public int getInitialPosition() {
		return initial_position;
	}
	
	/**
	 * Checks to see if the initial_position was set or if the first
	 * position of the current word is earlier than the current 
	 * initial_position and replaces it if either case is true.
	 * @param position
	 */
	public void setInitialPosition(int position) {
		if(initial_position == 0 || position < initial_position) {
			initial_position = position;
		}
	} 
	
	/**
	 * Returns the String that contains the name of the file.
	 * @return
	 */
	public String getFile() {
		return file;
	}
	
	/**
	 * Returns a String representing the data stored in the SearchResult object
	 * @return
	 */
	@Override 
	public String toString() {
		return ('"' + file + '"' + ", " + frequency + ", " + initial_position + "\n");
	}

	/**
	 * The compareTo method used by the TreeSet in order to properly compare 
	 * and sort the SearchResults.
	 * @param r
	 */
	@Override
	public int compareTo(SearchResult r) {
		
		if(this.frequency > r.getFrequency()) {
			return -1;
		} else if (this.frequency < r.getFrequency()) {
			return 1;
		} else if (this.initial_position < r.getInitialPosition()) {
			return -1;
		} else if (this.initial_position > r.getInitialPosition()) {
			return 1;
		} else {
			return this.file.compareTo(r.getFile());
		}
		
	}

}
