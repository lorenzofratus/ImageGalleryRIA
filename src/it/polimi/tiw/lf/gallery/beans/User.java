package it.polimi.tiw.lf.gallery.beans;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class User {
	private int id;
	private String username;
	private String email;
	private String password;
	private List<Integer> order;
	
	public int getId() { return id; }
	public void setId(int id) { this.id = id; }
	
	public String getUsername() { return username; }
	public void setUsername(String username) { this.username = username; }
	
	public String getEmail() { return email; }
	public void setEmail(String email) { this.email = email; }
	
	public String getPassword() { return password; }
	public void setPassword(String password) { this.password = password; }
	
	public List<Integer> getOrder() { return order; }
	public void setOrder(List<Integer> order) { this.order = order; }
	
	public String getOrderString() {
		return this.order.stream().map(Object::toString).collect(Collectors.joining(","));
	}
	public void setOrder(String orderString) {
        List<Integer> orderList = new ArrayList<>();
        for(String albumString : orderString.split(",")) {
            boolean valid = true;
        	Integer albumId = null;
        	try {
        		albumId = Integer.parseInt(albumString);
        	} catch (NumberFormatException e) {
        		valid = false;
        	}
        	if(valid)
        	    orderList.add(albumId);
        }
        this.order = orderList;
	}
} 
