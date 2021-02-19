package it.polimi.tiw.lf.gallery.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import it.polimi.tiw.lf.gallery.beans.Comment;
import it.polimi.tiw.lf.gallery.dao.CommentDAO;
import it.polimi.tiw.lf.gallery.dao.UserDAO;
import it.polimi.tiw.lf.gallery.utils.ConnectionHandler;

@WebServlet("/GetCommentsList")
public class GetCommentsList extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection;
	
	public void init() throws ServletException {
		connection = ConnectionHandler.getConnection(getServletContext());
	}
	
	public void destroy() {		
		try {
			ConnectionHandler.closeConnection(connection);
		} catch(SQLException e) {
			e.printStackTrace();
		}
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		/* Extracts parameter from GET */		
		Integer imageId = null;
		try {
			imageId = Integer.parseInt(request.getParameter("image"));
		} catch (NumberFormatException | NullPointerException e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Incorrect or missing param values");
			return;
		}		

		/* Fetches the selected image comments */
		CommentDAO cDAO = new CommentDAO(connection);
		List<Comment> comments = null;
		try {
			comments = cDAO.findCommentsByImage(imageId);
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Not possible to recover comments");
			return;
		}
		
		/* Maps each comment to its user */
		UserDAO uDAO = new UserDAO(connection);
		Map<Integer, String> usernames = new HashMap<>();
		comments.stream().forEach(comment -> {
			int user = comment.getUser();
			if(usernames.get(user) == null)
				usernames.put(user, uDAO.findUsernameFromId(user));
			comment.setUsername(usernames.get(user));
		});
		
		/* Redirect to home page and add comments as parameter */
		String json = new Gson().toJson(comments);		
		
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write(json);
	}
}
