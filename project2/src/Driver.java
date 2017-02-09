
public class Driver {

	public static void main(String[] args) {
		
		ArgumentParser arguments = new ArgumentParser(args);

		if(!arguments.hasValue("-d") && !arguments.hasValue("-q")) {
			System.out.println("Usage: java -cp project1.jar Driver -d " +
					"<directory> -q <query> optionally you can include -i " +
					"to write inverted index to invertedindex.txt " +
					"Program now exiting,");
			System.exit(0);
		}

		SearchInvertedIndex index = new SearchInvertedIndex();
		DirectoryTraverser directory = new DirectoryTraverser(arguments.getValue("-d"));
		System.out.println("Traversing directory");
		directory.buildList();
		FileParser parser = new FileParser(index, directory);
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