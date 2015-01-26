/**
 * Display: UI Console
 */
package org.gla.mcom.util;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

import org.gla.mcom.Registry;
import org.gla.mcom.impl.ReceiverImpl;
import org.gla.mcom.impl.RegistryImpl;
import org.gla.mcom.impl.SenderImpl;
import org.gla.mcom.init.Initialiser;

import com.sun.xml.internal.bind.v2.schemagen.xmlschema.List;

import jlibs.core.lang.Ansi;

public class Display {
	
	private static Map<String, String> commands = new LinkedHashMap<String, String>();
	public static final Ansi ansi_help = new Ansi(Ansi.Attribute.NORMAL, Ansi.Color.RED, null);
	public static final Ansi ansi_error = new Ansi(Ansi.Attribute.NORMAL, Ansi.Color.RED, null);
	public static final Ansi ansi_normal = new Ansi(Ansi.Attribute.NORMAL, Ansi.Color.RED, null);
	public static final Ansi ansi_header = new Ansi(Ansi.Attribute.NORMAL, Ansi.Color.BLUE, null);
	public static final Ansi ansi_normal2 = new Ansi(Ansi.Attribute.NORMAL, Ansi.Color.GREEN, null);
	public static final Ansi ansi_console = new Ansi(Ansi.Attribute.NORMAL, Ansi.Color.BLUE, null);
	
	private static SenderImpl sender;

	public Display(){
		System.out.println(" ");
    	System.out.println(ansi_header.colorize("________________TCPComponent__________________"));
    	System.out.println(ansi_header.colorize("     Component message passing via sockets    "));
    	System.out.println(ansi_normal2.colorize("running on:"+Initialiser.local_address.getHostAddress()));
    	System.out.println(ansi_normal2.colorize("Listening port:"+ReceiverImpl.listenSocket.getLocalPort()));
    	System.out.println(ansi_header.colorize("______________________________________________"));
    	System.out.println(ansi_normal.colorize("Type ? for help                               "));
    	initCommands();
       	console();  
	}
	
	private static void initCommands(){//
		commands.put("ipr~ <ipr>", "set ip address<hip> of message recipient");
		commands.put("p~ <p>", "set listening port<p> of message recipient");
		commands.put("<m>", "send message <m> to recipient");
		commands.put("c~ <c>", "check if host with name <c> exist");
		commands.put("end", "terminate");
		commands.put("start", "start acting as a registrar");
		commands.put("stop", "stop acting as a registrar");
		commands.put("reg", "register the host with this registrar");
		commands.put("dereg", "deregister the host with this registrar");
		commands.put("lookup", "retrieve a list of all the hosts registered with this registrar");
	}
	
	@SuppressWarnings("resource")
	private static void console(){
    	System.out.println(ansi_console.colorize  ("\ncom("+Initialiser.local_address.getHostAddress()+")>>"));

		Scanner scanner = new Scanner(System.in);
		String command = scanner.nextLine();
		
		execute(command);
	}

	private static void execute(String command){
		if(command.equals("?")){
			printCommands();
		}
		else if(command.contains("~")){
			int delimiterIndex = command.indexOf("~");

			if(delimiterIndex == command.length() - 1){
		    	System.out.println(ansi_error.colorize("ERROR:Invalid format"));
			}
			else{
				String operation = command.substring(0, delimiterIndex);
				String value = command.substring(delimiterIndex + 1);
				
				if(operation.equals("ipr")){
					Initialiser.receiver_ip = value;
				}
				else if(operation.equals("p")){
					if(Initialiser.receiver_ip == null){
				    	System.out.println(ansi_error.colorize("ERROR:receiver not set"));
					}
					else{
						if(isNumeric(value)){
							//person we are talking to's listening port - where we are sending it
							Initialiser.receiver_listening_port = new Integer(value).intValue();
							sender = new SenderImpl();
							boolean connected = sender.makeConnection();							
							if(!connected){
						    	System.out.println(ansi_error.colorize("ERROR:connection failed"));
							}							
						}
						else{
					    	System.out.println(ansi_error.colorize("ERROR:host port NaN"));
						}
					}
				}
				else if(operation.equals("c")){
					InetAddress address = IPResolver.getAddress(value);
					if(address != null){
						System.out.println(ansi_normal2.colorize("check passed"));
						System.out.println(ansi_normal2.colorize("running on:"+address.getHostAddress()));
				    	System.out.println(ansi_normal2.colorize("DNS:"+address.getHostName()));
					}
					else{
						System.out.println(ansi_normal2.colorize("check failed"));
					}
				}
			}
		}
		// startRegistrar
		else if (command.equals("start")) {
			if (RegistryImpl.startRegistrar()) {

				ReceiverImpl.registrars.put(Initialiser.local_address.getHostAddress() + ":" + ReceiverImpl.listenSocket.getLocalPort(), true);
				
				sender = new SenderImpl();
				System.out.println("Registrar service started!");
				sender.broadcastMessage(Initialiser.local_address.getHostAddress() + "update registrars:" + mapToString(), convertRegistrars());
			} else {
				System.out.println("Could not start registrar service! Is it already started?");
			}
		}
		// stopRegistrar
		else if (command.equals("stop")) {
			ReceiverImpl.registrars.remove(Initialiser.local_address.getHostAddress() + ":" + ReceiverImpl.listenSocket.getLocalPort());

			sender = new SenderImpl();
			if (RegistryImpl.stopRegistrar()) {
				sender.broadcastMessage(Initialiser.local_address.getHostAddress() + "update registrars:" + mapToString(), convertRegistrars());
				System.out.println("Registrar service stopped!");
			} else {
				System.out.println("Could not stop registrar service! Is it already stopped?");
			}
		}
		else if (command.equals("reg") || command.equals("dereg")) {
			command += "~" + Initialiser.local_address.getHostAddress() + ":" + ReceiverImpl.listenSocket.getLocalPort();

			sender.sendMessage(command, true);
		}
		else if(command.equals("end")){
			System.exit(0);
		}
		else if(command.length() > 0){
			sender.sendMessage(command, true);
		}
		console();
	}
	
	@SuppressWarnings("rawtypes")
	private static void printCommands(){
		Iterator it = commands.entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry pairs = (Map.Entry)it.next();
	        ansi_help.out("["+pairs.getKey() + "]: ");
	        System.out.println(pairs.getValue());
	    }
	}
	
	private static boolean isNumeric(String str) {
		try {
			Double.parseDouble(str);
		} catch (NumberFormatException nfe) {
			return false;
		}
		return true;
	}
	
	
	public static String[] convertRegistrars(){
		String[] registrars = new String[ReceiverImpl.registrars.size()];
		int i = 0;
		for (String host : ReceiverImpl.registrars.keySet()){
			registrars[i] = host;
			i++;
		}
		
		return registrars;
	}
	
	public static String mapToString(){
		String result = "";
		
		for (Entry element : ReceiverImpl.registrars.entrySet()){
			result += element.getKey() + "&" + element.getValue() + "&";
		}
		
		return result.substring(0, result.length() - 1);
	}
	
}
