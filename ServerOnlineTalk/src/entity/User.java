package entity;

import java.io.PrintWriter;

import annotation.Column;
import annotation.Entity;

@Entity(tabName="user")
public class User {
	@Column(columnName="UserID",nullable=false,length=255)
	private String userID;
	@Column(columnName="UserPassword",nullable=false,length=255)
	private String password;
	@Column(columnName="UserName",nullable=false,length=255)
	private String userName;
	private boolean userStatus;
	
	public PrintWriter getWriter() {
		return writer;
	}
	public void setWriter(PrintWriter writer) {
		this.writer = writer;
	}
	private PrintWriter writer;
	
	public String getUserID() {
		return userID;
	}
	public void setUserID(String userID) {
		this.userID = userID;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public boolean isUserStatus() {
		return userStatus;
	}
	public void setUserStatus(boolean userStatus) {
		this.userStatus = userStatus;
	}
	
}
