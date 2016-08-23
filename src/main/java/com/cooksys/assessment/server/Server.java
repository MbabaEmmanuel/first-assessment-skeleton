package com.cooksys.assessment.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Server implements Runnable {
	private Logger log = LoggerFactory.getLogger(Server.class);
//	private ArrayList<ClientHandler> clientList;
	
	
	private int port;
	private ExecutorService executor;
	
	public Server(int port, ExecutorService executor) {
		super();
		this.port = port;
		this.executor = executor;
//		clientList = new ArrayList<ClientHandler>();
	}
	  

	
	public void run() {
		log.info("server started");
		ServerSocket ss;
		try {
			ss = new ServerSocket(this.port);
			while (true) {
				Socket socket = ss.accept();
				ClientHandler handler = new ClientHandler(socket);
//				clientList.add(handler);
//				for(ClientHandler x : clientList){
//					executor.execute(handler);
//				}
				executor.execute(handler);
				
			}
		} catch (IOException e) {
			log.error("Something went wrong :/", e);
		}
	}
	
}



