import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;


@SuppressWarnings("serial")
public class HistoryServlet extends BaseServlet {

	/** Log4j logger for debug messages. */
	private static Logger log = Logger.getLogger(HistoryServlet.class);



	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) {
		PrintWriter out = null;
	
		prepareResponse("History", response);

		String user = getUsername(request);

		try {
			out = response.getWriter();
			


			if(user == null)
				response.sendRedirect("/login");

			printUserName(out, request);
			printForm(out);

			db.printHistory(user, out, request, response);
			out.println("(<a href=\"/\">Return to the search engine.</a>)");

		}
		catch (Exception ex) {
			log.debug("falied in /history");
			log.debug("Unable to prepare response body.", ex);
		}

		finishResponse(response);
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) {
	}

	/**
	 * Prints search form.
	 *
	 * @param out - writer for the servlet
	 */
	private void printForm(PrintWriter out) {
		assert out != null;

		out.println("<center><h1>Web Search</h1>");
		out.println("<form action=\"/search\" method=\"post\">");
		out.println("<table border=\"0\">");
		out.println("\t<tr>");
		out.println("\t\t<td><input type=\"text\" name=\"query\" size=\"64\"></td>");
		out.println("\t</tr>");
		out.println("</table>");
		out.println("<p><input type=\"submit\" value=\"Search\"></p>");
		out.println("</form></center>");
	}
}
