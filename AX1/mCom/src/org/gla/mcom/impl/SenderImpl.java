/**
 * The Sender makes connection request and send messages to a connected recipient
 */

package org.gla.mcom.impl;

import org.gla.mcom.Sender;
import org.gla.mcom.init.Initialiser;
import org.gla.mcom.util.Display;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class SenderImpl implements Sender {
    private boolean accepted;

    public boolean makeConnection() {
        try {
            System.out.println("initiating tcp connection to " + Initialiser.receiver_ip + ":" + Initialiser.receiver_listening_port + ")");
            String i_message = "ping";
            Socket serverSocket = new Socket(Initialiser.receiver_ip, Initialiser.receiver_listening_port);
            DataInputStream in = new DataInputStream(serverSocket.getInputStream());
            DataOutputStream out = new DataOutputStream(serverSocket.getOutputStream());
            out.writeUTF(i_message); // UTF is a string encoding;

            String r_message = in.readUTF();
            if (r_message.equals("rejected")) {
                accepted = false;
            } else if (r_message.equals("accepted")) {
                accepted = true;
            }
            System.out.println(Display.ansi_normal2.colorize("connection to " + Initialiser.receiver_ip + ":" + Initialiser.receiver_listening_port + " " + r_message));

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

    public String sendMessage(String message, boolean expectResponse) {
        if (accepted) {

            try {
                Socket serverSocket = new Socket(Initialiser.receiver_ip, Initialiser.receiver_listening_port);
                DataOutputStream out = new DataOutputStream(serverSocket.getOutputStream());
                DataInputStream in = new DataInputStream(serverSocket.getInputStream());
                out.writeUTF(message); // UTF is a string encoding;
                if (expectResponse) {
                    try {
                        String response = in.readUTF();
                        System.out.println("Response:\r\n" + response);
                        return response;

                    } catch (EOFException e) {
                        // Suppress.
                    }
                }
                serverSocket.close();
            } catch (UnknownHostException e) {
                System.out.println("Sock:" + e.getMessage());
            } catch (EOFException e) {
                System.out.println("EOF:" + e.getMessage());
            } catch (IOException e) {
                System.out.println("IO:" + e.getMessage());
            }
        } else {
            System.out.println(Display.ansi_error.colorize("ERROR:No message recipient"));
        }

        return null;
    }

    public void broadcastMessage(String message, String[] clients) {
        // Store the previous connection's details.
        String old_ip = Initialiser.receiver_ip;
        int old_port = Initialiser.receiver_listening_port;

        for (String client : clients) {
            int separatorIndex = client.lastIndexOf(":");

            Initialiser.receiver_ip = client.substring(0, separatorIndex);
            Initialiser.receiver_listening_port = Integer.parseInt(client.substring(separatorIndex + 1));
            makeConnection();

            sendMessage(message, false);
        }

        // Reconnect to the previous connection if one was made.
        if (old_port > -1) {
            Initialiser.receiver_ip = old_ip;
            Initialiser.receiver_listening_port = old_port;
            makeConnection();
        }
    }
}