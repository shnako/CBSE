/**
 * The Receiver starts a thread on the background that listens to incoming messages
 * and respond to connection request.
 */
package org.gla.mcom.impl;

import java.net.*;
import java.io.*;

import org.gla.mcom.Receiver;
import org.gla.mcom.util.Display;
import org.gla.mcom.util.IPResolver;

public class ReceiverImpl implements Receiver{
	public static ServerSocket listenSocket;
	
	public void receiveMessage() { 		
		Thread server_thread = new Thread(new ReceiverImpl().new ReceiverRunner());
		server_thread.start();		
	}
	
	class ReceiverRunner implements Runnable {
			
		public void run() {
			listenSocket = IPResolver.configureHostListeningSocket(); 	

			while(true) {
				try {
					Socket clientSocket = listenSocket.accept();	
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
							System.out.println(Display.ansi_normal2.colorize("disconnecting "+clientSocket.getInetAddress()+":"+clientSocket.getPort()));
							send(""+clientSocket.getInetAddress()+":"+clientSocket.getPort()+" disconnected", out);
							closeConnection(clientSocket);
						}
						else{
							System.out.println(Display.ansi_normal.colorize("["+clientSocket.getInetAddress()+":"+clientSocket.getPort()+"]"+r_message));
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
		
		private boolean acceptPing(Socket clientSocket){		
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
	}

}