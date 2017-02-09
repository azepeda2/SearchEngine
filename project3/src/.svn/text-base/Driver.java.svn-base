import java.io.File;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;


public class Driver {

	private static Logger log = Logger.getLogger(Driver.class);
	
	public static void main(String[] args) {
		
		if(new File("log4j.properties").canRead() == false){
			BasicConfigurator.configure();
		} else {
			PropertyConfigurator.configure("log4j.properties");
		}
		
		ArgumentParser arguments = new ArgumentParser(args);

		if(!arguments.hasValue("-d") || !arguments.hasValue("-q") || 
				!arguments.hasValue("-t")) {
			System.out.println("Usage: java -cp project1.jar Driver -d " +
					"<directory> -q <query> -t <threads> optionally you can " +
					"include -i to write inverted index to invertedindex.txt" +
					" Program now exiting,");
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
		System.out.println("Finished creating inverted index.");
		
		if(arguments.hasFlag("-i")) {
			
			if(!index.writeResults()) {
				System.out.println("couldn't write results");
				System.exit(0);
			}
			
			System.out.println("Refer to invertedindex.txt");
		}
		
		index.callSearchIndex(parser.parseQueryFile(arguments.getValue("-q")));
		
		if(index.writeSearchResults())
			System.out.println("Finished Search. Refer to searchresults.txt.");
		
	}	
}