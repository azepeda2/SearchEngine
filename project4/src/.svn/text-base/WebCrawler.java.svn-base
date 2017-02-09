import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;


public class WebCrawler {

	private static Logger log = Logger.getLogger(FileParser.class);
	private InvertedIndex index;
	private WorkQueue workers;
	private HashSet<URL> webPages;
	private MultiReaderLock webPagesLock;
	private int pending;
	private URL seed;
	
	/**
	 * Constructor of the WebCrawler.
	 * 
	 * @param index inverted index to store the words found in the files
	 * @param workers the work queue to perform parsing of files
	 * @param seed initial url of the web page to be parsed
	 */
	public WebCrawler(InvertedIndex index, WorkQueue workers, String seed) {
		this.index = index;
		this.workers = workers;
		this.pending = 0;
		this.webPages = new HashSet<URL>();
		this.webPagesLock = new MultiReaderLock();
		try {
			this.seed = new URL(seed);
		} catch (MalformedURLException e) {
			try {
				String adjustedSeed = "http://" + seed;
				this.seed = new URL(adjustedSeed);
			} catch (MalformedURLException ex) {
				System.out.println("Seed url " + seed + " is not valid.");
				System.exit(0);
			}
		}
	}
	
	/**
	 * Adds the seed url to the work queue and waits for the worker threads to 
	 * finishing crawling the web and creating the inverted index.
	 */
	public void beginCrawling() {
		addWebPage(seed);
		
		while(getPending() > 0) {
			log.debug("Still working on web crawling...");

			synchronized(this) {
				try {
					System.out.print(".");
					wait(1500);
				} catch (InterruptedException ex) {
					log.warn("Interrupted while waiting.", ex);
				}
			}
		}
		
		System.out.print("\n");
		
		if(this.webPages.size() == 0) {
			System.out.println("Seed url " + seed.toString() +
					" could not be parsed. Program now exitting.");
			System.exit(0);
		}
		
		log.debug("Web crawling work complete.");
	}
	
	/**
	 * Checks to see that web crawler hasn't reached its max of 50 pages and 
	 * that it doesn't already have the url in the list, if both cases hold 
	 * it adds a new job to the work queue.
	 * @param url webpage to be added to work list
	 */
	public void addWebPage(URL url) {
		this.webPagesLock.acquireWriteLock();
		
		if(this.webPages.size() < 50 && !this.webPages.contains(url)) {
						
			webPages.add(url);
			workers.execute(new WebCrawlerWorker(url));
		}
		
		this.webPagesLock.releaseWriteLock();
	}
	
	public void removeWebPage(URL url) {
		this.webPagesLock.acquireWriteLock();
		webPages.remove(url);
		this.webPagesLock.releaseWriteLock();
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
	 * Private class WebCrawlerWorker implements Runnable in order to 
	 * implement the work to be done by the threads in the work queue. 
	 * 
	 * @author Alejandro Zepeda
	 *
	 */
	private class WebCrawlerWorker implements Runnable {
		private URL url;
		private InvertedIndex partialIndex;

		/**
		 * Constructor of the WebCrawlerWorker.
		 * @param url the webpage to parsed
		 */
		public WebCrawlerWorker(URL url) {
			this.url = url;
			this.partialIndex = new InvertedIndex();
			updatePending(1);
		}

		/**
		 * Overrides the run function of Runnable and calls functions to
		 * parse the web page.
		 */
		@Override
		public void run() {
			log.debug("Starting to parse \"" + url.toString() + "\"");
			
			if(parseWords()) {
				index.addPartialIndex(url.toString(), partialIndex);
			}
			
			updatePending(-1);
			log.debug("Finished parsing \"" + url.toString() + "\"");
		}
		
		/**
		 * Checks for url links in the given string, checks for and removes 
		 * any fragment portion in the url, and calls addWebPage() 
		 * method which adds it to the work queue if it is not already in 
		 * the webPages list.
		 * @param text string that may contain url links
		 */
		public void extractLinks(String text) {
			String regex = "(?i)<a\\s[^>]*href\\s*=\\s*\"([^\"]*)\"[^>]*>";
			
			Pattern p = Pattern.compile(regex);
			Matcher m = p.matcher(text);

			while(m.find()) {
				String link = m.group(1);
				
				try {
					URL url1 = new URL(seed, link);
					
					if(!url1.toString().contains("http")) {
						URL url2 = new URL(url1.getFile());
						addWebPage(url2);
					} else {
						addWebPage(url1);
					}

				} catch (MalformedURLException e) {
					log.debug("Found url " + link + "not valid.");
				}
				
			} 
		}

		/**
		 * Parses a line of text into lowercase words with no symbols, and
		 * adds those words to a list.
		 *
		 * @param buffer text containing words separated by whitespaces
		 * @param count current index of last word found in web page
		 */
		private int parseLine(String buffer, int count) {
			String url = this.url.toString();
			
			for(String word : buffer.split("\\s+")) {
				word = word.toLowerCase().replaceAll("[\\W_]", "").trim();

				if(!word.isEmpty()) {
					partialIndex.addWord(word, url, count);
					count++;
				}
			}
			
			return count;
		}

		/**
		 * Tests whether a start tag exists for the element in the buffer. For
		 * example, <code>startElement("style", buffer)</code> will test if
		 * the buffer contains the start <style> tag.
		 *
		 * @param element the element name, like "style" or "script"
		 * @param buffer the html code being checked
		 * @return true if the start element tag exists in the buffer
		 */
		public boolean startElement(String element, String buffer) {
			String regex = "(<" + element + ")";
			Pattern p = Pattern.compile(regex);
			Matcher m = p.matcher(buffer);

			if(m.find(0)) {
				return true;
			}

			return false;
		}

		/**
		 * Tests whether a close tag exists for the element in the buffer. For
		 * example, <code>closeElement("style", buffer)</code> will test if
		 * the buffer contains the close </style> tag.
		 * 
		 * @param element the element name, like "style" or "script"
		 * @param buffer the html code being checked
		 * @return true if the start element tag exists in the buffer
		 */
		public boolean closeElement(String element, String buffer) {
			String regex = "(</" + element + "+>)";
			Pattern p = Pattern.compile(regex);
			Matcher m = p.matcher(buffer);

			if(m.find(0)) {
				return true;
			}

			return false;
		}

		/**
		 * Removes the element tags and all text between from the buffer. For
		 * example, <code>stripElement("style", buffer)</code> will return a
		 * string with the <style>...</style> tags and all text in between
		 * removed. Both the start and close tags must be present.
		 *
		 * @param element the element name, like "style" or "script"
		 * @param buffer the html code being checked
		 * @return text without the start and close tags and all text in between
		 */
		public String stripElement(String element, String buffer) {
			if(startElement(element, buffer) && closeElement(element, buffer)) {
				String regex = "(i?)(<" + element + ".*/" + element + ">)";
				String strip;
				Pattern p = Pattern.compile(regex);
				Matcher m = p.matcher(buffer);
				
				while(m.find()) {
					strip = m.group(0);
					buffer = buffer.replace(strip, "");
				}
				
			}
				
			return buffer;
		}

		/**
		 * Replaces all HTML entities in the buffer with a single space. For
		 * example, "2010&ndash;2012" will become "2010 2012".
		 *
		 * @param buffer the html code being checked
		 * @return text with HTML entities replaced by a space
		 */
		public String stripEntities(String buffer) {
			String regex = "(i?)(&[a-z0-9]+;|&#[a-z0-9]+;)";
			String entity;
			Pattern p = Pattern.compile(regex);
			Matcher m = p.matcher(buffer);
			
			while(m.find()) {
				entity = m.group(0);
				buffer = buffer.replace(entity, " ");
			}
			
			return buffer;

		}

		/**
		 * Replaces all HTML tags in the buffer with a single space. If there is 
		 * an "<" but no ">", it reads in another line until it finds one. For
		 * example, "hello <b>world</b>!" will become "hello  world !". It also 
		 * calls extractLink() method in the case that there is a link in the 
		 * buffer.
		 *
		 * @param buffer the HTML code being checked
		 * @return text with HTML tags replaced by a space
		 */
		public String stripHTML(String buffer, BufferedReader reader) {
			int start, end;
			String tag;

			while(buffer.contains("<")) {
				
				while(!buffer.contains(">")) {
					try {
						buffer += reader.readLine();
					} catch (IOException e) {
						log.debug("error reading in line");
					}
				}
				
				if(buffer.toLowerCase().contains("href")) {
					extractLinks(buffer);
				}
				
				start = buffer.indexOf("<");
				end = buffer.indexOf(">", start + 1) + 1;
				
				if(start < end) {
					tag = buffer.substring(start, end);
					buffer = buffer.replace(tag, " ");
				} else {
					tag = buffer.substring(0, start);
					buffer = buffer.replace(tag, " ");
				}
			}
			
			return buffer;

		}
		
		/**
		 * Prepares a HTTP request to be sent to the server for the given 
		 * domain and resource.
		 * 
		 * @param resource file to be requested
		 * @param domain hosting web page
		 * @return string representation of request
		 */
		public String requestWebpage(String resource, String domain) {
			StringBuffer request = new StringBuffer();
			
			request.append("GET " + resource + " HTTP/1.1\n");
			request.append("Host: " + domain + "\n");
			request.append("Connection: close\n");
			request.append("\r\n");
			
			return request.toString();
		}

		/**
		 * Opens a socket and downloads the webpage one line at a time,
		 * removing all text between style or script tags, removing HTML tags,
		 * and removing special symbols (HTML entities). Stores only one line
		 * of the webpage at once unless additional lines are necessary to
		 * parse missing close tags. Each cleaned line of text is then parsed
		 * into words and placed into an inverted index along with its url and 
		 * location in the web page. Returns true if successful.
		 * @return true if successful
		 */
		public boolean parseWords() {
			String buffer;
			boolean status;
			Socket socket = null;
			BufferedReader reader = null;
			PrintWriter out = null;
			String domain = url.getHost();
			String resource = url.getFile();
			int count = 1;
			
			if(resource.equals(""))
				resource = "/";
			
			try {
				socket = new Socket(domain, 80);
				reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				out = new PrintWriter(socket.getOutputStream());

				out.write(requestWebpage(resource, domain));
				out.flush();
				
				buffer = reader.readLine();
				
				while(!buffer.isEmpty()) {
					buffer = reader.readLine().toLowerCase();
					if(buffer.contains("content-type:") && !buffer.contains("text/html")) {
						throw new IOException();
					}
				}
				
				
				while((buffer = reader.readLine())!= null) {
					
					if(startElement("script" , buffer)) {
						
						while(!closeElement("script" , buffer)) {
							buffer = buffer + reader.readLine(); 
						}
						
						buffer = stripElement("script", buffer);
					}
					
					if(startElement("style" , buffer)) {
						
						while(!closeElement("style" , buffer)) {
							buffer = buffer + reader.readLine(); 
						}
						
						buffer = stripElement("style", buffer);
					}
						
					buffer = stripHTML(buffer, reader);	
					buffer = stripEntities(buffer);
					count = parseLine(buffer, count);
				}
				
				status = true;
				
			} catch (UnknownHostException e) {
				 
				log.debug("Error retrieving web page " + this.url.toString());
				removeWebPage(this.url);
				status = false;

			} catch (IOException ioe) {
				log.debug("Error retrieving web page " + this.url.toString());
				removeWebPage(this.url);
				status = false;
			
			} finally {
				try {
					reader.close();
					out.close();
					socket.close();
					
				} catch (Exception e) {
					log.debug("Error closing reader, writer, socket when " +
							"retrieving web page.");
				}
			}
			return status;
		}
	}
}
