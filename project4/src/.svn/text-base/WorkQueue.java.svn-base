import java.util.LinkedList;

import org.apache.log4j.Logger;

public class WorkQueue {
    private final PoolWorker[] workers;
    private final LinkedList<Runnable> queue;
    private volatile boolean shutdown;
	private static Logger log = Logger.getLogger(WorkQueue.class);

	/**
	 * Constructor of the WorkQueue.
	 * @param threads number of threads to be created
	 */
    public WorkQueue(int threads) {
    		if(threads < 1) {
    			log.error("Only " + threads + " worker threads specified. " +
    					  "Must have at least one worker thread.");
    			System.out.println(threads + " specified. Please enter at least" +
    							   " 1 thread and try again.");
    			System.exit(0);
    		}

        this.queue   = new LinkedList<Runnable>();
        this.workers = new PoolWorker[threads];

        shutdown = false;

        for(int i = 0; i < threads; i++) {
            workers[i] = new PoolWorker();
            workers[i].start();
        }

        log.debug("WorkQueue initialized with " + threads + " threads.");
    }

    /**
     * Adds work to the work queue. Synchronized by the using synchronized on 
     * the queue object itself.
     * @param r work to be completed
     */
    public void execute(Runnable r) {
        synchronized(queue) {
            queue.addLast(r);
            queue.notify();
        }
    }

    /**
     * Shuts down the work queue after allowing any currently executing
     * threads to finish first. Might leave unfinished work in queue if not 
     * called before work is completed.
     */
    public void shutdown() {
        log.debug("Attempting shutdown of all threads.");
        shutdown = true;

        synchronized(queue) {
        		queue.notifyAll();
        }
    }

    /**
	 * Private class PoolWorker implements Runnable in order to 
	 * implement the work to be done by the threads in the work queue. 
	 * 
	 * @author IBM with some code added
	 *
	 */
    private class PoolWorker extends Thread {
    	
    	@Override
        public void run() {
            Runnable r = null;

            while(true) {
                synchronized(queue) {
                    while(queue.isEmpty() && !shutdown) {
                        try {
                            log.debug("Waiting for work...");
                            queue.wait();
                        }
                        catch (InterruptedException ex) {
                            log.debug("Interrupted while waiting for work.");
                        }
                    }

                    if(shutdown) {
                        log.debug("Shutdown detected.");
                        break;
                    }
                    else {
                        assert !queue.isEmpty() : "Work queue empty.";
                        r = queue.removeFirst();
                    }
                }

                try {
                    log.debug("Found work!");
                    assert r != null : "Runnable object null.";
                    r.run();
                }
                catch (RuntimeException ex) {
                    log.error("Encountered exception running work.", ex);
                }
            }

            log.debug("Thread terminating.");
        }
    }
}
