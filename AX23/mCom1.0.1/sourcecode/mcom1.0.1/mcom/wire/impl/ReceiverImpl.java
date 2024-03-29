/**
 * The Receiver starts a thread on the background that listens to incoming messages
 * and respond to connection request.
 * @Author Inah Omoronyia School of Computing Science, University of Glasgow 
 */

package mcom.wire.impl;

import mcom.console.Display;
import mcom.init.Initialiser;
import mcom.kernel.impl.RemoteMComInvocation;
import mcom.kernel.processor.BundleDescriptor;
import mcom.kernel.processor.BundleDescriptorFactory;
import mcom.kernel.util.KernelUtil;
import mcom.kernel.util.Metadata;
import mcom.wire.Receiver;
import mcom.wire.util.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;

public class ReceiverImpl implements Receiver {
    public static ServerSocket listenSocket;

    public void receiveMessage() {
        Thread server_thread = new Thread(new ReceiverImpl().new ReceiverRunner());
        server_thread.start();
    }
    
    private void authoriseUser(String hostAddress) {
    	
    }

    class ReceiverRunner implements Runnable {

        @SuppressWarnings("rawtypes")
        public void run() {
            listenSocket = IPResolver.configureHostListeningSocket();

            //noinspection InfiniteLoopStatement
            while (true) {
                Socket clientSocket;
                try {
                    clientSocket = listenSocket.accept();
                } catch (Exception ex) {
                    System.err.println("Listen failed: " + ex.getMessage());
                    continue;
                }

                try {
                    if (clientSocket != null) {
                        DataInputStream in = new DataInputStream(clientSocket.getInputStream());
                        DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());

                        String r_message = in.readUTF();

                        if (r_message.equals("ping")) { //client checking recipient existence
                            boolean accepted = acceptPing(clientSocket);
                            String response = accepted ? "accepted" : "rejected";
                            send(response, out);
                        } else if (r_message.startsWith("REGACCEPT")) {
                            System.out.println(Display.ansi_normal.colorize("[" + Helpers.getStringRepresentationOfIpPort("" + clientSocket.getInetAddress(), clientSocket.getPort()) + "]" + r_message));

                            String[] res = r_message.split("REGACCEPT-");

                            String regip_port = res[1];
                            DynamicRegistrarDiscovery.addActiveRegistrar(regip_port);

                            send(Helpers.getStringRepresentationOfIpPort("" + clientSocket.getInetAddress(), clientSocket.getPort()) + " ack", out);
                        } else if (r_message.startsWith("ADVERTHEADER-")) {

                            String[] res = r_message.split("ADVERTBODY-");
                            String part0 = res[0];

                            Metadata meta = KernelUtil.getMetadataFromString(res[1]);
                            res[1] = KernelUtil.stripMetadataFromString(res[1]);

                            String body = res[1];
                            String bodySplit = body.split("<BundleId>")[1].split("</BundleId>")[0];

                            int bundleId = new Integer(bodySplit);

                            String[] res1 = part0.split("ADVERTHEADER-");
                            String header = res1[1];

                            if (meta.getMetadata("advertised").equals("true")) {
                                RegistrarService.addAdvert(header, body);
                            } else if (meta.getMetadata("advertised").equals("false")) {
                                RegistrarService.removeAdvert(header);
                                BundleDescriptorFactory.removeBundleDescriptor(bundleId);
                            }

                            send(Helpers.getStringRepresentationOfIpPort("" + clientSocket.getInetAddress(), clientSocket.getPort()) + " ack", out);
                        } else if (r_message.startsWith("LOOKUPADVERTS-")) {

                            if (RegistrarService.isRegistrarService) {
                                for (Object o : RegistrarService.getAdverts().entrySet()) {
                                    Map.Entry pairs = (Map.Entry) o;
                                    String header = (String) pairs.getKey();
                                    String body = (String) pairs.getValue();
                                    String message = "LOOKUPRESPONSEHEADER-" + header + "LOOKUPRESPONSEBODY-" + body;

                                    send(message, out);
                                }
                            }
                        } else if (r_message.contains("LOOKUPRESPONSEHEADER-")) {
                            String[] s1 = r_message.split("LOOKUPRESPONSEHEADER-");
                            String[] s2 = s1[1].split("LOOKUPRESPONSEBODY-");
                            String header = s2[0];
                            String body = s2[1];

                            RemoteLookupService.addLookupResult(header, body);
                            System.out.println("Lookup response:");
                            System.out.println("ContractHost: " + header.trim());
                            System.out.println(body.trim());
                        
                    } else if (r_message.contains("UPGRADE-ACCESS-LEVEL-")) {
                        String[] s1 = r_message.split("UPGRADE-ACCESS-LEVEL-");
                        String hostAddress = s1[1].trim();            
                        authoriseUser(hostAddress);         
                        System.out.println(hostAddress+" is now an authorized user");
                        send("Authorization granted", out);
                        
                    }else if (r_message.contains("UPDATE-USAGE-COUNTER-")){
                            String[] s1 = r_message.split("UPDATE-USAGE-COUNTER-");
                            int bundleId = new Integer(s1[1].trim());

                           /* for (BundleDescriptor bundle : Initialiser.bundleDescriptors){
                                if (bundle.getBundleId() == bundleId){
                                    bundle.setUsageCounter(bundle.getUsageCounter() + 1);
                                }
                            }*/
                            BundleDescriptor bundle = KernelUtil.loadBundleDescriptor(bundleId);
                            bundle.setUsageCounter(bundle.getUsageCounter() + 1);
                            KernelUtil.storeBundleDescriptor(bundle);
                        }
                        
                        else if (r_message.contains("INVOKEREQUESTHEADER-")) { //invocation server
                            String de[] = r_message.split("INVOKEREQUESTHEADER-FROM-");
                            String d1 = de[1];
                            String d2[] = d1.split("-TO-");
                            String fromipport = d2[0];
                            String f[] = Helpers.splitIpPort(fromipport);
                            String fromip = f[0].trim();
                            String fromport = f[1].trim();
                            String d3 = d2[1];
                            String d4[] = d3.split("INVOKEREQUESTBODY-");
                            String toipport = d4[0];
                            String t[] = Helpers.splitIpPort(toipport);
                            String toip = t[0];
                            String toport = t[1];
                            String invokebody = d4[1];

                            Document inv_doc = KernelUtil.decodeTextToXml(invokebody.trim());

                            Object result = RemoteMComInvocation.executeRemoteCall(inv_doc);

                            Element dresult = inv_doc.createElement("Result");
                            dresult.appendChild(inv_doc.createTextNode("" + result));
                            inv_doc.getLastChild().appendChild(dresult);

                            String response_header = "INVOKERRESPONSEHEADER-FROM-" + Helpers.getStringRepresentationOfIpPort(toip.trim(), Integer.parseInt(toport.trim())) + "-TO-" + Helpers.getStringRepresentationOfIpPort(fromip.trim(), Integer.parseInt(fromport.trim()));

                            String invoke_response_body = "INVOKERESPONSEBODY-";
                            invoke_response_body = invoke_response_body + KernelUtil.getBDString(inv_doc);
                            String invokerMessage = response_header + invoke_response_body;

                            new SenderImpl().sendMessage(fromip, new Integer(fromport), invokerMessage, false);

                        } else if (r_message.contains("INVOKERRESPONSEHEADER-")) { //invocation client
                            String dr[] = r_message.split("INVOKERRESPONSEHEADER-FROM-");
                            String d1 = dr[1];
                            String d2[] = d1.split("INVOKERESPONSEBODY-");
                            String fromtoipport = d2[0];
                            String fromipport = fromtoipport.split("-TO-")[0];
                            String responsebody = d2[1];

                            System.out.println(KernelUtil.prettyPrint(responsebody));

                        } else {
                            System.out.println(Display.ansi_normal.colorize("[" + Helpers.getStringRepresentationOfIpPort("" + clientSocket.getInetAddress(), clientSocket.getPort()) + "]" + r_message));
                        }
                    }
                } catch (EOFException e) {
                    System.out.println("EOF:" + e.getMessage());
                } catch (IOException e) {
                    System.out.println("IO:" + e.getMessage());
                } finally {
                    closeConnection(clientSocket);
                }
            }
        }

        private boolean acceptPing(Socket clientSocket) {
            System.out.println(Display.ansi_normal2.colorize("now connected to " + Helpers.getStringRepresentationOfIpPort("" + clientSocket.getInetAddress(), clientSocket.getPort())));

            return true;
        }

        private void send(String message, DataOutputStream out) {
            if (out != null) {
                try {
                    out.writeUTF(message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                System.out.println(Display.ansi_error.colorize("ERROR:No receiver"));
            }
        }

        private void closeConnection(Socket clientSocket) {
            if (clientSocket != null) {
                try {
                    clientSocket.close();
                } catch (IOException ex) {
                    System.err.println("Closing the client socket failed: " + ex.getMessage());
                }
            }
        }
    }
}