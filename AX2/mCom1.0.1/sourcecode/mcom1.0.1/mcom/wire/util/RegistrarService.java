package mcom.wire.util;
/**
 * @Author Inah Omoronyia School of Computing Science, University of Glasgow 
 */

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import mcom.init.Initialiser;
import mcom.wire.impl.ReceiverImpl;
import mcom.wire.impl.SenderImpl;

public class RegistrarService {
	public static boolean isRegistrarService = false;
	private static Map<String,String> registedAdverts;//<String:header, String:body>
	private static MulticastSocket socket;
	private static Thread t_thread;
	
	public void startRegistrarService() {
		isRegistrarService = true;
		registedAdverts = new HashMap<String, String>();
		InetAddress multicastAddressGroup;
		try {
			multicastAddressGroup = InetAddress.getByName(RegistrarConstants.MULTICAST_ADDRESS_GROUP);
			int multicastPort = RegistrarConstants.MULTICAST_PORT;
			
			socket = new MulticastSocket(multicastPort);
			socket.joinGroup(multicastAddressGroup);
			
		} 
		catch (UnknownHostException e) {
			e.printStackTrace();
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
		
		t_thread = new Thread(new RegistrarService().new RegistrarServiceRunner());
		t_thread.start();
		
		System.out.println("RegistrarService active: "+isRegistrarService);		
	}
	
	public static void addAdvert(String header, String body){
		if(isRegistrarService){
			registedAdverts.put(header, body);
		}		
	}
	
	public static void removeAdvert(String header){
		registedAdverts.remove(header);
	}
	
	public static Map<String,String> getAdverts(){
		return registedAdverts;
	}
	
	class RegistrarServiceRunner implements Runnable {
		public void run() {
			try {
				while(isRegistrarService){
					byte[] buf = new byte[1000];
					DatagramPacket receivedPacket = new DatagramPacket(buf, buf.length);

					socket.receive(receivedPacket);
					String recStr = new String(receivedPacket.getData()).trim(); //valid format: REGPING

					System.out.println(recStr.trim());

					if(recStr.startsWith("REGPING")){
						//respond to request							
						String [] res = recStr.split(":");
						String serviceip = res[1].trim();
						String serviceport = res[2].trim();

						String resStr = "REGACCEPT:"+Initialiser.local_address.getHostAddress()+":"+ReceiverImpl.listenSocket.getLocalPort();							
						new SenderImpl().sendMessage(serviceip, new Integer(serviceport), resStr);														
					}					
				}
			}
			catch (IOException ioe) {}
		}
	}
	
	public void stopRegistrarService() {
		isRegistrarService = false;	
		registedAdverts = null;
		t_thread.interrupt();
		socket.close();
		System.out.println("RegistrarService active: "+isRegistrarService);
		
	}
}
