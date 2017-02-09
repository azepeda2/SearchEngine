
public class Driver {

	public static void main(String[] args) {
		
		ArgumentParser arguments = new ArgumentParser(args);
		if(!arguments.hasValue("-d")) {
			System.out.println("Usage: java -cp project1.jar Driver -d " +
					"<directory> Program now exiting,");
			System.exit(0);
		}

		InvertedIndex index = new InvertedIndex();
		DirectoryTraverser directory = new DirectoryTraverser(arguments.getValue("-d"));
		System.out.println("Traversing directory");
		directory.buildList();
		FileParser parser = new FileParser(index, directory);
		System.out.println("Creating inverted index");
		parser.parseFiles();
		
		if(!index.writeResults()) {
			System.out.println("couldn't write results");
			System.exit(0);
		}
		
		System.out.println("Finished creating inverted index. Refer to" +
				" invertedindex.txt");	
	}	
}