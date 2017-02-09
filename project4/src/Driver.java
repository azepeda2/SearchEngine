import java.io.File;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.PropertyConfigurator;


public class Driver {

	
	public static void main(String[] args) {
		
		if(new File("log4j.properties").canRead() == false){
			BasicConfigurator.configure();
		} else {
			PropertyConfigurator.configure("log4j.properties");
		}
		
		ArgumentParser arguments = new ArgumentParser(args);

		if(arguments.hasValue("-u")) {
		
			if(!arguments.hasValue("-u") || !arguments.hasValue("-q") || 
					!arguments.hasValue("-t")) {
				System.out.println("Usage: java -cp project1.jar Driver -u " +
						"<seed> -q <query> -t <threads> optionally you can " +
						"include -i to write inverted index to " +
						"invertedindex.txt. Program now exiting.");
				System.exit(0);
			}
			
			int threads = Integer.parseInt(arguments.getValue("-t"));
			WorkQueue queue = new WorkQueue(threads);
			SearchInvertedIndex index = new SearchInvertedIndex(queue);
			WebCrawler crawler = new WebCrawler(index, queue, arguments.getValue("-u"));
			System.out.println("Crawling web and creating inverted index");
			crawler.beginCrawling();
			System.out.println("Finished creating inverted index.");

			if(arguments.hasFlag("-i")) {
				
				if(!index.writeResults()) {
					System.out.println("couldn't write index to file");
					System.exit(0);
				}
				
				System.out.println("Refer to invertedindex.txt");
			}
			
			index.callSearchIndex(FileParser.parseQueryFile(arguments.getValue("-q")));
			queue.shutdown();
			
			if(index.writeSearchResults())
				System.out.println("Finished Search. Refer to searchresults.txt.");

			
		} else {
			if(!arguments.hasValue("-d") || !arguments.hasValue("-q") || 
					!arguments.hasValue("-t")) {
				System.out.println("Usage: Remember to enter all of the " +
						"required commands. If inserting a seed URL: \n");
				System.out.println("java -cp project1.jar Driver -d " +
						"<directory> -q <query> -t <threads> optionally you " +
						"can include -i to write inverted index to " +
						"invertedindex.txt. Program now exiting. \n");
				System.out.println("or if you are inserting a seed directory:" +
						"\n\njava -cp project1.jar Driver -d <directory> -q " +
						"<query> -t <threads> optionally you can include -i " +
						"to write inverted index to invertedindex.txt. " +
						"Program now exiting.");
				System.exit(0);
			}
			
			int threads = Integer.parseInt(arguments.getValue("-t"));
			WorkQueue queue = new WorkQueue(threads);
			SearchInvertedIndex index = new SearchInvertedIndex(queue);
			DirectoryTraverser directory = new DirectoryTraverser(arguments.getValue("-d"));
			System.out.println("Traversing directory");
			directory.buildList();
			FileParser parser = new FileParser(index, directory, queue);
			System.out.println("Creating inverted index");
			parser.parseFiles();

			if(arguments.hasFlag("-i")) {
				
				if(!index.writeResults()) {
					System.out.println("couldn't write index to file");
					System.exit(0);
				}
				
				System.out.println("Refer to invertedindex.txt");
			}
			
			index.callSearchIndex(FileParser.parseQueryFile(arguments.getValue("-q")));
			queue.shutdown();
			
			if(index.writeSearchResults())
				System.out.println("Finished Search. Refer to searchresults.txt.");

		}
		
		
	}	
}