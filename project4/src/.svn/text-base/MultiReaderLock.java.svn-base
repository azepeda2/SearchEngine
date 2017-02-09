import org.apache.log4j.Logger;


public class MultiReaderLock {

	private int writers;
	private int readers;
	private static Logger log = Logger.getLogger(MultiReaderLock.class);
	
	/**
	 * Constructor of the MultiReaderLock.
	 */
	public MultiReaderLock() {
		this.writers = 0;
		this.readers = 0;
	}
	
	/**
	 * If no writers currently have the write lock, increments the number of 
	 * readers and "acquires the lock, else waits until there are no writers.
	 */
	public synchronized void acquireReadLock() {
		while(writers > 0) {
			try {
				wait();
			} catch (InterruptedException e) {
				log.debug("Wait in acquireReadLock was interrupted");
			}
		}
		readers++;
	}
	
	/**
	 * Decrements the number of readers by one, essentially "releasing" the 
	 * read lock. Notifies all waiting threads.
	 */
	public synchronized void releaseReadLock() {
		readers--;
		if(readers <= 0)
			notifyAll();
	}
	
	/**
	 * If no readers or other writers currently have the lock, increments the 
	 * number of writers and "acquires the lock, else waits until there are no 
	 * readers or writers.
	 */
	public synchronized void acquireWriteLock(){
		while(readers > 0 || writers > 0) {
			try {
				wait();
			} catch (InterruptedException e) {
				log.debug("Wait in acquireWritedLock was interrupted");
			}
		}
		writers++;
	}
	
	/**
	 * Decrements the number of writers by one, essentially "releasing" the 
	 * write lock. Notifies all waiting threads.
	 */
	public synchronized void releaseWriteLock(){
		writers--;
		
		if(writers <= 0)
			notifyAll();
	}
}
