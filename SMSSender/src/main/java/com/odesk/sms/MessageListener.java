package com.odesk.sms;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Socket listening to shutdown message from client
 * Used to terminate the application gracefully
 * @author jayant
 *
 */
@Component("messageListener")
public class MessageListener {

	// maintains a list of shutdown notifiers
	static private final Log log = LogFactory.getLog(MessageListener.class);
	private List<ShutdownNotifier> observers = new ArrayList<ShutdownNotifier>();
	
	@Value("${server.socket.port}")
	private int port;
	
	@Value("${server.socket.exit.command}")
	private String exitMessage;
	
	ServerSocket server;
	Socket connection;
	ObjectOutputStream out;
	ObjectInputStream in;
	
	
	public void addObserver(ShutdownNotifier n){
		observers.add(n);
		
	}
	
	public void notifyObserver(){
		for(ShutdownNotifier s:observers){
			s.shutdown();
		}
	}
	
	/**
	 * Will shutdown producer/consumer threads once 
	 * it recieves notification from Client
	 */
	public void listen(){
		new Thread(new Runnable(){
			@Override
			public void run() {
				try{
					server = new ServerSocket(port);
					log.info("Waiting for Client Input---");
					connection = server.accept();
					log.info("Connection received from " + connection.getInetAddress().getHostName());
					out = new ObjectOutputStream(connection.getOutputStream());
					out.flush();
					while(!SmsLauncher.shutdown){
						in = new ObjectInputStream(connection.getInputStream());
						String message= null;
						do{
							message = (String)in.readObject();
							log.info("Message received>"+ message);
							if(message.equals(exitMessage)){
								log.info("Notifying Producer/Consumer threads to shut down !!!");
								notifyObserver();
								SmsLauncher.shutdown = true;
							}
						}while(!message.equals(exitMessage));			
					}
					log.info("Socket connection to be closed now !!!");
				}catch (IOException e) {
					log.error("IOException in message Listener, message="+e.getMessage());
					e.printStackTrace();
				} catch (Exception e) {
					log.error("Exception in message Listener, message="+e.getMessage());
					e.printStackTrace();
				}finally{
					try {
						if(in != null)
							in.close();
						server.close();
					} catch (IOException e) {
						log.error("IOException in message Listener, message="+e.getMessage());
						throw new RuntimeException(e);
					}
				}
			}	
		}).start();
	}
	
}
