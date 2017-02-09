import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@SuppressWarnings("serial")
public class AccountMaintenanceServlet extends BaseServlet {
	
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) {
		try {
			prepareResponse("Account Maintenance", response);

			PrintWriter out = response.getWriter();
			String username = getUsername(request);
			String error = request.getParameter("error");
			String changepass = request.getParameter("changepass");
			String clearhistory = request.getParameter("clearhistory");
			
			if(username == null) {
				response.sendRedirect("/login");
			}
			
			printUserName(out, request);
			out.println("<h1>Account Maintenance</h1>");

			if(error != null) {
				String errorMessage = getStatusMessage(error);
				out.println("<p style=\"color: red;\">" + errorMessage + "</p>");
			}
			
			if(changepass != null) {
				out.println("<p>Password was successfully changed!</p>");
			}
			
			if(clearhistory != null) {
				out.println("<p>Search history was successfully cleared!</p>");
			}

			printForm(out);
			out.println("(<a href=\"/\">Return to the search engine.</a>)");
			finishResponse(response);
		}
		catch(IOException ex) {
			log.debug("Unable to prepare response properly.", ex);
		}
	}


	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) {
		prepareResponse("Account Maintenance", response);

		String username = getUsername(request);
		String currentpass = request.getParameter("pass");
		String newpass = request.getParameter("newpass");
		String change = request.getParameter("change");
		String clear = request.getParameter("clear");
		Status status = null;
		
		if(username != null && currentpass != null && newpass != null && 
				change != null) {
			status = db.changePassword(username, currentpass, newpass);
			
			try {
				if(status == Status.OK) {
					response.sendRedirect(response.encodeRedirectURL("/account?changepass=true"));
				}
				else {
					String url = "/account?error=" + status.name();
					url = response.encodeRedirectURL(url);
					response.sendRedirect(url);
				}
			}
			catch(IOException ex) {
				log.warn("Unable to redirect user. " + status, ex);
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			}
		
		} else if(username != null || clear != null) {
			status = db.clearHistory(username, currentpass);
			
			try {
				if(status == Status.OK) {
					response.sendRedirect(response.encodeRedirectURL("/account?clearhistory=true"));
				}
				else {
					String url = "/account?error=" + status.name();
					url = response.encodeRedirectURL(url);
					response.sendRedirect(url);
				}
			}
			catch(IOException ex) {
				log.warn("Unable to redirect user. " + status, ex);
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			}
		}
		
	}

	/**
	 * Prints password change form.
	 *
	 * @param out - writer for the servlet
	 */
	private void printForm(PrintWriter out) {
		assert out != null;

		out.println("<form action=\"/account?change=true\" method=\"post\">");
		out.println("<table border=\"0\">");
		out.println("\t<tr>");
		out.println("\t\t<td>Current Password:</td>");
		out.println("\t\t<td><input type=\"password\" name=\"pass\" size=\"30\"></td>");
		out.println("\t</tr>");
		out.println("\t<tr>");
		out.println("\t\t<td>New Password:</td>");
		out.println("\t\t<td><input type=\"password\" name=\"newpass\" size=\"30\"></td>");
		out.println("</tr>");
		out.println("</table>");
		out.println("<p><input type=\"submit\" value=\"change password\"></p>");
		out.println("</form>");
		
		out.println("<form action=\"/account?clear=true\" method=\"post\">");
		out.println("<table border=\"0\">");
		out.println("\t<tr>");
		out.println("\t\t<td>Current Password:</td>");
		out.println("\t\t<td><input type=\"password\" name=\"pass\" size=\"30\"></td>");
		out.println("\t</tr>");
		out.println("</table>");
		out.println("<p><input type=\"submit\" value=\"clear history\"></p>");
		out.println("</form>");
	}
}