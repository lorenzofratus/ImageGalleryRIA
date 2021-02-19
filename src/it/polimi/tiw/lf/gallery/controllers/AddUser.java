package it.polimi.tiw.lf.gallery.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;

import it.polimi.tiw.lf.gallery.dao.UserDAO;
import it.polimi.tiw.lf.gallery.exceptions.BadUserData;
import it.polimi.tiw.lf.gallery.utils.ConnectionHandler;

@WebServlet("/AddUser")
@MultipartConfig
public class AddUser extends HttpServlet {
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
		/* OWASP email regex */
		Pattern pattern = Pattern.compile("^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$");
		Matcher matcher;
		
		/* Extracts parameters from POST */
		String username = null;
		String email = null;
		String password = null;
		String passConfirm = null;
		try {
			username = StringEscapeUtils.escapeJava(request.getParameter("username"));
			email = StringEscapeUtils.escapeJava(request.getParameter("email"));
			password = StringEscapeUtils.escapeJava(request.getParameter("password"));
			passConfirm = StringEscapeUtils.escapeJava(request.getParameter("pass-confirm"));
			/* Initial parameters validation */
			if(username == null || username.equals("")) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.getWriter().println("Username cannot be empty");
				return;
			}
			if(email == null || email.equals("")) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.getWriter().println("Email cannot be empty");
				return;
			}
			matcher = pattern.matcher(email);
			if(!matcher.matches()) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.getWriter().println("Bad email format");
				return;				
			}
			if(password == null || password.equals("")) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.getWriter().println("Password cannot be empty");
				return;
			}
			if(passConfirm == null || !passConfirm.equals(password)) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.getWriter().println("The two passwors must be identical");
				return;
			}
		} catch (NumberFormatException | NullPointerException e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Incorrect or missing param values");
			return;
		}		
		
		/* Checks if username and email are already used before registering the user */
		UserDAO uDAO = new UserDAO(connection);
		try {
			if(uDAO.checkByUsername(username)) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.getWriter().println("Username already taken");
				return;
			}
			if(uDAO.checkByEmail(email)) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.getWriter().println("Email already used");
				return;
			}
			/* Registers the user */
			uDAO.registerUser(username, email, password);
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Not possible to register user");
			return;
		} catch (BadUserData e ) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Incorrect param values");
			return;
		}
		
		/* Redirects to login */
		request.getRequestDispatcher("CheckLogin").forward(request, response);
	}
}
