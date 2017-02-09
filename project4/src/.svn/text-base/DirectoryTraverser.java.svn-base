import java.io.File;
import java.util.ArrayList;


public class DirectoryTraverser {

	private ArrayList<File> dl;
	private String initialDirectory;
	
	/**
	 * Constructs the DirectoryTraverser class and initializes
	 * a new private ArrayList<String> to store the files found.
	 * Sets the initial directory in initialDirectory.
	 * @param dir initial directory to be traversed
	 */
	public DirectoryTraverser(String dir) {
		this.dl = new ArrayList<File>();
		this.initialDirectory = dir;
	}
	
	/**
	 * Returns the size of the ArrayList stored inside of dl.
	 * @return size of ArrayList dl
	 */
	public int getSize() {
		return this.dl.size();
	}
	
	/**
	 * Returns the file at index i inside of the ArrayList.
	 * @param i index of file to be returned
	 * @return the file at index i
	 */
	public File getFile(int i) {
		return this.dl.get(i);
	}
	/**
	 * Calls buildList if path is a valid directory
	 * returns true if it successfully builds the list,
	 * otherwise returns false.
	 * @return true if initial directory is valid
	 */
	public boolean buildList() {
		File file = new File(initialDirectory);
		
		if(!file.isDirectory()) {
			System.out.println("Path is not valid. Could not build " +
					"list program now exitting.");
			System.exit(0);
			return false;
			
		} else {
			buildList(file);
		}
		
		return true;
	}

	/**
 	 * Helper method to buildList.
	 * @param path file to be added or directory to be traversed
	 * @return true if successfully added all files to dl
	 */
	private boolean buildList(File path) {

		File[] fileList = path.listFiles();
		
		for(File file: fileList) {
		
			if(file.isDirectory()) { 
				buildList(file);
			} 
			else {	
				int dot = file.getName().lastIndexOf(".");
				int last = file.getName().length();
				if(file.isFile() && dot > 0 && 
						file.getName().substring(dot, last).toLowerCase().equals(".txt")) { 
					dl.add(file);	
				}
			}
		}
		
		if(dl.isEmpty()) {
			System.out.println("No files were found in the directory. System now exiting.");
			System.exit(0);
			return false;
		}
		
		return true;
	}

}