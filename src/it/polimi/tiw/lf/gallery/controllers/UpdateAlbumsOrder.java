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

import it.polimi.tiw.lf.gallery.beans.User;
import it.polimi.tiw.lf.gallery.dao.UserDAO;
import it.polimi.tiw.lf.gallery.utils.ConnectionHandler;

@WebServlet("/UpdateAlbumsOrder")
@MultipartConfig
public class UpdateAlbumsOrder extends HttpServlet {
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
		String order = null;
		order = StringEscapeUtils.escapeJava(request.getParameter("order"));
		/* Order parameter cannot be null */
		if(order == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("The order cannot be null");
			return;
		}
		
		/* Updates the DB only if the order is really changed */
		String oldOrder = user.getOrderString();
		user.setOrder(order);
		if(!user.getOrderString().equals(oldOrder)) {
			UserDAO uDAO = new UserDAO(connection);
			try {
				uDAO.updateAlbumsOrder(user);
			} catch (SQLException e) {
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				response.getWriter().println("Not possible to update albums order");
				return;
			}
		}
	}
}
