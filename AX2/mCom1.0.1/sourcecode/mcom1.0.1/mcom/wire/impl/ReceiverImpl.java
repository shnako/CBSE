/**
 * The Receiver starts a thread on the background that listens to incoming messages
 * and respond to connection request.
 * @Author Inah Omoronyia School of Computing Science, University of Glasgow 
 */

package mcom.wire.impl;

import java.net.*;
import java.util.Iterator;
import java.util.Map;
import java.io.*;

import mcom.console.Display;
import mcom.wire.Receiver;
import mcom.wire.util.DynamicRegistrarDiscovery;
import mcom.wire.util.IPResolver;
import mcom.wire.util.RegistrarService;

public class ReceiverImpl implements Receiver{
	public static ServerSocket listenSocket;
	
	public void receiveMessage() { 		
		Thread server_thread = new Thread(new ReceiverImpl().new ReceiverRunner());
		server_thread.start();		
	}
	
	class ReceiverRunner implements Runnable {
			
		@SuppressWarnings("rawtypes")
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
						
						else if(r_message.startsWith("REGACCEPT")){
							System.out.println(Display.ansi_normal.colorize("["+clientSocket.getInetAddress()+":"+clientSocket.getPort()+"]"+r_message));
							
							String [] res = r_message.split("REGACCEPT:");
							String regip_port = res[1];
							DynamicRegistrarDiscovery.addActiveRegistrar(regip_port);
							
							send(""+clientSocket.getInetAddress()+":"+clientSocket.getPort()+" ack", out);
							closeConnection(clientSocket);
						}
						
						else if(r_message.startsWith("ADVERT_HEADER:")){
							
							String [] res = r_message.split("ADVERT_BODY:");
							String part0 = res[0];
							String body = res[1];
							
							String []res1 = part0.split("ADVERT_HEADER:");
							String header = res1[1];
							
							RegistrarService.addAdvert(header, body);
							
							send(""+clientSocket.getInetAddress()+":"+clientSocket.getPort()+" ack", out);
							closeConnection(clientSocket);
						}
						else if(r_message.startsWith("LOOKUP_ADVERTS:")){
														
							if(RegistrarService.isRegistrarService){								
								Iterator it = RegistrarService.getAdverts().entrySet().iterator();
							    while (it.hasNext()) {
							        Map.Entry pairs = (Map.Entry)it.next();
							        String header = (String)pairs.getKey();
							        String body = (String)pairs.getValue();							        
							        String message = "LOOKUP_RESPONSE_HEADER:"+header+"LOOKUP_RESPONSE_BODY:"+body;
									
							        send(message, out);
							    }
							}
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