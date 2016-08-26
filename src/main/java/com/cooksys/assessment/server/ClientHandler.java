package com.cooksys.assessment.server;
import java.util.Date;
import java.util.HashMap;
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
	public static final String ANSI_RESET = "\u001B[0m";
	public static final String ANSI_RED = "\u001B[31m";
	public static final String ANSI_GREEN = "\u001B[32m";
	public static final String ANSI_YELLOW = "\u001B[33m";
	public static final String ANSI_BLUE = "\u001B[34m";
	public static final String ANSI_PURPLE = "\u001B[35m";
	public static final String ANSI_CYAN = "\u001B[36m";
	public static final String ANSI_WHITE = "\u001B[37m";
	private Logger log = LoggerFactory.getLogger(ClientHandler.class);
	private Socket socket;
	static HashMap <String, BufferedReader> inputMessage = new HashMap <>();
	static HashMap <String, PrintWriter> outputMessage = new HashMap <>();
	static HashMap <String, Socket> userList = new HashMap <>();

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
				
				
				if(message.getCommand().charAt(0)== '@'){
					if(userList.containsKey(message.getCommand().substring(1)))
					{
						//looks for specific user in list, writes to given users PrintWriter and flushes message.
						log.info("user <{}> is sending a direct message to <{}>", message.getUsername(), message.getCommand());
						String directMessage = message.getCommand().substring(1);
						message.setContents(ANSI_BLUE + "`" + formattedDate + ": <" + message.getUsername() + "> (whispers): " + message.getContents()+ "`" +ANSI_RESET);
						String userResponse = mapper.writeValueAsString(message);
						outputMessage.get(directMessage).write(userResponse);
						outputMessage.get(directMessage).flush();
					}
					else 
 
					{
						message.setContents(ANSI_RED + "Smart, but not smart enough. Enter new command or new User!" + ANSI_RESET);
						String badUser = mapper.writeValueAsString(message);
						writer.write(badUser);
						writer.flush();
					}

				} 
				else
				{

				switch (message.getCommand()) {
					case "connect":
						if(message.getUsername().charAt(0) != ' ')
						{ 
							//if a space isn't entered or put before username, add  that username to userList,  then put user into inputmessage and outputmessage hashmap
							log.info("user <{}> connected", message.getUsername());
							ClientHandler.userList.put(message.getUsername(), this.socket);
							message.setContents(ANSI_GREEN + "`" + formattedDate + ": <" + message.getUsername() + "> has connected" + "`" + ANSI_RESET);
							ClientHandler.inputMessage.put(message.getUsername(), reader);
							ClientHandler.outputMessage.put(message.getUsername(), writer);
							// use output message hashmap to send message to each user
							for (String users: userList.keySet())
							{
								String connectionAlert = mapper.writeValueAsString(message);
								outputMessage.get(users).write(connectionAlert);
								outputMessage.get(users).flush();
							}
						}
						else 
						{
							//if user has space in front send error message and close user socket
						message.setContents(ANSI_RED + "Smart, but not smart enough. Enter new user!" + ANSI_RESET);
						String badUserName = mapper.writeValueAsString(message);
						writer.write(badUserName);
						writer.flush();
						this.socket.close();
						}
						
						break;
					case "disconnect":
						// remove user from all lists and write disconnect message to all users, lastly close socket
						log.info("user <{}> disconnected", message.getUsername());
						for (String userDisconnect: userList.keySet())
						{
							message.setContents(ANSI_RED + "`" + formattedDate + ": <" + message.getUsername() + "> has disconnected" + "`" + ANSI_RESET);
							String disconnectionAlert = mapper.writeValueAsString(message);
							outputMessage.get(userDisconnect).write(disconnectionAlert);
							outputMessage.get(userDisconnect).flush();
						}
						ClientHandler.inputMessage.remove(message.getUsername());
						ClientHandler.outputMessage.remove(message.getUsername());
						ClientHandler.userList.remove(message.getUsername());
						this.socket.close();
						break;
					case "echo":
						log.info("user <{}> echoed message <{}>", message.getUsername(), message.getContents());
						message.setContents(ANSI_YELLOW + "`" + formattedDate + ": " + message.getUsername() + " (echo): " + message.getContents() + "`" + ANSI_RESET);
						String response = mapper.writeValueAsString(message);
						writer.write(response);
						writer.flush();
						break;
					case "broadcast":
						log.info("user <{}> broadcasted <{}>", message.getUsername(), message.getContents());
						message.setContents(ANSI_WHITE + "`" + formattedDate + ": " + message.getUsername() + " (all): " + message.getContents() + "`" + ANSI_RESET);
						for (String user: userList.keySet()) 
						{
							String responseBroad = mapper.writeValueAsString(message);
							outputMessage.get(user).write(responseBroad);
							outputMessage.get(user).flush();	
						}
						break;
					case "users":
						log.info("user <{}> wants all users", message.getUsername());
						String userMessage = "`" + formattedDate + ": " + "currently connected users:`";
						for (String user: userList.keySet())
						{
							userMessage += "\n" + "`" +user + "`";
						}
						message.setContents(ANSI_PURPLE + userMessage + ANSI_RESET);
						String allUsers = mapper.writeValueAsString(message);
						writer.write(allUsers);
						writer.flush();
						break;
		
					
						}
				}
				
			}

		} catch (IOException e) {
			log.error("Something went wrong :/", e);
		}
	}

}