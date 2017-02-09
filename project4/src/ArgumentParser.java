import java.util.HashMap;

public class ArgumentParser {

	private HashMap<String, String> argMap;
	
	/**
	 * Constructor of the ArgumentParser. 
	 * @param args
	 */
	public ArgumentParser(String[] args) {
		argMap = new HashMap<String, String>();
		parseArgs(args);
	}
	/**
	 * Iterates through the arguments input at the command line and 
	 * stores the keys and values into a map. If the key has no value
	 * it is stored with a null value.
	 * @param args command line arguments
	 */
	private void parseArgs(String[] args) {
		
		for(int i = 0; i < args.length; i++) {
			if(i + 1 < args.length && args[i].startsWith("-") &&
					!args[i + 1].startsWith("-")) {
				argMap.put(args[i], args[i + 1]);
			} else if(args[i].startsWith("-")) {
				argMap.put(args[i], null);
			}
		}
	}
	
	/**
	 * Returns true if the argument starts with a '-' indicating it
	 * is a flag, returns false otherwise.
	 * @param arg string to be tested
	 * @return true if arg is a flag
	 */
	public static boolean isFlag(String arg) {
		
		if(arg.startsWith("-")) {
			return true;
		}
		return false;
	}
	
	/**
	 * returns true if value does not start with a '-' returns
	 * false otherwise.
	 * @param arg string to be tested
	 * @return true if arg is a value
	 */
	public static boolean isValue(String arg) {
		
		if(!arg.startsWith("-")) {
			return true;
		}
		return false;
	}
	
	/**
	 * Returns true if the the flag exists in the map
	 * returns false otherwise.
	 * @param flag check for this flag
	 * @return true if flag is in map
	 */
	public boolean hasFlag(String flag) {
		
		if(argMap.containsKey(flag)) {
			return true;
		}
		return false;
	}
	
	/**
	 * Returns true if the key contains a value,
	 * returns false otherwise.
	 * @param flag check if it has a value in the map
	 * @return true if flag has a value
	 */
	public boolean hasValue(String flag) {
		
		if(hasFlag(flag) && argMap.get(flag) != null) {
			return true;
		}
		return false;
	}
	
	/**
	 * Returns the value that matches with that key if it exists
	 * returns false otherwise.
	 * @param flag value to be looked for
	 * @return true if value is in map
	 */
	public String getValue(String flag) {
		if(hasFlag(flag)) {
			String value = argMap.get(flag);
			return value;
		}
		return null;
	}
	
	/**
	 * Returns the number of keys that are stored within the map.
	 * @return number of flags in map
	 */
	public int numFlags() {
		return argMap.size();
		
	}
	
	/**
	 * Returns the number of valid arguments that have been stored 
	 * in the map. Doesn't count the one with null values.
	 * @return number of valid arguments in map
	 */
	public int numArguments() {
		int args = 0;
		
		for(String key: argMap.keySet()) {
			if(argMap.get(key) != null) {
				args++;
			}
		}
		return args;
	}
			
}
