/**
 * Display: UI Console
 * 
 * @Author Inah Omoronyia School of Computing Science, University of Glasgow 
 */
package mcom.console;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;

import mcom.init.Initialiser;
import mcom.kernel.impl.StubImpl;
import mcom.kernel.util.Metadata;
import mcom.wire.impl.ReceiverImpl;
import mcom.wire.util.DynamicRegistrarDiscovery;
import mcom.wire.util.RegistrarService;
import jlibs.core.lang.Ansi;

public class Display {
	
	private static Map<String, String> commands = new LinkedHashMap<String, String>();
	public static final Ansi ansi_help = new Ansi(Ansi.Attribute.NORMAL, Ansi.Color.RED, null);
	public static final Ansi ansi_error = new Ansi(Ansi.Attribute.NORMAL, Ansi.Color.RED, null);
	public static final Ansi ansi_normal = new Ansi(Ansi.Attribute.NORMAL, Ansi.Color.RED, null);
	public static final Ansi ansi_header = new Ansi(Ansi.Attribute.NORMAL, Ansi.Color.BLUE, null);
	public static final Ansi ansi_normal2 = new Ansi(Ansi.Attribute.NORMAL, Ansi.Color.GREEN, null);
	public static final Ansi ansi_console = new Ansi(Ansi.Attribute.NORMAL, Ansi.Color.BLUE, null);
	public static Scanner scanner;
	public Display(){
		System.out.println(" ");
    	System.out.println(ansi_header.colorize("_________________mCom-1.0.1________________"));
    	System.out.println(ansi_header.colorize(" A lightweight software component platform "));
    	System.out.println(ansi_normal2.colorize("running on:"+Initialiser.local_address.getHostAddress()));
    	System.out.println(ansi_normal2.colorize("Listening port:"+ReceiverImpl.listenSocket.getLocalPort()));
    	System.out.println(ansi_header.colorize("___________________________________________"));
    	System.out.println(ansi_normal.colorize("Type ? for help                            "));
    	initCommands();
       	console();
	}
	
	private static void initCommands(){//
		commands.put("isreg%<bool>", "Switch registrar on/off");
		commands.put("drs", "execute dynamic registrar discovery");
		commands.put("reg", "show available registrars");
		commands.put("con", "create connection to host");
		commands.put("deploy", "deploys all bundles in LocalBundleDir directory");
		commands.put("undeploy%<bundleId>", "undeploys specified bundle");
		commands.put("llookup", "Local lookup all bundle contracts in LocalBundleDir");
		commands.put("rlookup", "Remote lookup all bundle contracts with known Registrars");
		commands.put("adv%<bundleId>", "advertise specified bundleId on known Registers");
		commands.put("unadv%<bundleId>", "stop advertising specified bundleId on known Registers");
		commands.put("ladv", "List all adverts in this Register");
		commands.put("invoke", "A remote invocation of a specified  bundle contract");
		
		commands.put("end", "terminate");
	}
	
	private static void console(){
    	//System.out.println(ansi_console.colorize  ("\ncom("+Initialiser.local_address.getHostAddress()+")>>"));

		scanner = new Scanner(System.in);
		String command = scanner.nextLine();
		execute(command);		
	}

	@SuppressWarnings("rawtypes")
	private static void execute(String command){
		if(command.equals("?")){
			printCommands();
		}
		else if(command.contains("%")){
			String[] c = command.split("%");
			if(c.length <2){
		    	System.out.println(ansi_error.colorize("ERROR:Invalid format"));
			}
			else{
				String operation = c[0];
				String value = c[1];
				
				if(operation.equals("isreg")){
					if(value.equalsIgnoreCase("true")){
						Initialiser.reg_ser = new RegistrarService();
						Initialiser.reg_ser.startRegistrarService();						
					}
					else if(value.equalsIgnoreCase("false")){
						Initialiser.reg_ser.stopRegistrarService();						
					}
				}
				else if(operation.equals("adv")){
					if(isNumeric(value)){
						new StubImpl().advertise(new Integer(value), true);
					}
					else{
						System.err.println("NaN");
					}
				}
				else if(operation.equals("unadv")){
					if(isNumeric(value)){
						new StubImpl().advertise(new Integer(value), false);
					}
					else{
						System.err.println("NaN");
					}
				}
				else if(operation.equals("undeploy")){
					if(isNumeric(value)){
						new StubImpl().undeploy(new Integer(value));
					}
					else{
						System.err.println("NaN");
					}
				}
			}
		}
		else if(command.equals("drs")){
			Initialiser.dynDis = new DynamicRegistrarDiscovery();
			Initialiser.dynDis.doDynamicRegistersDiscovery();
		}
		else if(command.equals("reg")){	
			if(DynamicRegistrarDiscovery.getActiveRegistrars() == null || DynamicRegistrarDiscovery.getActiveRegistrars().size() ==0){
				System.err.println("No known Registrar");
			}
			else{
				for(String regip_port:DynamicRegistrarDiscovery.getActiveRegistrars()){
					String [] res = regip_port.split(":");
					String serviceip = res[0];
					String serviceport = res[1];
			        System.out.println("ip "+serviceip + " : port " + serviceport);

				}
			}
			
		}
		// AX3 State implementation.
		else if(command.equals("con")){
			new StubImpl().connect();

		}
		else if(command.equals("deploy")){
			new StubImpl().deploy();
		}

		else if(command.equals("llookup")){
			new StubImpl().localLookup();
		}
		else if(command.equals("rlookup")) {
			new StubImpl().remoteLookup();
		}
		else if(command.equals("invoke")){
			new StubImpl().invoke();
		}
		else if(command.equals("ladv")) {
			RegistrarService.getAdverts();
			Iterator it = RegistrarService.getAdverts().entrySet().iterator();
		    while (it.hasNext()) {
		        Map.Entry pairs = (Map.Entry)it.next();
		        ansi_help.out("["+pairs.getKey() + "]: ");
		        System.out.println(pairs.getValue());
		    }
		}
		else if(command.equals("end")){
			System.exit(0);
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
	
	public static boolean isNumeric(String str) {
		try {
			Double.parseDouble(str);
		} 
		catch (NumberFormatException nfe) {
			return false;
		}
		return true;
	}
}
