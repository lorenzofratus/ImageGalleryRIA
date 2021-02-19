package it.polimi.tiw.lf.gallery.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import it.polimi.tiw.lf.gallery.beans.Album;
import it.polimi.tiw.lf.gallery.beans.User;
import it.polimi.tiw.lf.gallery.dao.AlbumDAO;
import it.polimi.tiw.lf.gallery.utils.ConnectionHandler;

@WebServlet("/GetAlbumsList")
public class GetAlbumsList extends HttpServlet {
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
		/* Gets user info */
		User user = (User) request.getSession().getAttribute("user");		
		
		/* Fetches all the albums from DB */
		AlbumDAO aDAO = new AlbumDAO(connection);
		Map<Integer, Album> albums = null;
		try {
			albums = aDAO.findAlbumsList();
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Not possible to recover albums");
			return;
		}		
		
		/* Sorts the list according to the order of the user
		 * Appends any album whose id is not in the order */
		List<Integer> order = user.getOrder();
		Map<Integer, Album> sortedAlbums = new LinkedHashMap<>();
		for(Integer id : order) {
			Album album = albums.get(id);
			if(album != null)
				sortedAlbums.put(id, album);
		}
		sortedAlbums.putAll(albums);
		
		/* Redirect to home page and add albums as parameter */
		Gson gson = new GsonBuilder().setDateFormat("yyyy MM dd").create();
		String json = gson.toJson(sortedAlbums.values());		
		
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write(json);
	}	
}
