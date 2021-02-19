package it.polimi.tiw.lf.gallery.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;

import it.polimi.tiw.lf.gallery.beans.Image;
import it.polimi.tiw.lf.gallery.beans.User;
import it.polimi.tiw.lf.gallery.dao.CommentDAO;
import it.polimi.tiw.lf.gallery.dao.ImageDAO;
import it.polimi.tiw.lf.gallery.exceptions.BadCommentFormat;
import it.polimi.tiw.lf.gallery.utils.ConnectionHandler;

@WebServlet("/AddComment")
@MultipartConfig
public class AddComment extends HttpServlet {
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
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		/* Gets user info */
		User user = (User) request.getSession().getAttribute("user");		
		
		/* Extracts parameters from POST */
		Integer imageId = null;
		String text = null;
		try {
			imageId = Integer.parseInt(request.getParameter("image"));
			text = StringEscapeUtils.escapeJava(request.getParameter("text"));
			/* Text parameter cannot be empty */
			if(text == null || text.equals("")) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.getWriter().println("Your comment cannot be empty");
				return;
			}
		} catch (NumberFormatException | NullPointerException e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Incorrect or missing param values");
			return;
		}		
		
		/* Looks for the image to comment it */
		CommentDAO cDAO = new CommentDAO(connection);
		ImageDAO iDAO = new ImageDAO(connection);
		Image image = null;
		try {
			image = iDAO.findImageById(imageId);
			/* ImageId is not a correct image */
			if(image == null) {
				response.setStatus(HttpServletResponse.SC_NOT_FOUND);
				response.getWriter().println("Image not found");
				return;
			}
			/* Adds the comment */
			cDAO.addComment(user.getId(), text, imageId);
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Not possible to create comment");
			return;
		} catch (BadCommentFormat e ) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Incorrect param values");
			return;
		}
	}
}
