/**
 * The Receiver starts a thread on the background that listens to incoming messages
 * and respond to connection request.
 */
package org.gla.mcom.impl;

import java.net.*;
import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.gla.mcom.Receiver;
import org.gla.mcom.Registry;
import org.gla.mcom.init.Initialiser;
import org.gla.mcom.util.Display;
import org.gla.mcom.util.IPResolver;

public class ReceiverImpl implements Receiver{
	public static ServerSocket listenSocket;
	
	public void receiveMessage() { 		
		Thread server_thread = new Thread(new ReceiverImpl().new ReceiverRunner());
		server_thread.start();		
	}
	
	class ReceiverRunner implements Runnable {
			
		private Map<String, Boolean> registrars = new HashMap<String, Boolean>();

		public void run() {
			listenSocket = IPResolver.configureHostListeningSocket(); 	

			while(true) {
				try {
					
					Socket clientSocket = listenSocket.accept();
					String clientSocketString = clientSocket.getInetAddress() + ":" + clientSocket.getPort();
					if(clientSocket !=null){
						DataInputStream in= new DataInputStream(clientSocket.getInputStream());
						DataOutputStream out =new DataOutputStream(clientSocket.getOutputStream());

						String r_message = in.readUTF();

						if(r_message.equals("ping")){ //client checking recipient existence
							boolean accepted = acceptPing(clientSocket);					
							String response = "";
							if(accepted){
								response = "accepted";	
								send(response, out);
								closeConnection(clientSocket);
							}
							else{
								response = "rejected";	
								send(response,out);
								closeConnection(clientSocket);
							}
						}
						else if(r_message.equals("disconnect")){
							System.out.println(Display.ansi_normal2.colorize("disconnecting "+clientSocketString));
							send(""+clientSocketString+" disconnected", out);
							closeConnection(clientSocket);
						}
						// lookup implementation
						else if(r_message.equals("lookup")){
							Registry registry = RegistryImpl.getRegistryInstance();
							if (registry == null) {
								send("This is not a registrar!", out);
							} else {
								String result = "Registered hosts:\r\n";
								int i = 0;
								for(String ip_port: registry.lookup()) {
									result += ip_port + "\r\n";
								}
								
								send(result, out);
								System.out.println(result);
							}
							closeConnection(clientSocket);
						}
						else if(r_message.contains("~")){
							int delimiterIndex = r_message.indexOf("~");

							if(delimiterIndex == r_message.length() - 1){
								System.out.println("ERROR:Invalid format");
							}
							else{
								String operation = r_message.substring(0, delimiterIndex);
								String value = r_message.substring(delimiterIndex + 1);
								
								// register implementation
								if(operation.equals("reg")){
									String message;
									Registry registry = RegistryImpl.getRegistryInstance();
									if (registry == null) {
										message = "This is not a registrar!";
									} else {
										if (registry.register(value)) {
											message = value + " has been registered";
										} else {
											message = value + " could not be registered, it is probably registered already";
										}
										
									}
									
									send(message, out);
									System.out.println(message);
									
									closeConnection(clientSocket);
								}
								// deregister implementation
								else if(operation.equals("dereg")){
									String message;
									Registry registry = RegistryImpl.getRegistryInstance();
									if (registry == null) {
										message = "This is not a registrar!";
									} else {
										if (registry.deregister(value)) {
											message = value + " has been deregistered";
										} else {
											message = value + " could not be deregistered, it is probably not registered yet";
										}
									}
									
									send(message, out);
									System.out.println(message);
									
									closeConnection(clientSocket);
								}
							}
						}
						else{
							parseRegistrars(r_message);
							System.out.println(Display.ansi_normal.colorize("["+clientSocketString+"]"+r_message));
							closeConnection(clientSocket);
						}	
					}
				} 
				catch(EOFException e) {
					System.out.println("EOF:"+e.getMessage());
				} 
				catch(IOException e) {
					System.out.println("IO:"+e.getMessage());
				}
				 
			}
		}
		
		private boolean acceptPing(Socket clientSocket){ //broadcasting own address		
			System.out.println(Display.ansi_normal2.colorize("now connected to "+clientSocket.getInetAddress()+":"+clientSocket.getPort()));
			boolean accepted = true;
			
			return accepted;
		}
		private void send(String message,DataOutputStream out){
			if(out !=null){
				try {
					out.writeUTF(message);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			else{
		    	System.out.println(Display.ansi_error.colorize("ERROR:No receiver"));
			}
		}
		
		private void closeConnection(Socket clientSocket){
			if (clientSocket != null){
				try {
					clientSocket.close();
					clientSocket = null;
				} catch (IOException e) {
					/* close failed */
				}
			}
		}
		
		private void parseRegistrars(String message){
			String host;
			
			if (message.contains("addRegistrar")){
				host = message.substring(0, message.indexOf("add"));
				registrars.put(host, true);
			}
			else if (message.contains("addclient")){
				host = message.substring(0, message.indexOf("add"));
				registrars.put(host, false);
			}
			else if (message.contains("removed")){
				host = message.substring(0, message.indexOf("removed"));
				registrars.remove(host);
			}
			
		}
		
		
	}

}