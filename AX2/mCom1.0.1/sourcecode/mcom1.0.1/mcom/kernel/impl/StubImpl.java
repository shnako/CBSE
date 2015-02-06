package mcom.kernel.impl;

/**
 * @Author Inah Omoronyia School of Computing Science, University of Glasgow 
 */
import java.net.MalformedURLException;
import java.util.Scanner;

import mcom.bundle.Contract;
import mcom.bundle.ContractType;
import mcom.bundle.util.bParameter;
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
				message.append("ADVERT_HEADER:"+Initialiser.local_address.getHostAddress()+":"+ReceiverImpl.listenSocket.getLocalPort());
				message.append(System.getProperty("line.separator"));
				message.append("ADVERT_BODY:");
				message.append(System.getProperty("line.separator"));
				message.append("BundleID:"+bd.getBundleId());
				message.append(System.getProperty("line.separator"));
				message.append("BundleName:"+bd.getBundleName());
				message.append(System.getProperty("line.separator"));
				message.append("Contracts:");
				message.append(System.getProperty("line.separator"));
				
				for(int j=0; j<bd.getContracts().length;j++){
					int k = j+1;
					message.append("  ("+k+") "+bd.getContracts()[j].getBundleEntityContract().getMethodName());
					message.append("[Parameters:");					
					for(bParameter bp:bd.getContracts()[j].getBundleEntityContract().getbParameters()){
						message.append("-"+bp.getClassName()+" ");						
					}
					message.append("] [Returns:"+bd.getContracts()[j].getReturnType()+"] Type:"+ContractType.getType(bd.getContracts()[j].getContractType()));
					message.append(System.getProperty("line.separator"));
					message.append("      "+bd.getContracts()[j].getDescription());
					message.append(System.getProperty("line.separator"));
				}
				
				//advertise to all known Registers
				String advert = message.toString();	
				if(DynamicRegistrarDiscovery.getActiveRegistrars() == null || DynamicRegistrarDiscovery.getActiveRegistrars().size() ==0){
					System.err.println("No known Registrar");
				}
				else{
					for(String regip_port:DynamicRegistrarDiscovery.getActiveRegistrars()){
						String [] res = regip_port.split(":");
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
			
			System.out.println("BundleID:"+bds[i].getBundleId());
			System.out.println("BundleName:"+bds[i].getBundleName());
			System.out.println("Contracts:");
			for(int j=0; j<bds[i].getContracts().length;j++){
				int k = j+1;
				System.out.print("  ("+k+") "+bds[i].getContracts()[j].getBundleEntityContract().getMethodName());	
				System.out.print("[Parameters:");
				for(bParameter bp:bds[i].getContracts()[j].getBundleEntityContract().getbParameters()){
					System.out.print("-"+bp.getClassName()+" ");
				}
				System.out.print("] [Returns:"+bds[i].getContracts()[j].getReturnType()+"] Type:"+ContractType.getType(bds[i].getContracts()[j].getContractType()));
				System.out.println("");
				System.out.println("      "+bds[i].getContracts()[j].getDescription());
			}
		}			
	}
		
	public void remoteLookup() {
		// lookup on all contracts advertised on known Registers
		// (This function is similar to localLookup() beside the requirement that lookup 
		// should be executed on known remote Registers)	
		RemoteLookupService.doRemoteLookup();

	}

	public void invoke() {
		//Invoke a specific contract from the lookup list	
		int bundleId = -1;
		System.out.println("Input BundleID:");
		Scanner scanner = new Scanner(System.in);
		String bid = scanner.nextLine();
		if(bid !=null && bid.trim().length()>0){
			if(Display.isNumeric(bid.trim())){
				bundleId = new Integer(bid.trim());
			}
		}
		else{
			System.err.println("BundleID is invalid");
			return;
		}
		
		String contractName = null;
		System.out.println("Input Contract Name:");		
		String cn = scanner.nextLine();
		if(cn !=null && cn.trim().length()>0){
			contractName = cn.trim();
		}
		else{
			System.err.println("Contract Name is invalid");
			return;
		}
		boolean cexist = false;
		
		for(BundleDescriptor bd: Initialiser.bundleDescriptors){
			if(bd.getBundleId() == bundleId){
				for(Contract contract: bd.getContracts()){
					if(contract.getBundleEntityContract().getMethodName().equals(contractName)){
						System.err.println("NOTE: This version of mCom only takes String or primitive parameter types");
						for(bParameter oldp: contract.getBundleEntityContract().getbParameters()){
							System.out.println("Input parameter value of type:"+ oldp.getClassName());
							String p1 = scanner.nextLine();
							
							if(p1 !=null && p1.trim().length()>0){
								p1 = p1.trim();
								
								if(oldp.getClassName().contains("String")){
									bParameter newp = new bParameter();
									newp.setClassName(oldp.getClassName());
									newp.setValue(p1);
									contract.getBundleEntityContract().updatebParameter(oldp, newp);
								}
								else if(oldp.getClassName().contains("Float") || oldp.getClassName().contains("float")){
									if(Display.isNumeric(p1)){
										bParameter newp = new bParameter();
										newp.setClassName(oldp.getClassName());
										newp.setValue(new Float(p1));
										contract.getBundleEntityContract().updatebParameter(oldp, newp);
									}
									else{
										System.err.println("invalid parameter type");
										return;
									}
								}
								else if(oldp.getClassName().contains("Integer") || oldp.getClassName().contains("int")){
									if(Display.isNumeric(p1)){
										bParameter newp = new bParameter();
										newp.setClassName(oldp.getClassName());
										newp.setValue(new Integer(p1));
										contract.getBundleEntityContract().updatebParameter(oldp, newp);
									}
									else{
										System.err.println("invalid parameter type");
										return;
									}
								}
								else if(oldp.getClassName().contains("Double") || oldp.getClassName().contains("double")){
									if(Display.isNumeric(p1)){
										bParameter newp = new bParameter();
										newp.setClassName(oldp.getClassName());
										newp.setValue(new Double(p1));
										contract.getBundleEntityContract().updatebParameter(oldp, newp);
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
						cexist = true;		
						
						RemoteMComInvocation.executeRemoteCall(bd.getAddress(), bd.getPort(),contract);
					}
				}
			}
		}
		if(!cexist){
			System.err.println("Requested contract does not exist");
		}
		scanner.close();
	}

}
