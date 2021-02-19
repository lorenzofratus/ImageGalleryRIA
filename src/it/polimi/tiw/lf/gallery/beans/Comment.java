package it.polimi.tiw.lf.gallery.beans;

public class Comment {
	private int id;
	private int user;
	private String username;
	private String text;
	private int image;
	
	public int getId() { return id; }
	public void setId(int id) { this.id = id; }
	
	public int getUser() { return user; }
	public void setUser(int user) { this.user = user; }
	
	public String getUsername() { return username; }
	public void setUsername(String username) { this.username = username; }
	
	public String getText() { return text; }
	public void setText(String text) { this.text = text; }
	
	public int getImage() { return image; }
	public void setImage(int image) { this.image = image; }
}
