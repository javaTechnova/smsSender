package com.odesk.sms;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Properties;

/**
 * To be invoked when the application needs to be shut
 * allows for graceful shutdown
 * usage 
 * java ClientCommandUtility exit
 * The message 'exit' can be referenced from 
 * "server.socket.exit.command" in env.properties file
 * @author jayant
 *
 */
public class ClientCommandUtility {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws IOException, InterruptedException {
		
		Properties prop = new Properties();
		String configFile = null;
		if(args.length == 1 && args[0].equals("dev")){
			configFile = System.getProperty("user.dir")+System.getProperty("file.separator")
			+"src"+System.getProperty("file.separator")+"main"+System.getProperty("file.separator")
			+"external"+System.getProperty("file.separator")+"config"
			+System.getProperty("file.separator")+"env.properties";
		}else{
			configFile = System.getProperty("user.dir")+System.getProperty("file.separator")
							+"config"+System.getProperty("file.separator")+"env.properties";
		}	
		prop.load(new FileInputStream(configFile));
		int port = Integer.valueOf(prop.getProperty("server.socket.port"));
		Socket client = null;
		ObjectOutputStream out = null;
		ObjectInputStream in = null;
		try{
			client = new Socket("localhost",port);
			out = new ObjectOutputStream(client.getOutputStream());
			in = new ObjectInputStream(client.getInputStream());
			String message = prop.getProperty("server.socket.exit.command");
		
			out.writeObject(message);
			out.flush();
			
			
			System.out.println("Pushed message to server, wait for server to shutdown !!!");
		}
		catch(IOException ioException){
			ioException.printStackTrace();
		} finally{
			try{
				out.close();
				in.close();
				client.close();
			}
			catch(IOException ioException){
				ioException.printStackTrace();
			}
		}
	}

}
