/**
 * The Sender makes connection request and send messages to a connected recipient
 * @Author Inah Omoronyia School of Computing Science, University of Glasgow 
 */

package mcom.wire.impl;

import mcom.console.Display;
import mcom.init.Initialiser;
import mcom.kernel.util.KernelUtil;
import mcom.wire.Sender;
import mcom.wire.util.*;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class SenderImpl implements Sender {

    public boolean makeConnection() {
        boolean accepted = false;

        try {
            System.out.println("initiating connection to " + Initialiser.receiver_ip + ":" + Initialiser.receiver_listening_port + ")");
            String i_message = "ping";
            Socket serverSocket = new Socket(Initialiser.receiver_ip, Initialiser.receiver_listening_port);
            DataInputStream in = new DataInputStream(serverSocket.getInputStream());
            DataOutputStream out = new DataOutputStream(serverSocket.getOutputStream());
            out.writeUTF(i_message); // UTF is a string encoding;

            String r_message = in.readUTF();
            if (r_message.isEmpty()) {
                if (r_message.equals("rejected")) {
                    accepted = false;
                } else if (r_message.equals("accepted")) {
                    accepted = true;
                }
                System.out.println(Display.ansi_normal2.colorize("connection to " + Helpers.getStringRepresentationOfIpPort(Initialiser.receiver_ip, Initialiser.receiver_listening_port) + " " + r_message));

            }
            serverSocket.close();
        } catch (UnknownHostException e) {
            System.out.println("Sock:" + e.getMessage());
        } catch (EOFException e) {
            System.out.println("EOF:" + e.getMessage());
        } catch (IOException e) {
            System.out.println("IO:" + e.getMessage());
        }

        return accepted;
    }

    public boolean sendMessage(String ip, int port, String message, boolean expectResponse) {
        boolean ack = false;
        try {
            Socket serverSocket = new Socket(ip, port);
            DataInputStream in = new DataInputStream(serverSocket.getInputStream());
            DataOutputStream out = new DataOutputStream(serverSocket.getOutputStream());
            out.writeUTF(message); // UTF is a string encoding;

            if (!expectResponse) {
                serverSocket.close();
                //noinspection ConstantConditions
                return ack;
            }

            String r_message = in.readUTF();
            if (!r_message.isEmpty()) {
                if (r_message.contains("ack")) {
                    ack = true;
                    System.out.println(Display.ansi_normal2.colorize(Helpers.getStringRepresentationOfIpPort(Initialiser.receiver_ip, Initialiser.receiver_listening_port) + " " + r_message));
                } else if (r_message.startsWith("REGACCEPT")) {
                    String[] res = r_message.split("REGACCEPT-");
                    String regip_port = res[1];
                    DynamicRegistrarDiscovery.addActiveRegistrar(regip_port);
                } else if (r_message.startsWith("ADVERTHEADER-")) {

                    String[] res = r_message.split("ADVERTBODY-");
                    String part0 = res[0];
                    String body = res[1];

                    String[] res1 = part0.split("ADVERTHEADER-");
                    String header = res1[1];

                    RegistrarService.addAdvert(header, body);

                } else if (r_message.contains("LOOKUPRESPONSEHEADER-")) {
                    String[] s1 = r_message.split("LOOKUPRESPONSEHEADER-");
                    String[] s2 = s1[1].split("LOOKUPRESPONSEBODY-");
                    String header = s2[0];
                    String body = s2[1];

                    RemoteLookupService.addLookupResult(header, body);
                    System.out.println("Lookup response:");
                    System.out.println("ContractHost: " + header.trim());
                    System.out.println(body.trim());
                } else if (r_message.contains("INVOKERRESPONSEHEADER-")) { //invocation client
                    if (!r_message.contains("INVOKERRESPONSEHEADER-FROM-")) {
                        System.err.println("Access denied: " + r_message);
                    } else {
                        String dr[] = r_message.split("INVOKERRESPONSEHEADER-FROM-");
                        String d1 = dr[1];
                        String d2[] = d1.split("INVOKERESPONSEBODY-");
                        //String fromtoipport = d2[0];
                        String responsebody = d2[1];

                        //print response body
                        System.out.println(KernelUtil.prettyPrint(responsebody));
                    }
                }
            }
        } catch (UnknownHostException e) {
            System.out.println("Sock:" + e.getMessage());
        } catch (EOFException e) {
            System.out.println("EOF:" + e.getMessage());
        } catch (IOException e) {
            System.out.println("IO:" + e.getMessage());
        }

        return ack;
    }

    public void sendMessage(String message) {
        System.out.println(Display.ansi_error.colorize("ERROR:No message recipient"));
    }
}