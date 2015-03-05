package mcom.kernel.impl;

/**
 * @Author Inah Omoronyia School of Computing Science, University of Glasgow 
 */

import mcom.console.Display;
import mcom.init.Initialiser;
import mcom.kernel.Stub;
import mcom.kernel.processor.BundleDescriptor;
import mcom.kernel.processor.BundleDescriptorFactory;
import mcom.kernel.util.KernelUtil;
import mcom.kernel.util.Metadata;
import mcom.wire.impl.ReceiverImpl;
import mcom.wire.impl.SenderImpl;
import mcom.wire.util.*;
import org.w3c.dom.Document;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

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
        BundleDescriptorFactory.removeBundleDescriptor(bundleId);
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

                StringBuilder message = new StringBuilder();
                message.append("ADVERTHEADER-" + Helpers.getStringRepresentationOfIpPort(Initialiser.local_address.getHostAddress(), ReceiverImpl.listenSocket.getLocalPort()));
                message.append(System.getProperty("line.separator"));
                message.append("ADVERTBODY-" + KernelUtil.getMetadataAndBDString(bd.getBDString(), meta));

                //advertise to all known Registers
                String advert = message.toString();
                if (DynamicRegistrarDiscovery.getActiveRegistrars() == null || DynamicRegistrarDiscovery.getActiveRegistrars().size() == 0) {
                    System.err.println("No known Registrar");
                } else {
                    for (String regip_port : DynamicRegistrarDiscovery.getActiveRegistrars()) {
                        String[] res = Helpers.splitIpPort(regip_port);
                        String serviceip = res[0];
                        String serviceport = res[1];
                        new SenderImpl().sendMessage(serviceip, new Integer(serviceport), advert);

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

    public void localLookup() {
        BundleDescriptor[] bds = KernelUtil.loadBundleDescriptors();
        System.out.println("No bundles: " + bds.length);
        for (int i = 0; i < bds.length; i++) {
            Initialiser.addBundleDescriptor(bds[i]);
            int m = i + 1;
            System.out.println("||(" + m + ") BundleID:" + bds[i].getBundleId() + " BundleName:" + bds[i].getBundleName() + "||");
            System.out.println(bds[i]);
        }
    }

    public void remoteLookup() {
        // lookup on all contracts advertised on known Registers
        // (This function is similar to localLookup() beside the requirement that lookup
        // should be executed on known remote Registers)
        RemoteLookupService.doRemoteLookup();
    }

    // AX3 State implementation.
    public void connect() {
        // Get the host address.
        System.out.print("Host address in format <ip>__<port>: ");
        String host = Display.scanner.nextLine();
        if (host == null || host.isEmpty()) {
            System.err.println("Invalid host address!");
        }

        // Get the connection type.
        System.out.print("Input connection type (l - stateless, f - stateful, p - persistent: ");
        ConnectionType connectionType;
        try {
            connectionType = ConnectionType.fromString(Display.scanner.nextLine());
        } catch (IllegalArgumentException ex) {
            System.err.println(ex.getMessage());
            return;
        }

        // Determine the address ip and port.
        String ip;
        int port;
        try {
            String[] addressComponents = Helpers.splitIpPort(host);
            ip = addressComponents[0];
            port = Integer.parseInt(addressComponents[1]);
        } catch (Exception ex) {
            System.err.println("Invalid host address!");
            return;
        }

        // Build message to send.
        String message = "CONNECTION-REQUEST|CLIENT-IP-" + Initialiser.local_address.getHostAddress();
        message += "CONNECTION-TYPE-" + connectionType.getText();

        new SenderImpl().sendMessage(ip, port, message, connectionType);
    }

    @SuppressWarnings("rawtypes")
    public void invoke() {
        //Invoke a specific contract from the lookup list
        int bundleId = -1;
        String contractName = null;
        HashMap<String, String> parameters = new HashMap<String, String>(); //parameter name:value

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
                            HashMap<Object, Object> evparameters = KernelUtil.retrieveParameters(bd_doc, contractName);

                            Iterator evit = evparameters.entrySet().iterator();
                            while (evit.hasNext()) {
                                Map.Entry evpairs = (Map.Entry) evit.next();
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
                                            return;
                                        }
                                    } else if (p_name.contains("Integer") || p_name.contains("int")) {
                                        if (Display.isNumeric(p1)) {
                                            parameters.put(p_name, p1);
                                        } else {
                                            System.err.println("invalid parameter type");
                                            return;
                                        }
                                    } else if (p_name.contains("Double") || p_name.contains("double")) {
                                        if (Display.isNumeric(p1)) {
                                            parameters.put(p_name, p1);
                                        } else {
                                            System.err.println("invalid parameter type");
                                            return;
                                        }
                                    }
                                } else {
                                    System.err.println("invalid parameter type");
                                    return;
                                }
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
        ClientConnectionDetails clientConnectionDetails = ClientConnectionManager.getClientConnectionManager().getConnection(hostIpPort);
        if (clientConnectionDetails != null) {
            meta.addMetadata("CONNECTION-ID", "" + clientConnectionDetails.getServerConnectionId());
        }
        // TODO JON, add your security metadata here.

        String s[] = Helpers.splitIpPort(hostIpPort);
        String bhost_ip = s[0];
        String bhost_port = s[1];

        String invoke_request_header = "INVOKEREQUESTHEADER-FROM-" + Initialiser.getLocalIpPort() + "-TO-" + Helpers.getStringRepresentationOfIpPort(bhost_ip.trim(), Integer.parseInt(bhost_port.trim()));

        String invoke_request_body = "INVOKEREQUESTBODY-";
        Document remoteCallEncoding = KernelUtil.encodeRemoteCallAsxml(bhost_ip, new Integer(bhost_port.trim()), bundleId, contractName, parameters);
        invoke_request_body = invoke_request_body + KernelUtil.getMetadataAndBDString(KernelUtil.getBDString(remoteCallEncoding), meta);
        String invokerMessage = invoke_request_header + invoke_request_body;
        new SenderImpl().sendMessage(bhost_ip, new Integer(bhost_port.trim()), invokerMessage);
    }
}
