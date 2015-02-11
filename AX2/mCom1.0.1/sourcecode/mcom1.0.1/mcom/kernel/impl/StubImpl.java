package mcom.kernel.impl;

/**
 * @Author Inah Omoronyia School of Computing Science, University of Glasgow 
 */
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.w3c.dom.Document;

import mcom.console.Display;
import mcom.init.Initialiser;
import mcom.kernel.Stub;
import mcom.kernel.processor.BundleDescriptor;
import mcom.kernel.processor.BundleDescriptorFactory;
import mcom.kernel.util.KernelUtil;
import mcom.wire.impl.ReceiverImpl;
import mcom.wire.impl.SenderImpl;
import mcom.wire.util.DynamicRegistrarDiscovery;
import mcom.wire.util.RemoteLookupService;

public class StubImpl implements Stub{

	public boolean deploy() {
		try {
			BundleDescriptorFactory.buildBundleDescriptors();	
			System.out.println("bundle deployment completed");
			return true;
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return false;
	}

	public boolean advertise(int bundleId) {
		//Advertise a local BundleDescriptor with bundleId on known registers
		boolean advertised = false;
		for(BundleDescriptor bd:Initialiser.bundleDescriptors){
			if(bd.getBundleId() == bundleId){
				
				StringBuilder message = new StringBuilder();
				message.append("ADVERTHEADER-"+Initialiser.local_address.getHostAddress()+"__"+ReceiverImpl.listenSocket.getLocalPort());
				message.append(System.getProperty("line.separator"));
				message.append("ADVERTBODY-"+bd.getBDString());
								
				//advertise to all known Registers
				String advert = message.toString();	
				if(DynamicRegistrarDiscovery.getActiveRegistrars() == null || DynamicRegistrarDiscovery.getActiveRegistrars().size() ==0){
					System.err.println("No known Registrar");
				}
				else{
					for(String regip_port:DynamicRegistrarDiscovery.getActiveRegistrars()){
						String [] res = regip_port.split("__");
						String serviceip = res[0];
						String serviceport = res[1];
						new SenderImpl().sendMessage(serviceip, new Integer(serviceport), advert);	

					}
				    advertised = true;	
				}				
			}
		}
		if(!advertised){
			System.err.println("Advertised: "+advertised);
		}
		else{
			System.out.println("Advertised: "+advertised);
		}
		
		return true;
	}
	
	public void localLookup() {
		BundleDescriptor [] bds = KernelUtil.loadBundleDescriptors();
		System.out.println("No bundles: "+bds.length);
		for(int i =0;i<bds.length;i++){			
			Initialiser.addBundleDescriptor(bds[i]);
			int m = i+1;
			System.out.println("||("+m+") BundleID:"+bds[i].getBundleId()+" BundleName:"+bds[i].getBundleName()+"||");
			System.out.println(bds[i]);			
		}			
	}
		
	public void remoteLookup() {
		// lookup on all contracts advertised on known Registers
		// (This function is similar to localLookup() beside the requirement that lookup 
		// should be executed on known remote Registers)	
		RemoteLookupService.doRemoteLookup();
	}

	@SuppressWarnings("rawtypes")
	public void invoke() {
		//Invoke a specific contract from the lookup list	
		int bundleId = -1;
		String contractName = null;
		HashMap<String,String> parameters = new HashMap<String,String>(); //parameter name:value
		
		System.out.println("Input BundleID:");

		String bid = Display.scanner.nextLine();
		if(bid !=null && bid.trim().length()>0){
			if(Display.isNumeric(bid.trim())){
				bundleId = new Integer(bid.trim());
				
				//verify bundleId from
				Map<String, String> lookupResults = RemoteLookupService.getLookupResults();
				if(lookupResults == null || lookupResults.size() ==0){
					System.err.println("Empty rlookup");
					return;
				}
				Iterator it = lookupResults.entrySet().iterator();
				
				boolean bid_exit = false;
				while (it.hasNext()) {
				  Map.Entry pairs = (Map.Entry)it.next();
				  String header = (String)pairs.getKey();
				  String body = (String)pairs.getValue();
				  
				  Document bd_doc = KernelUtil.decodeTextToXml(body.trim());
				    
				  if(bundleId == KernelUtil.retrieveBundleId(bd_doc)){
					  bid_exit = true;
					  
					  System.out.println("Input Contract Name:");		
					  String cn = Display.scanner.nextLine();
					  if(cn !=null && cn.trim().length()>0){
						  contractName = cn.trim();
						  ArrayList<String> cnames = KernelUtil.retrieveContractNames(bd_doc);
						  boolean cname_exist = false;
						  for(String cname: cnames){
							  if(cname.equals(contractName)){
								  cname_exist = true;
								  System.err.println("NOTE: This version of mCom only takes String or primitive parameter types");
								  //BUG FIX:mCom does not handle polymorphism (more than one contract with same contractName but different parameters) 
								  HashMap<Object,Object> evparameters = KernelUtil.retrieveParameters(bd_doc, contractName);
								  
								  Iterator evit = evparameters.entrySet().iterator();
								  while (evit.hasNext()) {
									  Map.Entry evpairs = (Map.Entry)evit.next();
								      String p_name = (String)evpairs.getKey();
								      //String p_value = (String)evpairs.getValue();
								      
								      System.out.println("Input parameter value of type:"+ p_name);
								      String p1 = Display.scanner.nextLine();
										
										if(p1 !=null && p1.trim().length()>0){
											p1 = p1.trim();
											
											if(p_name.contains("String")){
												parameters.put(p_name, p1);
											}
											else if(p_name.contains("Float") || p_name.contains("float")){
												if(Display.isNumeric(p1)){
													parameters.put(p_name, p1);
												}
												else{
													System.err.println("invalid parameter type");
													return;
												}
											}
											else if(p_name.contains("Integer") || p_name.contains("int")){
												if(Display.isNumeric(p1)){
													parameters.put(p_name, p1);													
												}
												else{
													System.err.println("invalid parameter type");
													return;
												}
											}
											else if(p_name.contains("Double") || p_name.contains("double")){
												if(Display.isNumeric(p1)){
													parameters.put(p_name, p1);	
												}
												else{
													System.err.println("invalid parameter type");
													return;
												}
											}
										}
										else{
											System.err.println("invalid parameter type");
											return;
										}		
								  }
								  sendRemoteCall(bundleId, contractName,parameters,header);

							  }
						  }
						  if(!cname_exist){
							  System.err.println("invalid contract name");
							  return;
						  }
					  }
				  }
				}
				if(!bid_exit){
					System.err.println("invalid bundleId");
					return;
				}
			}
		}
		else{
			System.err.println("BundleID is invalid");
			return;
		}		
	}
	
	private static void sendRemoteCall(int bundleId, String contractName,	HashMap<String, String> parameters, String header) {
		
		String s [] = header.split("__");
		String bhost_ip = s[0];
		String bhost_port = s[1];
				
		String invoke_request_header = "INVOKEREQUESTHEADER-FROM-"+Initialiser.local_address.getHostAddress()+"__"+ReceiverImpl.listenSocket.getLocalPort()+"-TO-"+bhost_ip+"__"+bhost_port;
		
		String invoke_request_body = "INVOKEREQUESTBODY-";
		Document remoteCallEncoding = KernelUtil.encodeRemoteCallAsxml(bhost_ip, new Integer(bhost_port.trim()),bundleId, contractName, parameters);
		invoke_request_body =invoke_request_body+ KernelUtil.getBDString(remoteCallEncoding);
		String invokerMessage = invoke_request_header+invoke_request_body;
		new SenderImpl().sendMessage(bhost_ip, new Integer(bhost_port.trim()), invokerMessage);
	}	
	

}