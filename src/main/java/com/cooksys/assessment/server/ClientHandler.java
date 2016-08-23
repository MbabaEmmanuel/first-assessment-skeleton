package com.cooksys.assessment.server;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cooksys.assessment.model.Message;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ClientHandler implements Runnable {
	
	private Logger log = LoggerFactory.getLogger(ClientHandler.class);
	private Socket socket;
	private ListOfUser allUsers;
	private ArrayList < BufferedReader> in;
	private ArrayList <PrintWriter> out;
	private ArrayList < ListOfUser> userList;

	public ClientHandler(Socket socket) {
		super();
		this.socket = socket;
	}
	Date date = new Date();
	
	

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
						this.userList.add( this.allUsers = new ListOfUser(message.getUsername(), this.socket));
						this.in.add( reader);
						this.out.add( writer);
						break;
					case "disconnect":
						log.info("user <{}> disconnected", message.getUsername());
						this.in.remove(message.getUsername());
						this.out.remove(message.getUsername());
						this.userList.remove(message.getUsername());
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
						for (ListOfUser user: userList) {
							String responseBroad = mapper.writeValueAsString(message);
							out.write(responseBroad);
							out.flush();	
						}
						break;
					
						
				}
			}

		} catch (IOException e) {
			log.error("Something went wrong :/", e);
		}
	}

}
