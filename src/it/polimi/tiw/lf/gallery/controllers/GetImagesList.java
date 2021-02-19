package it.polimi.tiw.lf.gallery.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import it.polimi.tiw.lf.gallery.beans.Image;
import it.polimi.tiw.lf.gallery.dao.ImageDAO;
import it.polimi.tiw.lf.gallery.utils.ConnectionHandler;

@WebServlet("/GetImagesList")
public class GetImagesList extends HttpServlet {
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
		Integer albumId = null;
		try {
			albumId = Integer.parseInt(request.getParameter("album"));
		} catch (NumberFormatException | NullPointerException e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Incorrect or missing param values");
			return;
		}		

		/* Fetches the selected album images */
		ImageDAO iDAO = new ImageDAO(connection);
		List<Image> images = null;
		try {
			images = iDAO.findImagesByAlbum(albumId);
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Not possible to recover images");
			return;
		}
		
		/* Redirect to home page and add images as parameter */
		Gson gson = new GsonBuilder().setDateFormat("yyyy MM dd").create();
		String json = gson.toJson(images);		
		
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write(json);
	}
}
