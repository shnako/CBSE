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

public class ReceiverImpl implements Receiver {
	public static ServerSocket listenSocket;

	public void receiveMessage() {
		Thread server_thread = new Thread(new ReceiverImpl().new ReceiverRunner());
		server_thread.start();
	}

	class ReceiverRunner implements Runnable {
		public void run() {
			listenSocket = IPResolver.configureHostListeningSocket();

			//noinspection InfiniteLoopStatement
			while (true) {
				try {
					Socket clientSocket = listenSocket.accept();
					String clientSocketString = clientSocket.getInetAddress() + ":" + clientSocket.getPort();
					DataInputStream in = new DataInputStream(clientSocket.getInputStream());
					DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());

					String r_message = in.readUTF();

					if (r_message.equals("ping")) { //client checking recipient existence
						boolean accepted = acceptPing(clientSocket);
						String response;
						if (accepted) {
							response = "accepted";
							send(response, out);
							closeConnection(clientSocket);
						} else {
							response = "rejected";
							send(response, out);
							closeConnection(clientSocket);
						}
					} else if (r_message.equals("disconnect")) {
						System.out.println(Display.ansi_normal2.colorize("disconnecting " + clientSocketString));
						send("" + clientSocketString + " disconnected", out);
						closeConnection(clientSocket);
					}
					// lookup implementation
					else if (r_message.equals("lookup")) {
						Registry registry = RegistryImpl.getRegistryInstance();
						if (registry == null) {
							send("This is not a registrar!", out);
						} else {
                            String[] registryHosts = registry.lookup();

							String result = "";

							for (String ip_port : registryHosts) {
								result += ip_port + Parameters.ITEM_SEPARATOR;
							}

							send(result, out);
							System.out.println("Lookup returned " + registryHosts.length + " hosts.");
						}
						closeConnection(clientSocket);
					}
					// getreg implementation
					else if (r_message.equals("getreg")) {
						String result = "";

						for (String ip_port : Registrars.getRegistrars()) {
							result += ip_port + Parameters.ITEM_SEPARATOR;
						}

						send(result, out);
						System.out.println("Returned registrars: " + result);
					} else if (r_message.contains(Parameters.COMMAND_SEPARATOR)) {
						int delimiterIndex = r_message.indexOf(Parameters.COMMAND_SEPARATOR);

						String operation = r_message.substring(0, delimiterIndex);
						String value = "";
						if (delimiterIndex != r_message.length() - 1) {
							value = r_message.substring(delimiterIndex + 1);
						}

						// register implementation
						if (operation.equals("reg")) {
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

							closeConnection(clientSocket);
						}
						// deregister implementation
						else if (operation.equals("dereg")) {
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

							closeConnection(clientSocket);
						}
						else if (operation.equals("update_registrars")) {
							int regCount = parseRegistrars(value);
							System.out.println("Registrars updated. New count: " + regCount);
						}
					} else {
						System.out.println(Display.ansi_normal.colorize("[" + clientSocketString + "]" + r_message));
						closeConnection(clientSocket);
					}
				} catch (EOFException e) {
					System.out.println("EOF:" + e.getMessage());
				} catch (IOException e) {
					System.out.println("IO:" + e.getMessage());
				}

			}
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
	}

}