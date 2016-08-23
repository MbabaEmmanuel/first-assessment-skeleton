package com.cooksys.assessment.server;
import java.net.Socket;

public class ListOfUser {
	private String userName;
	private Socket socket;
	
	public ListOfUser(String userName, Socket socket) {
		super();
		this.userName = userName;
		this.socket = socket;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public Socket getSocket() {
		return socket;
	}

	public void setSocket(Socket socket) {
		this.socket = socket;
	}
	
	

}
