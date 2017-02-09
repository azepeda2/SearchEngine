import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@SuppressWarnings("serial")
public class RedirectServlet extends BaseServlet {
	
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) {	
		try {
			response.sendRedirect("/search");
		}
		catch (Exception ex) {
			log.debug("Unable to redirect to /search page.", ex);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}

	/**
	 * Operation not supported.
	 *
	 * @see #doGet(HttpServletRequest, HttpServletResponse)
	 */
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) {
		doGet(request, response);
	}
}
