package com.cooksys.assessment.server;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cooksys.assessment.model.Message;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ClientHandler implements Runnable {
	
	private Logger log = LoggerFactory.getLogger(ClientHandler.class);
	private Socket socket;
	static HashMap <String, BufferedReader> in = new HashMap <>();
	static HashMap <String, PrintWriter> out = new HashMap <>();
	static HashMap <String, ListOfUser> userList = new HashMap <>();

	public ClientHandler(Socket socket) {
		super();
		this.socket = socket;
	}
	Date date = new Date();
	SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy h:mm:ss a");
	String formattedDate = sdf.format(date);
	
	

	public void run() {
		try {

			ObjectMapper mapper = new ObjectMapper();
			BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			PrintWriter writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
			
			
			
			while (!socket.isClosed()) {
				String raw = reader.readLine();
				Message message = mapper.readValue(raw, Message.class);

				switch (message.getCommand()) {
					case "connect":
						log.info("user <{}> connected", message.getUsername());
						ClientHandler.userList.put(message.getUsername(), new ListOfUser(message.getUsername(), this.socket));
						message.setContents(formattedDate + ": " + message.getUsername() + " has connected");
						ClientHandler.in.put(message.getUsername(), reader);
						ClientHandler.out.put(message.getUsername(), writer);
						for (ListOfUser users: userList.values()){
						String connectionAlert = mapper.writeValueAsString(message);
						out.get(users.getUserName()).write(connectionAlert);
						out.get(users.getUserName()).flush();
						}
						break;
					case "disconnect":
						log.info("user <{}> disconnected", message.getUsername());
						for (ListOfUser use: userList.values()){
							message.setContents(formattedDate + ": " + message.getUsername() + " has disconnected");
							String disconnectionAlert = mapper.writeValueAsString(message);
							out.get(use.getUserName()).write(disconnectionAlert);
							out.get(use.getUserName()).flush();
						}
						ClientHandler.in.remove(message.getUsername());
						ClientHandler.out.remove(message.getUsername());
						ClientHandler.userList.remove(message.getUsername());
						this.socket.close();
						break;
					case "echo":
						log.info("user <{}> echoed message <{}>", message.getUsername(), message.getContents());
						String response = mapper.writeValueAsString(message);
						writer.write(response);
						writer.flush();
						break;
					case "broadcast":
						log.info("user <{}> broadcasted <{}>", message.getUsername(), message.getContents());
						message.setContents(formattedDate + ": " + message.getUsername() + " (all): " + message.getContents());
//						ClientHandler.in.put(message.getUsername(), reader);
//						ClientHandler.out.put(message.getUsername(), writer);
						for (ListOfUser user: userList.values()) {
							String responseBroad = mapper.writeValueAsString(message);
							out.get(user.getUserName()).write(responseBroad);
							out.get(user.getUserName()).flush();	
						}
						break;
					case "users":
						log.info("user <{}> wants all users", message.getUsername());
						String msg = formattedDate + ": " + "currently connected users: " + "\n";
						message.setContents(msg);
						String respondUser = mapper.writeValueAsString(message.getContents());
						out.get(message.getUsername()).write(respondUser);
						out.get(message.getUsername()).flush();
							Set<String> users = userList.keySet();
							for (String u: users){
							
							
							message.setContents(u + "\n");
							
							String respondUser1 = mapper.writeValueAsString(message.getContents());
							out.get(message.getUsername()).write(respondUser1);
							out.get(message.getUsername()).flush();
							}
						
						
						break;
					default:
						for (ListOfUser userName: userList.values()){
						if(message.getCommand() == userName.getUserName() ){
							message.setContents(formattedDate + ": " + message.getUsername() + " (all): " + message.getContents());
							String responseBroad = mapper.writeValueAsString(message);
							out.get(userName.getUserName()).write(responseBroad);
							out.get(userName.getUserName()).flush();	
						}
				}
						
						
						
					
					
//					case "users":
//						log.info("user <{}> wants all users", message.getUsername());
//						String userResponse = mapper.ClientHandler.userList.keySet();
//						out.get(message.getUsername()).write(userResponse);
//						out.get(message.getUsername()).flush();
//						break;
						
						
						
			
					
						
				}
			}

		} catch (IOException e) {
			log.error("Something went wrong :/", e);
		}
	}

}