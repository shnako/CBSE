/**
 * The Sender makes connection request and send messages to a connected recipient
 * @Author Inah Omoronyia School of Computing Science, University of Glasgow 
 */

package mcom.wire.impl;

import java.net.*;
import java.io.*;

import mcom.console.Display;
import mcom.init.Initialiser;
import mcom.kernel.util.KernelUtil;
import mcom.wire.Sender;
import mcom.wire.util.DynamicRegistrarDiscovery;
import mcom.wire.util.RegistrarService;
import mcom.wire.util.RemoteLookupService;

public class SenderImpl implements Sender{
	private boolean accepted = false;
	
	public boolean makeConnection() {
		boolean accepted = false;
		
		try {
			System.out.println("initiating connection to "+Initialiser.receiver_ip+":"+Initialiser.receiver_listening_port+")");
			String i_message = "ping";
			Socket serverSocket = new Socket(Initialiser.receiver_ip,Initialiser.receiver_listening_port);
			DataInputStream in = new DataInputStream(serverSocket.getInputStream());
			DataOutputStream out = new DataOutputStream(serverSocket.getOutputStream());
			out.writeUTF(i_message); // UTF is a string encoding;
			
			String r_message = in.readUTF();
			if(i_message != null){
				if(r_message.equals("rejected")){
					accepted = false;
				}
				else if(r_message.equals("accepted")){
					accepted = true;
				}
				System.out.println(Display.ansi_normal2.colorize("connection to "+Initialiser.receiver_ip+"__"+Initialiser.receiver_listening_port+" "+r_message));
				
			}			
			serverSocket.close();
		} 
		catch (UnknownHostException e) {
			System.out.println("Sock:" + e.getMessage());
		} 
		catch (EOFException e) {
			System.out.println("EOF:" + e.getMessage());
		} 
		catch (IOException e) {
			System.out.println("IO:" + e.getMessage());
		}
		
		return accepted;
	}
	
	public boolean sendMessage(String ip, int port, String message) {
		boolean ack = false;
		try {
			//System.out.println("initiating connection to "+ip+"__"+port+")");
			Socket serverSocket = new Socket(ip,port);
			DataInputStream in = new DataInputStream(serverSocket.getInputStream());
			DataOutputStream out = new DataOutputStream(serverSocket.getOutputStream());
			out.writeUTF(message); // UTF is a string encoding;
			
			String r_message = in.readUTF();
			if(message != null){
				if(r_message.contains("ack")){
					ack = true;
					System.out.println(Display.ansi_normal2.colorize(Initialiser.receiver_ip+"__"+Initialiser.receiver_listening_port+" "+r_message));
				}
				else if(r_message.startsWith("REGACCEPT")){
					String [] res = r_message.split("REGACCEPT-");
					String regip_port = res[1];
					DynamicRegistrarDiscovery.addActiveRegistrar(regip_port);					
				}
				
				else if(r_message.startsWith("ADVERTHEADER-")){
					
					String [] res = r_message.split("ADVERTBODY-");
					String part0 = res[0];
					String body = res[1];
					
					String []res1 = part0.split("ADVERTHEADER-");
					String header = res1[1];
					
					RegistrarService.addAdvert(header, body);
					
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

				else if(r_message.contains("INVOKERRESPONSEHEADER-")){ //invocation client
					String dr [] = r_message.split("INVOKERRESPONSEHEADER-FROM-");
					String d1 = dr[1];
					String d2 [] = d1.split("INVOKERESPONSEBODY-");
					//String fromtoipport = d2[0];
					String responsebody = d2[1];
					
					//print response body
					System.out.println(KernelUtil.prettyPrint(responsebody));

				}
				
			}			
			serverSocket.close();
		} 
		catch (UnknownHostException e) {
			System.out.println("Sock:" + e.getMessage());
		} 
		catch (EOFException e) {
			System.out.println("EOF:" + e.getMessage());
		} 
		catch (IOException e) {
			System.out.println("IO:" + e.getMessage());
		}
		
		return ack;
	}
	
	public void sendMessage(String message) {
		if(accepted){
			
			try {
				Socket serverSocket = new Socket(Initialiser.receiver_ip,Initialiser.receiver_listening_port);
				DataOutputStream out = new DataOutputStream(serverSocket.getOutputStream());
				out.writeUTF(message); // UTF is a string encoding;
				serverSocket.close();
			} 
			catch (UnknownHostException e) {
				System.out.println("Sock:" + e.getMessage());
			} 
			catch (EOFException e) {
				System.out.println("EOF:" + e.getMessage());
			} 
			catch (IOException e) {
				System.out.println("IO:" + e.getMessage());
			}			
		}
		else{
			System.out.println(Display.ansi_error.colorize("ERROR:No message recipient"));
		}		
	}		
}