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

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import mcom.console.Display;
import mcom.kernel.impl.RemoteMComInvocation;
import mcom.kernel.util.KernelUtil;
import mcom.wire.Receiver;
import mcom.wire.util.DynamicRegistrarDiscovery;
import mcom.wire.util.IPResolver;
import mcom.wire.util.RegistrarService;
import mcom.wire.util.RemoteLookupService;

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
							System.out.println(Display.ansi_normal.colorize("["+clientSocket.getInetAddress()+"__"+clientSocket.getPort()+"]"+r_message));
							
							String [] res = r_message.split("REGACCEPT-");
							String regip_port = res[1];
							DynamicRegistrarDiscovery.addActiveRegistrar(regip_port);
							
							send(""+clientSocket.getInetAddress()+"__"+clientSocket.getPort()+" ack", out);
							closeConnection(clientSocket);
						}
						
						else if(r_message.startsWith("ADVERTHEADER-")){
							
							String [] res = r_message.split("ADVERTBODY-");
							String part0 = res[0];
							String body = res[1];
							
							String []res1 = part0.split("ADVERTHEADER-");
							String header = res1[1];
							
							RegistrarService.addAdvert(header, body);
							
							send(""+clientSocket.getInetAddress()+"__"+clientSocket.getPort()+" ack", out);
							closeConnection(clientSocket);
						}
						else if(r_message.startsWith("LOOKUPADVERTS-")){
														
							if(RegistrarService.isRegistrarService){								
								Iterator it = RegistrarService.getAdverts().entrySet().iterator();
							    while (it.hasNext()) {
							        Map.Entry pairs = (Map.Entry)it.next();
							        String header = (String)pairs.getKey();
							        String body = (String)pairs.getValue();							        
							        String message = "LOOKUPRESPONSEHEADER-"+header+"LOOKUPRESPONSEBODY-"+body;
									
							        send(message, out);
							    }
							}
							closeConnection(clientSocket);
						}
						else if(r_message.contains("LOOKUPRESPONSEHEADER-")){
							String [] s1 = r_message.split("LOOKUPRESPONSEHEADER-");
							String [] s2 = s1[1].split("LOOKUPRESPONSEBODY-");
							String header = s2[0];
							String body = s2[1];
							
							RemoteLookupService.addLookupResult(header, body);
							System.out.println("Lookup response:");
							System.out.println("ContractHost: "+header.trim());
							System.out.println(body.trim());
						}
						else if(r_message.contains("INVOKEREQUESTHEADER-")){ //invocation server
							
							String de[] = r_message.split("INVOKEREQUESTHEADER-FROM-");							
							String d1 = de[1];
							String d2 []= d1.split("-TO-");
							String fromipport = d2[0];
							String f[] = fromipport.split("__");
							String fromip = f[0].trim();
							String fromport = f[1].trim();
							String d3 = d2[1];
							String d4[] = d3.split("INVOKEREQUESTBODY-");
							String toipport = d4[0];
							String t[] = toipport.split("__");
							String toip = t[0];
							String toport = t[1];
							String invokebody = d4[1];
														
							Document inv_doc = KernelUtil.decodeTextToXml(invokebody.trim());
							
							Object result = RemoteMComInvocation.executeRemoteCall(inv_doc);
							
							Element dresult = inv_doc.createElement("Result");
						    dresult.appendChild(inv_doc.createTextNode(""+result));
						    inv_doc.getLastChild().appendChild(dresult);
						    
							String response_header = "INVOKERRESPONSEHEADER-FROM-"+toip.trim()+"__"+toport.trim()+"-TO-"+fromip.trim()+"__"+fromport.trim();
							
							String invoke_response_body = "INVOKERESPONSEBODY-";
							invoke_response_body =invoke_response_body+ KernelUtil.getBDString(inv_doc);
							String invokerMessage = response_header+invoke_response_body;
							
							new SenderImpl().sendMessage(fromip, new Integer(fromport), invokerMessage);
														
						}						
						else if(r_message.contains("INVOKERRESPONSEHEADER-")){ //invocation client
							String dr [] = r_message.split("INVOKERRESPONSEHEADER-FROM-");
							String d1 = dr[1];
							String d2 [] = d1.split("INVOKERESPONSEBODY-");
							//String fromtoipport = d2[0];
							String responsebody = d2[1];
							
							//print response body
							System.out.println(KernelUtil.prettyPrint(responsebody));

						}
						
						else{
							System.out.println(Display.ansi_normal.colorize("["+clientSocket.getInetAddress()+"__"+clientSocket.getPort()+"]"+r_message));
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
			System.out.println(Display.ansi_normal2.colorize("now connected to "+clientSocket.getInetAddress()+"__"+clientSocket.getPort()));
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