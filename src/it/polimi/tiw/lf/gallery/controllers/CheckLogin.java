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

@WebServlet("/CheckLogin")
@MultipartConfig
public class CheckLogin extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection;

	public void init() throws ServletException {
		connection = ConnectionHandler.getConnection(getServletContext());
	}
	
	public void destroy() {
		try {
			ConnectionHandler.closeConnection(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {		
		/* Extracts and escapes parameter from POST */
		String username = null;
		String password = null;
		username = StringEscapeUtils.escapeJava(request.getParameter("username"));
		password = StringEscapeUtils.escapeJava(request.getParameter("password"));
		if(username == null || username.equals("")) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Username cannot be empty");
			return;
		}
		if(password == null || password.equals("")) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Password cannot be empty");
			return;
		}
		
		/* User authentication */
		UserDAO uDAO = new UserDAO(connection);
		User user = null;
		try {
			user = uDAO.checkCredentials(username, password);
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Internal server error, retry later");
			return;
		}
		
		/* If no user exists send error, otherwise adds user to session and go to home */
		if(user == null) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			response.getWriter().println("Incorrect credentials");
		} else {
			request.getSession().setAttribute("user", user);
			response.setStatus(HttpServletResponse.SC_OK);
			response.setContentType("application/json");
			response.setCharacterEncoding("UTF-8");
			response.getWriter().println(user.getUsername());
		}
	}

}
