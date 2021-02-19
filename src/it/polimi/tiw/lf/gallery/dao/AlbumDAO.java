package it.polimi.tiw.lf.gallery.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

import it.polimi.tiw.lf.gallery.beans.Album;

public class AlbumDAO {
	private Connection connection;
	
	public AlbumDAO(Connection connection) {
		this.connection = connection;
	}
	
	public Map<Integer, Album> findAlbumsList() throws SQLException {
		Map<Integer, Album> albums = new LinkedHashMap<>();
		String query = "SELECT * FROM album ORDER BY date DESC";
		try(PreparedStatement pstatement = connection.prepareStatement(query)) {
			try(ResultSet result = pstatement.executeQuery()) {
				while(result.next()) {
					Album album = new Album();
					album.setId(result.getInt("id"));
					album.setTitle(result.getString("title"));
					album.setDate(result.getDate("date"));
					albums.put(album.getId(), album);
				}
			}
		}
		return albums;
	}
}
