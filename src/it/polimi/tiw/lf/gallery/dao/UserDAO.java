package it.polimi.tiw.lf.gallery.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import it.polimi.tiw.lf.gallery.beans.User;
import it.polimi.tiw.lf.gallery.exceptions.BadUserData;

public class UserDAO {
	private Connection connection;
	
	public UserDAO(Connection connection) {
		this.connection = connection;
	}
	
	public User checkCredentials(String identifier, String password) throws SQLException {
		User user = null;
		String query = "SELECT id, username, albums FROM user WHERE (username = ? OR email = ?) AND password = ?";
		try(PreparedStatement pstatement = connection.prepareStatement(query)) {
			pstatement.setString(1, identifier);
			pstatement.setString(2, identifier);
			pstatement.setString(3, password);
			try(ResultSet result = pstatement.executeQuery()) {
				if(result.next()) {
					user = new User();
					user.setId(result.getInt("id"));
					user.setUsername(result.getString("username"));
					user.setOrder(result.getString("albums"));
				}
			}
		}
		return user;
	}
	
	public boolean checkByUsername(String username) throws SQLException {
		String query = "SELECT id FROM user WHERE username = ?";
		try(PreparedStatement pstatement = connection.prepareStatement(query)) {
			pstatement.setString(1, username);
			try(ResultSet result = pstatement.executeQuery()) {
				if(result.next()) {
					return true;
				}
			}
		}
		return false;
	}
	
	public boolean checkByEmail(String email) throws SQLException {
		String query = "SELECT id FROM user WHERE email = ?";
		try(PreparedStatement pstatement = connection.prepareStatement(query)) {
			pstatement.setString(1, email);
			try(ResultSet result = pstatement.executeQuery()) {
				if(result.next()) {
					return true;
				}
			}
		}
		return false;
	}
	
	public void registerUser(String username, String email, String password) throws SQLException, BadUserData {
		if(username == null || username.equals(""))
			throw new BadUserData("Not valid username");
		if(email == null || email.equals(""))
			throw new BadUserData("Not valid email");
		if(password == null || password.equals(""))
			throw new BadUserData("Not valid password");
		
		String query = "INSERT into user (username, email, password) VALUES(?, ?, ?)";
		connection.setAutoCommit(false);
		try(PreparedStatement pstatement = connection.prepareStatement(query)) {
			pstatement.setString(1, username);
			pstatement.setString(2, email);
			pstatement.setString(3, password);
			pstatement.executeUpdate();
			connection.commit();
		} catch (SQLException e) {
			connection.rollback();
			throw e;
		}
	}
	
	public void updateAlbumsOrder(User user) throws SQLException {
		String query = "UPDATE user SET albums = ? WHERE id = ?";
		connection.setAutoCommit(false);
		try(PreparedStatement pstatement = connection.prepareStatement(query)) {
			pstatement.setString(1, user.getOrderString());
			pstatement.setInt(2, user.getId());
			pstatement.executeUpdate();
			connection.commit();
		} catch (SQLException e) {
			connection.rollback();
			throw e;
		}
	}
	
	public String findUsernameFromId(int userId) {
		String username = "Anonymous";
		String query = "SELECT username FROM user WHERE id = ?";
		try(PreparedStatement pstatement = connection.prepareStatement(query)) {
			pstatement.setInt(1, userId);
			try(ResultSet result = pstatement.executeQuery()) {
				if(result.next()) {
					username = result.getString("username");
				}
			} 
		} catch (SQLException e) { }
		return username;
	}
}
