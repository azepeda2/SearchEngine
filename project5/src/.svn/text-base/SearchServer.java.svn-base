import org.apache.log4j.Logger;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;


public class SearchServer {
	protected static Logger log = Logger.getLogger(SearchServer.class);
	private static SearchInvertedIndex index;
	private int port;
	
	public SearchServer(SearchInvertedIndex index, int port) {
		SearchServer.index = index;
		this.port = port;
	}
	
	public static SearchInvertedIndex getSearchInstance() {
		return index;
	}

	public void startServer() {
		Server server = new Server(port);

		ServletHandler handler = new ServletHandler();
		server.setHandler(handler);
		
		handler.addServletWithMapping(HistoryServlet.class, "/history");
		handler.addServletWithMapping(AccountMaintenanceServlet.class,  "/account");
		handler.addServletWithMapping(SearchServlet.class, "/search");
		handler.addServletWithMapping(LoginUserServlet.class, "/login");
		handler.addServletWithMapping(LoginRegisterServlet.class, "/register");
		handler.addServletWithMapping(RedirectServlet.class, "/*");

		log.info("Starting server on port " + port + "...");

		try {
			server.start();
			server.join();

			log.info("Exiting...");
		}
		catch (Exception ex) {
			log.fatal("Interrupted while running server.", ex);
			System.exit(-1);
		}
	}
	
}