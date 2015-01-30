/**
 * The Receiver starts a thread on the background that listens to incoming messages
 * and respond to connection request.
 */
package org.gla.mcom.impl;

import org.gla.mcom.Receiver;
import org.gla.mcom.Registry;
import org.gla.mcom.util.Display;
import org.gla.mcom.util.IPResolver;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;

public class ReceiverImpl implements Receiver {
    public static ServerSocket listenSocket;

    class ReceiverRunner implements Runnable {
        public void run() {
            listenSocket = IPResolver.configureHostListeningSocket();

            //noinspection InfiniteLoopStatement
            while (true) {
                try {
                    Socket clientSocket = listenSocket.accept();
                    DataInputStream in = new DataInputStream(clientSocket.getInputStream());
                    DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());

                    try {
                        String r_message = in.readUTF();
                        // Process parameterless message.
                        if (r_message.equals("ping")) ping(clientSocket, out);
                        else if (r_message.equals("disconnect")) disconnect(clientSocket, out);
                        else if (r_message.equals("lookup")) lookup(out);
                        else if (r_message.equals("getreg")) getreg(out);
                        else if (r_message.contains(Parameters.COMMAND_SEPARATOR)) {
                            // Split the message into operation and value.
                            int delimiterIndex = r_message.indexOf(Parameters.COMMAND_SEPARATOR);
                            String operation = r_message.substring(0, delimiterIndex);
                            String value = "";
                            if (delimiterIndex != r_message.length() - 1) {
                                value = r_message.substring(delimiterIndex + 1);
                            }

                            // Process message with parameters.
                            if (operation.equals("reg")) reg(out, value);
                            else if (operation.equals("dereg")) dereg(out, value);
                            else if (operation.equals("update_registrars")) update_registrars(value);
                        } else
                            System.out.println(Display.ansi_normal.colorize("[" + getClientSocketString(clientSocket) + "]" + r_message));
                    } finally {
                        closeConnection(clientSocket);
                    }
                } catch (EOFException e) {
                    System.out.println("EOF:" + e.getMessage());
                } catch (IOException e) {
                    System.out.println("IO:" + e.getMessage());
                }
            }
        }

        //region AX1 Implementation
        private void lookup(DataOutputStream out) {
            Registry registry = RegistryImpl.getRegistryInstance();
            if (registry == null) {
                send("This is not a registrar!", out);
            } else {
                HashSet<String> registryHosts = registry.lookup();
                send(Helpers.setToString(registryHosts), out);
                System.out.println("Lookup returned " + registryHosts.size() + " hosts.");
            }
        }

        private void getreg(DataOutputStream out) {
            String result = Registrars.getStringRepresentation();
            send(result, out);
            if (result.equals("")) {
                System.out.println("No registered Registrars");
            } else {
                System.out.println("Returned " + result.split(Parameters.ITEM_SEPARATOR).length + " registrars.");
            }
        }

        private void reg(DataOutputStream out, String value) {
            String message;
            Registry registry = RegistryImpl.getRegistryInstance();

            if (registry == null) {
                message = "This is not a registrar!";
            } else {
                if (registry.register(value)) {
                    message = value + " has been registered";
                } else {
                    message = value + " could not be registered, it is registered already";
                }
            }

            send(message, out);
            System.out.println(message);
        }

        private void dereg(DataOutputStream out, String value) {
            String message;
            Registry registry = RegistryImpl.getRegistryInstance();

            if (registry == null) {
                message = "This is not a registrar!";
            } else {
                if (registry.deregister(value)) {
                    message = value + " has been deregistered";
                } else {
                    message = value + " could not be deregistered, it is not registered yet";
                }
            }

            send(message, out);
            System.out.println(message);
        }

        private void update_registrars(String value) {
            int regCount = parseRegistrars(value);
            System.out.println("Registrars updated. New count: " + regCount);
        }

        private String getClientSocketString(Socket clientSocket) {
            return clientSocket.getInetAddress() + ":" + clientSocket.getPort();
        }
        //endregion

        //region Supplied functionality
        private void ping(Socket clientSocket, DataOutputStream out) {
            boolean accepted = acceptPing(clientSocket);
            String response;
            if (accepted) {
                response = "accepted";
                send(response, out);
            } else {
                response = "rejected";
                send(response, out);
            }
        }

        private void disconnect(Socket clientSocket, DataOutputStream out) {
            System.out.println(Display.ansi_normal2.colorize("disconnecting " + getClientSocketString(clientSocket)));
            send("" + getClientSocketString(clientSocket) + " disconnected", out);
        }

        private boolean acceptPing(Socket clientSocket) { //broadcasting own address
            System.out.println(Display.ansi_normal2.colorize("Connected to " + clientSocket.getInetAddress() + ":" + clientSocket.getPort()));

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
                } catch (IOException e) {
                    /* close failed */
                }
            }
        }

        private int parseRegistrars(String message) {
            String[] registrars = message.split(Parameters.ITEM_SEPARATOR);
            if (registrars.length == 1 && registrars[0].isEmpty()) {
                registrars = new String[0];
            }
            Registrars.initializeRegistrars(registrars);
            return registrars.length;
        }
        //endregion
    }

    public void receiveMessage() {
        Thread server_thread = new Thread(new ReceiverImpl().new ReceiverRunner());
        server_thread.start();
    }
}