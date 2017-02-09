import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

/**
 * Part of the {@link SearchServer} example. Handles all database-related
 * actions.
 *
 * @author Sophie Engle modified by Alejandro Zepeda
 */
public class LoginDatabaseHandler {

	/** A {@link org.apache.log4j.Logger log4j} logger for debugging. */
	private static Logger log = Logger.getLogger(LoginDatabaseHandler.class);

	/** Makes sure only one database handler is instantiated. */
	private static LoginDatabaseHandler singleton = new LoginDatabaseHandler();

	/** Used to create necessary tables for this example. */
	private static final String CREATE_SQL =
			"CREATE TABLE Login_Users (" +
					"userid INTEGER AUTO_INCREMENT PRIMARY KEY, " +
					"username VARCHAR(32) NOT NULL UNIQUE, " +
					"password CHAR(64) NOT NULL, " +
					"usersalt CHAR(32) NOT NULL);";

	private static final String CREATE_HISTORY_SQL =
			"CREATE TABLE Search_History (" +
					"entry INTEGER AUTO_INCREMENT PRIMARY KEY, " +
					"username VARCHAR(32) NOT NULL, " +
					"query VARCHAR(100) NOT NULL);";

	private static final String ADD_SEARCH_SQL =
			"INSERT INTO Search_History (username, query) " +
					"VALUES (?, ?);";
	
	/** Used to insert a new user into the database. */
	private static final String REGISTER_SQL =
			"INSERT INTO Login_Users (username, password, usersalt) " +
					"VALUES (?, ?, ?);";

	/** Used to determine if a username already exists. */
	private static final String USER_SQL =
			"SELECT username FROM Login_Users WHERE username = ?";

	/** Used to retrieve the salt associated with a specific user. */
	private static final String SALT_SQL =
			"SELECT usersalt FROM Login_Users WHERE username = ?";

	/** Used to authenticate a user. */
	private static final String AUTH_SQL =
			"SELECT username FROM Login_Users " +
					"WHERE username = ? AND password = ?";

	private static final String CHANGE_PASS_SQL = 
			"UPDATE Login_Users SET password=? WHERE username = ?";

	private static final String CHANGE_SALT_SQL = 
			"UPDATE Login_Users SET usersalt=? WHERE username = ?";

	/** Used to remove a user from the database. */
	private static final String DELETE_SQL =
			"DELETE FROM Login_Users WHERE username = ?";
	
	private static final String DELETE_HISTORY_SQL =
			"DELETE FROM Search_History WHERE username = ?";
	
	protected static final String SELECT_QUERY_SQL =
			"SELECT query FROM Search_History " +
					"WHERE username = ?";

	/** Format string to simplify output of table cell data. */
	protected static final String cellFormat = "\t<td>%s</td>%n";

	/** Used to configure connection to database. */
	private DatabaseConfigurator db;

	/** Used to generate password hash salt for user. */
	private Random random;

	/**
	 * Initializes a database handler for the Login example.
	 */
	private LoginDatabaseHandler() {
		db = new DatabaseConfigurator();
		random = new Random(System.currentTimeMillis());

		Status status = null;

		// exit if unable to establish connection with database
		status = db.testConfig();
		if (status != Status.OK) {
			log.fatal(status);
			System.exit(status.ordinal());
		}

		// exit if unable to setup tables necessary for this example
		status = setupTables();
		if (status != Status.OK) {
			log.fatal(status);
			System.exit(status.ordinal());
		}
	}

	/**
	 * Gets the single instance of the database handler.
	 *
	 * @return instance of the database handler
	 */
	public static LoginDatabaseHandler getInstance() {
		return singleton;
	}

	/**
	 * Registers a new user, placing the username, password hash, and
	 * salt into the database if the username does not already exist.
	 *
	 * @param newuser - username of new user
	 * @param newpass - password of new user
	 * @return {@link Status.OK} if registration successful
	 */
	public Status registerUser(String newuser, String newpass) {
		Connection connection = null;
		PreparedStatement statement = null;

		Status status = Status.ERROR;

		log.debug("Registering " + newuser + ".");

		// make sure we have non-null and non-emtpy values for login
		if (newuser == null || newpass == null ||
				newuser.trim().isEmpty() || newpass.trim().isEmpty()) {
			status = Status.INVALID_LOGIN;
			log.debug(status);
			return status;
		}

		try {
			connection = db.getConnection();

			// make sure username doesn't already exist
			if (userExists(connection, newuser)) {
				status = Status.DUPLICATE_USER;
			}
			else {
				// create random salt for user
				byte[] saltBytes = new byte[16];
				random.nextBytes(saltBytes);

				String usersalt = encodeHex(saltBytes, 32);
				String passhash = getHash(newpass, usersalt);

				try {
					// insert username, password hash, and salt
					statement = connection.prepareStatement(REGISTER_SQL);
					statement.setString(1, newuser);
					statement.setString(2, passhash);
					statement.setString(3, usersalt);
					statement.executeUpdate();

					status = Status.OK;
				}
				catch (SQLException ex) {
					status = Status.SQL_EXCEPTION;

					log.warn("Unable to register user " + newuser + ".");
					log.debug(status, ex);
				}
			}
		}
		catch (Exception ex) {
			status = Status.CONNECTION_FAILED;
			log.debug(status, ex);
		}
		finally {
			try {
				statement.close();
				connection.close();
			}
			catch (Exception ignored) {
				// do nothing
			}
		}

		return status;
	}

	/**
	 * Checks if the provided username and password match what is stored
	 * in the database. Must retrieve the salt and hash the password to
	 * do the comparison.
	 *
	 * @param username - username to authenticate
	 * @param password - password to authenticate
	 * @return {@link Status.OK} if authentication successful
	 */
	public Status authenticateUser(String username, String password) {
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet results = null;

		Status status = Status.ERROR;

		log.debug("Authenticating user " + username + ".");

		try {
			connection = db.getConnection();

			try {
				// get salt for user
				String usersalt = getSalt(connection, username);
				String passhash = getHash(password, usersalt);

				// test if username and password match
				statement = connection.prepareStatement(AUTH_SQL);
				statement.setString(1, username);
				statement.setString(2, passhash);

				results = statement.executeQuery();

				if (results.next()) {
					status = Status.OK;
				}
				else {
					status = Status.INVALID_LOGIN;
				}

				statement.close();
			}
			catch (Exception ex) {
				status = Status.SQL_EXCEPTION;
				log.debug(status, ex);
			}
		}
		catch (Exception ex) {
			status = Status.CONNECTION_FAILED;
			log.debug(status, ex);
		}
		finally {
			try {
				// make sure database connection is closed
				connection.close();
			}
			catch (Exception ignored) {
				// do nothing
			}
		}

		return status;
	}

	/**
	 * Removes a user from the database if the username and password are
	 * provided correctly.
	 *
	 * @param username - username to remove
	 * @param password - password of user
	 * @return {@link Status.OK} if removal successful
	 */
	public Status removeUser(String username, String password) {
		Status status = authenticateUser(username, password);

		if (status == Status.OK) {
			log.debug("Removing user " + username + ".");
			Connection connection = null;
			PreparedStatement statement = null;

			try {
				connection = db.getConnection();

				try {
					statement = connection.prepareStatement(DELETE_SQL);
					statement.setString(1, username);

					int count = statement.executeUpdate();
					status = (count == 1) ? Status.OK : Status.INVALID_USER;

					statement.close();
				}
				catch (Exception ex) {
					status = Status.SQL_EXCEPTION;
					log.debug(status, ex);
				}
			}
			catch (Exception ex) {
				status = Status.CONNECTION_FAILED;
				log.debug(status, ex);
			}
			finally {
				try {
					// make sure database connection is closed
					connection.close();
				}
				catch (Exception ignored) {
					// do nothing
				}
			}
		}

		return status;
	}
	
	public Status clearHistory(String username, String password) {
		Status status = authenticateUser(username, password);

		log.debug("Removing user " + username + "'s history.");

		if (status == Status.OK) {
			Connection connection = null;
			PreparedStatement statement = null;

			try {
				connection = db.getConnection();

				try {
					statement = connection.prepareStatement(DELETE_HISTORY_SQL);
					statement.setString(1, username);
					statement.executeUpdate();
					statement.close();
				}
				catch (Exception ex) {
					status = Status.SQL_EXCEPTION;
					log.debug(status, ex);
				}
			}
			catch (Exception ex) {
				status = Status.CONNECTION_FAILED;
				log.debug(status, ex);
			}
			finally {
				try {
					// make sure database connection is closed
					connection.close();
				}
				catch (Exception ignored) {
					// do nothing
				}
			}
		}

		return status;
	}
	
	public Status addSearchQuery(String username, String query) {
		Status status = null;
		boolean exists = userExists(username);
		
		if (exists == true) {
			log.debug("Adding search query " + query + " to search history.");
			Connection connection = null;
			PreparedStatement statement = null;

			try {
				connection = db.getConnection();

				try {
					statement = connection.prepareStatement(ADD_SEARCH_SQL);
					statement.setString(1, username);
					statement.setString(2, query);

					int count = statement.executeUpdate();
					status = (count == 1) ? Status.OK : Status.INVALID_USER;

					statement.close();
				}
				catch (Exception ex) {
					status = Status.SQL_EXCEPTION;
					log.debug(status, ex);
				}
			}
			catch (Exception ex) {
				status = Status.CONNECTION_FAILED;
				log.debug(status, ex);
			}
			finally {
				try {
					// make sure database connection is closed
					connection.close();
				}
				catch (Exception ignored) {
					// do nothing
				}
			}
		}

		return status;
	}
	
	public Status printHistory(String username, PrintWriter out,
			HttpServletRequest request, HttpServletResponse response) {
		Status status = null;
		ResultSet results = null;
	
		boolean exists = userExists(username);
		log.debug("Getting " + username + "'s search history.");
		
		if (exists == true) {
			Connection connection = null;
			PreparedStatement statement = null;
			
			try {
				connection = db.getConnection();

				try {
					
					statement = connection.prepareStatement(SELECT_QUERY_SQL);
					statement.setString(1, username);

					results = statement.executeQuery();
					
					if (results.next()) {
						status = Status.OK;
						results.previous();
						out.printf("<center><table cellspacing=\"0\" cellpadding=\"2\" border=\"1\">\n");
						printColumn(request, response);
						printResults(out, results);
						out.printf("</table>%n%n</center>");
					}
					else {
						out.printf("<center><p>No results stored in database.");
						out.printf(" Start Searching</p></center>");
						status = Status.SQL_EXCEPTION;
					}

					statement.close();
				}
				catch (Exception ex) {
					status = Status.SQL_EXCEPTION;
					log.debug(status, ex);
				}
			}
			catch (Exception ex) {
				status = Status.CONNECTION_FAILED;
				log.debug(status, ex);
			}
			finally {
				try {
					// make sure database connection is closed
					connection.close();
				}
				catch (Exception ignored) {
					// do nothing
				}
			}
		}
		return status;	
	}
	

	/**
	 * Prints table column names with a grey background.
	 *
	 * @param request HTTP request for servlet
	 * @param response HTTP response for servlet
	 * @throws IOException if unable to create writer
	 */
	protected void printColumn(HttpServletRequest request, HttpServletResponse response) throws IOException {
		PrintWriter writer = response.getWriter();
		writer.printf("<tr style=\"background-color: #EEEEEE;\">\n");
		writer.printf(cellFormat, "Search History");
		writer.printf("</tr>%n");
	}

	/**
	 * Prints the result rows using a simple format.
	 *
	 * @param writer writer for the HTTP response
	 * @param results set of results from SQL query
	 * @throws SQLException if any issues with set of results
	 */
	protected void printResults(PrintWriter writer, ResultSet results) throws SQLException {
		while(results != null && results.next()) {
			writer.printf("<tr>%n");
			writer.printf(cellFormat, results.getString("query"));
			writer.printf("</tr>%n");
		}
	}

	public Status changePassword(String username, String oldpassword, String newpassword) {
		Status status = authenticateUser(username, oldpassword);

		log.debug("Changing user " + username + "'s password.");

		if (status == Status.OK) {
			Connection connection = null;
			PreparedStatement statement = null;

			String oldusersalt, oldpasshash = null;
			try {
				connection = db.getConnection();


				try {
					oldusersalt = getSalt(connection, username);
					oldpasshash = getHash(oldpassword, oldusersalt);
				} catch (SQLException e) {
					status = Status.SQL_EXCEPTION;
					log.debug(status, e);
				}

				if (username == null || oldpasshash == null || newpassword == null
						|| username.trim().isEmpty() || oldpasshash.trim().isEmpty()
						|| newpassword.trim().isEmpty()) {
					status = Status.MISSING_VALUES;
					log.debug(status);
					return status;
				}

				try {
					byte[] saltBytes = new byte[16];
					random.nextBytes(saltBytes);

					String usersalt = encodeHex(saltBytes, 32);
					String passhash = getHash(newpassword, usersalt);

					statement = connection.prepareStatement(CHANGE_PASS_SQL);
					statement.setString(1, passhash);
					statement.setString(2, username);

					int count = statement.executeUpdate();
					status = (count == 1) ? Status.OK : Status.CHANGE_PASS_FAILED;

					statement = connection.prepareStatement(CHANGE_SALT_SQL);
					statement.setString(1, usersalt);
					statement.setString(2, username);


					count = statement.executeUpdate();
					status = (count == 1) ? Status.OK : Status.CHANGE_PASS_FAILED;

					statement.close();
				}
				catch (Exception ex) {
					status = Status.SQL_EXCEPTION;
					log.debug(status, ex);
				}
			}
			catch (Exception ex) {
				status = Status.CONNECTION_FAILED;
				log.debug(status, ex);
			}
			finally {
				try {
					connection.close();
				}
				catch (Exception ignored) {}
			}
		}

		return status;
	}

	/**
	 * Tests if a user already exists in the database.
	 *
	 * @see #userExists(Connection, String)
	 * @param user - username to check
	 * @return <code>true</code> if user exists in database
	 */
	public boolean userExists(String user) {
		Connection connection = null;
		boolean exists = false;

		try {
			connection = db.getConnection();
			exists = userExists(connection, user);
		}
		catch (Exception ex) {
			log.debug(ex.getMessage(), ex);
		}
		finally {
			try {
				// make sure database connection is closed
				connection.close();
			}
			catch (Exception ignored) {
				// do nothing
			}
		}

		return exists;
	}

	/**
	 * Tests if a user already exists in the database. Requires an active
	 * database connection.
	 *
	 * @param connection - active database connection
	 * @param user - username to check
	 * @return <code>true</code> if user exists in database
	 * @throws SQLException if problem with database connection
	 */
	private boolean userExists(Connection connection, String user) throws SQLException {
		assert connection != null;
		assert user != null;

		PreparedStatement statement = null;
		ResultSet results = null;
		boolean exists = false;

		// user is not safe to use directly
		statement = connection.prepareStatement(USER_SQL);
		statement.setString(1, user);

		results = statement.executeQuery();

		// if any results, then user exists
		if (results.next()) {
			exists = true;
		}

		try {
			results.close();
			statement.close();
		}
		catch (Exception ignored) {
			// do nothing
		}

		return exists;
	}

	/**
	 * Checks if necessary table exists in database, and if not tries to
	 * create it.
	 *
	 * @return {@link Status.OK} if table exists or create is successful
	 */
	private Status setupTables() {

		Connection connection = null;
		Statement statement = null;
		ResultSet results = null;
		Status status = Status.ERROR;

		try {
			connection = db.getConnection();
			statement = connection.createStatement();
			results = statement.executeQuery("SHOW TABLES LIKE 'Login_Users';");

			if (!results.next()) {
				// attempt to create table
				statement.executeUpdate(CREATE_SQL);

				// check if table now exists
				results = statement.executeQuery("SHOW TABLES LIKE 'Login_Users';");
				status = (results.next()) ? Status.OK : Status.CREATE_FAILED;
			}
			else {
				// could check if correct columns here
				status = Status.OK;
			}
			
			results = statement.executeQuery("SHOW TABLES LIKE 'Search_History';");

			if (!results.next()) {
				// attempt to create table
				statement.executeUpdate(CREATE_HISTORY_SQL);

				// check if table now exists
				results = statement.executeQuery("SHOW TABLES LIKE 'Search_History';");
				status = (results.next()) ? Status.OK : Status.CREATE_FAILED;
			}
			else {
				// could check if correct columns here
				status = Status.OK;
			}

			results.close();
			statement.close();
		}
		catch (Exception ex) {
			status = Status.CREATE_FAILED;
			log.debug(status, ex);
		}
		finally {
			try {
				// make sure database connection is closed
				connection.close();
			}
			catch (Exception ignored) {
				// do nothing
			}
		}

		return status;
	}

	/**
	 * Calculates the hash of a password and salt using SHA-256.
	 *
	 * @param password - password to hash
	 * @param salt - salt associated with user
	 * @return hashed password
	 */
	private String getHash(String password, String salt) {
		String salted = salt + password;
		String hashed = salted;

		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			md.update(salted.getBytes());
			hashed = encodeHex(md.digest(), 64);
		}
		catch (Exception ex) {
			log.debug("Unable to properly hash password.", ex);
		}

		return hashed;
	}

	/**
	 * Returns the hex encoding of a byte array.
	 *
	 * @param bytes - byte array to encode
	 * @param length - desired length of encoding
	 * @return hex encoded byte array
	 */
	private String encodeHex(byte[] bytes, int length) {
		BigInteger bigint = new BigInteger(1, bytes);
		String hex = String.format("%0" + length + "X", bigint);

		assert hex.length() == length;
		return hex;
	}

	/**
	 * Gets the salt for a specific user.
	 *
	 * @param connection - active database connection
	 * @param user - which user to retrieve salt for
	 * @return salt for the specified user or null if user does not exist
	 * @throws SQLException if any issues with database connection
	 */
	private String getSalt(Connection connection, String user) throws SQLException {
		assert connection != null;
		assert user != null;

		PreparedStatement statement = null;
		ResultSet results = null;
		String salt = null;

		statement = connection.prepareStatement(SALT_SQL);
		statement.setString(1, user);

		results = statement.executeQuery();

		if (results.next()) {
			salt = results.getString("usersalt");
		}

		try {
			results.close();
			statement.close();
		}
		catch (Exception ignored) {
			// do nothing
		}

		return salt;
	}

	//	public static void main(String[] args) throws Exception {
	//		LoginDatabaseHandler logindb = LoginDatabaseHandler.getInstance();
	//
	//		Status status = logindb.registerUser("test01", "test01");
	//		System.out.println("Register test01: " + status);
	//
	//		status = logindb.registerUser("test02", "test02");
	//		System.out.println("Register test02: " + status);
	//
	//		status = logindb.registerUser("test01", "test01");
	//		System.out.println("Register test01: " + status);
	//
	//		status = logindb.authenticateUser("test01", "test01");
	//		System.out.println("Auth test01/test01: " + status);
	//
	//		status = logindb.authenticateUser("test01", "mypass");
	//		System.out.println("Auth test01/mypass: " + status);
	//
	//		status = logindb.removeUser("test01", "test01");
	//		System.out.println("Remove test01: " + status);
	//
	//		status = logindb.removeUser("test02", "test02");
	//		System.out.println("Remove test02: " + status);
	//	}
}
