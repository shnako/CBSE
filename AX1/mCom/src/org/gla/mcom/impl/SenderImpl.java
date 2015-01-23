/**
 * The Sender makes connection request and send messages to a connected recipient
 */

package org.gla.mcom.impl;

import java.net.*;
import java.io.*;

import org.gla.mcom.Sender;
import org.gla.mcom.init.Initialiser;
import org.gla.mcom.util.Display;

public class SenderImpl implements Sender{
	
	private boolean accepted;
	public boolean makeConnection() {
		
		try {
			System.out.println("initiating tcp connection to "+Initialiser.receiver_ip+":"+Initialiser.receiver_listening_port+")");
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
				System.out.println(Display.ansi_normal2.colorize("connection to "+Initialiser.receiver_ip+":"+Initialiser.receiver_listening_port+" "+r_message));
				
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
	
	public void sendMessage(String message) {
		if(accepted){
			
			try {
				Socket serverSocket = new Socket(Initialiser.receiver_ip,Initialiser.receiver_listening_port);
				DataInputStream in = new DataInputStream(serverSocket.getInputStream());
				DataOutputStream out = new DataOutputStream(serverSocket.getOutputStream());
				out.writeUTF(message); // UTF is a string encoding;
				try {
					System.out.println("Response:\r\n" + in.readUTF());
				} catch (EOFException e) {
					// Suppress.
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
		}
		else{
			System.out.println(Display.ansi_error.colorize("ERROR:No message recipient"));
		}		
	}		
}