package mcom.kernel.impl;

/**
 * @author Inah Omoronyia School of Computing Science, University of Glasgow
 */

import mcom.console.Display;
import mcom.init.Initialiser;
import mcom.kernel.Stub;
import mcom.kernel.processor.BundleDescriptor;
import mcom.kernel.processor.BundleDescriptorFactory;
import mcom.kernel.util.KernelUtil;
import mcom.kernel.util.Metadata;
import mcom.kernel.util.StateManager;
import mcom.wire.Sender;
import mcom.wire.impl.ReceiverImpl;
import mcom.wire.impl.SenderImpl;
import mcom.wire.util.DynamicRegistrarDiscovery;
import mcom.wire.util.Helpers;
import mcom.wire.util.RemoteLookupService;
import org.w3c.dom.Document;

import java.net.MalformedURLException;
import java.util.*;

public class StubImpl implements Stub {

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

    public void undeploy(int bundleId) {
        StateManager stateManager = StateManager.getStateManager();

        BundleDescriptorFactory.removeBundleDescriptor(bundleId);
        stateManager.removeInstanceFromMemory(bundleId);
        System.out.println("bundle undeployment completed");
    }

    public boolean advertise(int bundleId, boolean isAdvertised) {
        //Advertise a local BundleDescriptor with bundleId on known registers
        boolean success = false;
        for (BundleDescriptor bd : Initialiser.bundleDescriptors) {
            if (bd.getBundleId() == bundleId) {

                Metadata meta = new Metadata();
                meta.addMetadata("advertised", "" + isAdvertised);
                //meta.addMetadata("test1", "test2");

                //advertise to all known Registers
                String advert = "ADVERTHEADER-" + Helpers.getStringRepresentationOfIpPort(Initialiser.local_address.getHostAddress(), ReceiverImpl.listenSocket.getLocalPort()) +
                        System.getProperty("line.separator") +
                        "ADVERTBODY-" + KernelUtil.getMetadataAndBDString(bd.getBDString(), meta);
                if (DynamicRegistrarDiscovery.getActiveRegistrars() == null || DynamicRegistrarDiscovery.getActiveRegistrars().size() == 0) {
                    System.err.println("No known Registrar");
                } else {
                    for (String regip_port : DynamicRegistrarDiscovery.getActiveRegistrars()) {
                        String[] res = Helpers.splitIpPort(regip_port);
                        String serviceip = res[0];
                        String serviceport = res[1];
                        new SenderImpl().sendMessage(serviceip, new Integer(serviceport), advert, true);

                    }
                    success = true;
                }
            }
        }
        if (!success) {
            System.err.println("Advertising failed!");
        } else {
            System.out.println("Advertised: " + isAdvertised);
        }

        return true;
    }
    
//	public void changeAccessLevel(Integer bundleId, boolean b) {
//        for (BundleDescriptor bd : Initialiser.bundleDescriptors) {
//            if (bd.getBundleId() == bundleId){
//            		bd.setAccessLevel(Access.SUPER);   
//            		System.out.println(bundleId+" updated");
//            	}
//            else{
//            	System.out.println(bundleId+" updated");
//            	}
//            }		
//		}

    public void localLookup() {
        BundleDescriptor[] bds = KernelUtil.loadBundleDescriptors();
        System.out.println("Number of bundles: " + bds.length);
        for (int i = 0; i < bds.length; i++) {
            Initialiser.addBundleDescriptor(bds[i]);
            int m = i + 1;
            System.out.println("||(" + m + ") BundleID:" + bds[i].getBundleId() + " BundleName:" + bds[i].getBundleName() + " StateType:" + bds[i].getStateType() + "||");
            System.out.println(bds[i]);
        }
    }

    public void remoteLookup() {
        // lookup on all contracts advertised on known Registers
        // (This function is similar to localLookup() beside the requirement that lookup
        // should be executed on known remote Registers)
        RemoteLookupService.doRemoteLookup();
    }

    public void run(String contractName){
        Map<String, String> rLookup = RemoteLookupService.getLookupResults();
        List<Map.Entry<String, String>> rankings = new ArrayList<Map.Entry<String, String>>(rLookup.size());

        for (Map.Entry<String, String> entry : rLookup.entrySet()){
            String bundle = entry.getValue();
            Document bd_doc = KernelUtil.decodeTextToXml(bundle.trim());
            int bundleId = KernelUtil.retrieveBundleId(bd_doc);

            if (hasContract(bd_doc, contractName)){
                rankings.add(entry);
            }
        }

        Collections.sort(rankings, new Comparator<Map.Entry<String, String>>(){

            @Override
            public int compare(Map.Entry<String, String> e1, Map.Entry<String, String> e2) {
                Document e1Bundle = KernelUtil.decodeTextToXml(e1.getValue().trim());
                Document e2Bundle = KernelUtil.decodeTextToXml(e2.getValue().trim());

                int e1Usage = Integer.parseInt(e1Bundle.getElementById("UsageCounter").getNodeValue());
                int e2Usage = Integer.parseInt(e2Bundle.getElementById("UsageCounter").getNodeValue());

                return Integer.compare(e1Usage, e2Usage);
            }
        });

        if (!rankings.isEmpty()) {
            Map.Entry<String, String> highestRank = rankings.get(0);
            Document bundle = KernelUtil.decodeTextToXml(highestRank.getValue().trim());
            HashMap<String, String> parameters = getParameters(bundle, contractName);

            if (parameters != null) {
                sendRemoteInvocation(KernelUtil.retrieveBundleId(bundle), contractName, parameters, highestRank.getKey());

            }
        }
        else{
            System.out.println("Contract \"" + contractName + "\" not available");
        }
    }

    public boolean hasContract(Document bd_doc, String contractName){
        for (String contract : KernelUtil.retrieveContractNames(bd_doc)){
            if (contract.equals(contractName)){
                return true;
            }
        }

        return false;
    }

    private HashMap<String, String> getParameters(Document bd_doc, String contractName){
        HashMap<String, String> parameters = new HashMap<String, String>(); //parameter name:value
        HashMap<Object, Object> evparameters = KernelUtil.retrieveParameters(bd_doc, contractName);

        for (Object o : evparameters.entrySet()) {
            Map.Entry evpairs = (Map.Entry) o;
            String p_name = (String) evpairs.getKey();
            //String p_value = (String)evpairs.getValue();

            System.out.println("Input parameter value of type:" + p_name);
            String p1 = Display.scanner.nextLine();

            if (p1 != null && p1.trim().length() > 0) {
                p1 = p1.trim();

                if (p_name.contains("String")) {
                    parameters.put(p_name, p1);
                } else if (p_name.contains("Float") || p_name.contains("float")) {
                    if (Display.isNumeric(p1)) {
                        parameters.put(p_name, p1);
                    } else {
                        System.err.println("invalid parameter type");
                        return null;
                    }
                } else if (p_name.contains("Integer") || p_name.contains("int")) {
                    if (Display.isNumeric(p1)) {
                        parameters.put(p_name, p1);
                    } else {
                        System.err.println("invalid parameter type");
                        return null;
                    }
                } else if (p_name.contains("Double") || p_name.contains("double")) {
                    if (Display.isNumeric(p1)) {
                        parameters.put(p_name, p1);
                    } else {
                        System.err.println("invalid parameter type");
                        return null;
                    }
                }
            } else {
                System.err.println("invalid parameter type");
                return null;
            }
        }

        return parameters;
    }

    @SuppressWarnings("rawtypes")
    public void invoke() {
        //Invoke a specific contract from the lookup list
        int bundleId;
        String contractName;

        System.out.println("Input BundleID:");
        String bid = Display.scanner.nextLine();
        if (bid == null || bid.trim().length() == 0 || !Display.isNumeric(bid.trim())) {
            System.err.println("BundleID is invalid");
            return;
        }
        
        bundleId = new Integer(bid.trim());

        //verify bundleId from
        Map<String, String> lookupResults = RemoteLookupService.getLookupResults();
        if (lookupResults == null || lookupResults.size() == 0) {
            System.err.println("Empty rlookup");
            return;
        }

        Iterator it = lookupResults.entrySet().iterator();
        boolean bid_exit = false;
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry) it.next();
            String header = (String) pairs.getKey();
            String body = (String) pairs.getValue();

            Document bd_doc = KernelUtil.decodeTextToXml(body.trim());

            if (bundleId == KernelUtil.retrieveBundleId(bd_doc)) {
                bid_exit = true;

                System.out.println("Input Contract Name:");
                String cn = Display.scanner.nextLine();
                if (cn != null && cn.trim().length() > 0) {
                    contractName = cn.trim();
                    ArrayList<String> cnames = KernelUtil.retrieveContractNames(bd_doc);
                    boolean cname_exist = false;
                    for (String cname : cnames) {
                        if (cname.equals(contractName)) {
                            cname_exist = true;
                            System.err.println("NOTE: This version of mCom only takes String or primitive parameter types");
                            //BUG FIX:mCom does not handle polymorphism (more than one contract with same contractName but different parameters)

                            HashMap<String, String> parameters = getParameters(bd_doc, contractName);
                            if (parameters == null){
                                return;
                            }
                            sendRemoteInvocation(bundleId, contractName, parameters, header);

                        }
                    }
                    if (!cname_exist) {
                        System.err.println("invalid contract name");
                        return;
                    }
                }
            }
        }
        if (!bid_exit) {
            System.err.println("invalid bundleId");
        }
    }

    private static void sendRemoteInvocation(int bundleId, String contractName, HashMap<String, String> parameters, String hostIpPort) {
        hostIpPort = hostIpPort.trim();
        Metadata meta = new Metadata();
        // TODO JON, add your security metadata here.

        String s[] = Helpers.splitIpPort(hostIpPort);
        String bhost_ip = s[0];
        String bhost_port = s[1];

        String invoke_request_header = "INVOKEREQUESTHEADER-FROM-" + Initialiser.getLocalIpPort() + "-TO-" + Helpers.getStringRepresentationOfIpPort(bhost_ip.trim(), Integer.parseInt(bhost_port.trim()));

        String invoke_request_body = "INVOKEREQUESTBODY-";
        Document remoteCallEncoding = KernelUtil.encodeRemoteCallAsxml(bhost_ip, new Integer(bhost_port.trim()), bundleId, contractName, parameters);
        invoke_request_body = invoke_request_body + KernelUtil.getMetadataAndBDString(KernelUtil.getBDString(remoteCallEncoding), meta);
        String invokerMessage = invoke_request_header + invoke_request_body;

        Sender sender = new SenderImpl();
        sender.sendMessage(bhost_ip, new Integer(bhost_port.trim()), invokerMessage, true);
        sender.sendMessage(bhost_ip, new Integer(bhost_port.trim()), "UPDATE-USAGE-COUNTER-" + bundleId, true);

    }

	public void upgradeAuthorisation(int bid) {
		
		BundleDescriptor bd = KernelUtil.loadBundleDescriptor(bid);
        String ip;
        int port;
        String host = bd.getAddress().getHostAddress();
        try {
            String[] addressComponents = Helpers.splitIpPort(host);
            ip = addressComponents[0];
            port = Integer.parseInt(addressComponents[1]);
        } catch (Exception ex) {
            System.err.println("Invalid host address!");
            return;
        }
        // Build message to send.
        String message = "UPGRADE-ACCESS-LEVEL-" + Initialiser.local_address.getHostAddress();
        new SenderImpl().sendMessage(ip, port, message,  true);
	}
}
