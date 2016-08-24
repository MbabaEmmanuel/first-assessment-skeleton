package com.cooksys.assessment.model;

public class Message {
	private String username;
	private String command;
	private String contents;
	private String directUser;

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}

	public String getContents() {
		return contents;
	}

	public void setContents(String contents) {
		this.contents = contents;
	}
	

	public String getDirectUser()
	{
		return directUser;
	}

	public void setDirectUser(String directUser)
	{
		this.directUser = directUser;
	}



	
}
