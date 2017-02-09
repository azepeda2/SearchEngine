import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@SuppressWarnings("serial")
public class SearchServlet extends BaseServlet {
	
	private SearchInvertedIndex search = SearchServer.getSearchInstance();

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) {
		prepareResponse("Search", response);

		String user = getUsername(request);

		try {
			PrintWriter out = response.getWriter();

			if(user != null)
				printUserName(out, request);

			printForm(out);

			if(user == null) {
				out.println("<p>If you want to keep a history of your ");
				out.println("searches, you should login.<br>");
				out.println("(<a href=\"/login\">Login</a>)<br>");
				out.println("(<a href=\"/register\">New user? Register here.</a>)</p>");
			}
		}
		catch (IOException ex) {
			System.out.println("falied in /search");
			log.debug("Unable to prepare response body.", ex);
		}

		finishResponse(response);
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) {
		prepareResponse("Search", response);
		String query = request.getParameter("query");
		String user = getUsername(request);

		if(query == null) {
			try {
				response.sendRedirect("/search");
			} catch (IOException e) {
				log.debug("Unable to redirect to /search page.", e);
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			}
		} else {

			try {
				PrintWriter out = response.getWriter();
				ArrayList<String> results = new ArrayList<String>();
				
				if(user != null)
					printUserName(out, request);

				printForm(out);
				
				results = search.serverCallSearchIndex(query);
				printResults(out, results);
				db.addSearchQuery(user, query);
				
				if(user == null) {
					out.println("<p>If you want to keep a history of your ");
					out.println("searches, you should login.<br>");
					out.println("(<a href=\"/login\">Login</a>)<br>");
					out.println("(<a href=\"/register\">New user? ");
					out.println("Register here.</a>)</p>");
				}
			}
			catch (IOException ex) {
				System.out.println("falied in /search");
				log.debug("Unable to prepare response body.", ex);
			}
		}
		finishResponse(response);
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

	private boolean printResults(PrintWriter out, ArrayList<String> results) {
		assert out != null;
		
		if(results.isEmpty()) {
			out.println("<center><p>No Results were found with your query</p></center>");
			return false;
		
		} else {
			for(String link: results) {
				out.println("<p><a href=\"" + link + "\">" + link + "</a></p>");
			}
			return true;
		}
	}
}
