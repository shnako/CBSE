/**
 * Display: UI Console
 */
package org.gla.mcom.util;

import jlibs.core.lang.Ansi;
import org.gla.mcom.Sender;
import org.gla.mcom.impl.*;
import org.gla.mcom.init.Initialiser;

import java.net.InetAddress;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

public class Display {
    public static final Ansi ansi_help = new Ansi(Ansi.Attribute.NORMAL, Ansi.Color.RED, null);
    public static final Ansi ansi_error = new Ansi(Ansi.Attribute.NORMAL, Ansi.Color.RED, null);
    public static final Ansi ansi_normal = new Ansi(Ansi.Attribute.NORMAL, Ansi.Color.RED, null);
    public static final Ansi ansi_header = new Ansi(Ansi.Attribute.NORMAL, Ansi.Color.BLUE, null);
    public static final Ansi ansi_normal2 = new Ansi(Ansi.Attribute.NORMAL, Ansi.Color.GREEN, null);
    public static final Ansi ansi_console = new Ansi(Ansi.Attribute.NORMAL, Ansi.Color.BLUE, null);
    private static Map<String, String> commands = new LinkedHashMap<String, String>();
    private static Sender sender;

    public Display() {
        System.out.println(" ");
        System.out.println(ansi_header.colorize("________________TCPComponent__________________"));
        System.out.println(ansi_header.colorize("     Component message passing via sockets    "));
        System.out.println(ansi_normal2.colorize("running on:" + Initialiser.local_address.getHostAddress()));
        System.out.println(ansi_normal2.colorize("Listening port:" + ReceiverImpl.listenSocket.getLocalPort()));
        System.out.println(ansi_header.colorize("______________________________________________"));
        System.out.println(ansi_normal.colorize("Type ? for help                               "));
        initCommands();
        console();
    }

    private static void initCommands() {//
        commands.put("ipr" + Parameters.COMMAND_SEPARATOR + " <ipr>", "set ip address<hip> of message recipient");
        commands.put("p" + Parameters.COMMAND_SEPARATOR + " <p>", "set listening port<p> of message recipient");
        commands.put("<m>", "send message <m> to recipient");
        commands.put("c" + Parameters.COMMAND_SEPARATOR + " <c>", "check if host with name <c> exist");
        commands.put("end", "terminate");
        commands.put("start", "start acting as a registrar");
        commands.put("stop", "stop acting as a registrar");
        commands.put("reg", "register the host with this registrar");
        commands.put("dereg", "deregister the host with this registrar");
        commands.put("getreg", "get the list of available registrars");
        commands.put("lookup", "retrieve a list of all the hosts registered with this registrar");
        commands.put("bc" + Parameters.COMMAND_SEPARATOR + " <m>", "send message <m> to everyone registered");
        commands.put("all", "show all");
    }

    @SuppressWarnings("resource")
    private static void console() {
        System.out.println(ansi_console.colorize("\ncom(" + Initialiser.local_address.getHostAddress() + ")>>"));
        Scanner scanner = new Scanner(System.in);
        String command = scanner.nextLine();
        execute(command);
    }

    private static void execute(String command) {
        if (command.equals("?")) {
            printCommands();
        } else if (command.contains(Parameters.COMMAND_SEPARATOR)) {
            int delimiterIndex = command.indexOf(Parameters.COMMAND_SEPARATOR);
            if (delimiterIndex == command.length() - 1) {
                System.out.println(ansi_error.colorize("ERROR:Invalid format"));
            } else {
                String operation = command.substring(0, delimiterIndex);
                String value = command.substring(delimiterIndex + 1);
                if (operation.equals("ipr")) {
                    Initialiser.receiver_ip = value;
                } else if (operation.equals("p")) {
                    if (Initialiser.receiver_ip == null) {
                        System.out.println(ansi_error.colorize("ERROR:receiver not set"));
                    } else {
                        if (isNumeric(value)) {
                            //person we are talking to's listening port - where we are sending it
                            Initialiser.receiver_listening_port = new Integer(value);
                            sender = new SenderImpl();
                            boolean connected = sender.makeConnection();
                            if (!connected) {
                                System.out.println(ansi_error.colorize("ERROR:connection failed"));
                            }
                        } else {
                            System.out.println(ansi_error.colorize("ERROR:host port NaN"));
                        }
                    }
                } else if (operation.equals("c")) {
                    InetAddress address = IPResolver.getAddress(value);
                    if (address != null) {
                        System.out.println(ansi_normal2.colorize("check passed"));
                        System.out.println(ansi_normal2.colorize("running on:" + address.getHostAddress()));
                        System.out.println(ansi_normal2.colorize("DNS:" + address.getHostName()));
                    } else {
                        System.out.println(ansi_normal2.colorize("check failed"));
                    }
                } else if (operation.equals("bc")) {
                    try {
                        sender = new SenderImpl();
                        String response = sender.sendMessage("lookup", true);
                        String[] hosts = response.split(Parameters.ITEM_SEPARATOR);
                        sender.broadcastMessage(value, hosts);
                        System.out.println("Successfully broadcast to " + hosts.length + " hosts.");
                    } catch (Exception ex) {
                        System.out.println("Could not broadcast: " + ex.getMessage());
                    }
                }
            }
        }
        // startRegistrar
        else if (command.equals("start")) {
            if (RegistryImpl.startRegistrar()) {
                Registrars.addRegistrar(Initialiser.local_address.getHostAddress() + ":" + ReceiverImpl.listenSocket.getLocalPort());
                sender = new SenderImpl();
                System.out.println("Registrar service started!");
                sender.broadcastMessage("update_registrars" + Parameters.COMMAND_SEPARATOR + Registrars.getStringRepresentation(), getAllInstance());
            } else {
                System.out.println("Could not start registrar service! Is it already started?");
            }
        } else if (command.equals("getreg")) {
            Registrars.initializeRegistrars(sender.sendMessage("getreg", true).split(Parameters.ITEM_SEPARATOR));
            System.out.println("Got registrars:\r\n" + Registrars.getStringRepresentation());
        }
        else if (command.equals("getreg")) {
        	Registrars.initializeRegistrars(sender.sendMessage("getreg", true).split(Parameters.ITEM_SEPARATOR));
        	System.out.println("Got registrars:\r\n"+Registrars.getStringRepresentation());
        }
        // stopRegistrar
        else if (command.equals("stop")) {
            Registrars.removeRegistrar(Initialiser.local_address.getHostAddress() + ":" + ReceiverImpl.listenSocket.getLocalPort());
            sender = new SenderImpl();
            if (RegistryImpl.stopRegistrar()) {
                sender.broadcastMessage("update registrars:" + Registrars.getStringRepresentation(), getAllInstance());
                System.out.println("Registrar service stopped!");
            } else {
                System.out.println("Could not stop registrar service! Is it already stopped?");
            }
        } else if (command.equals("reg") || command.equals("dereg")) {
            command += Parameters.COMMAND_SEPARATOR + Initialiser.local_address.getHostAddress() + ":" + ReceiverImpl.listenSocket.getLocalPort();
            sender.sendMessage(command, true);
        } else if (command.equals("end")) {
            System.exit(0);
        } else if (command.equals("all")) {
            String result = "";
            for (String ip_port : getAllInstance()) {
                result += ip_port + Parameters.ITEM_SEPARATOR;
            }
            System.out.println(result);
        } else if (command.length() > 0) {
            sender.sendMessage(command, true);
        }
        console();
    }

    @SuppressWarnings("rawtypes")
    private static void printCommands() {
        for (Object o : commands.entrySet()) {
            Entry pairs = (Entry) o;
            ansi_help.out("[" + pairs.getKey() + "]: ");
            System.out.println(pairs.getValue());
        }
    }

    private static boolean isNumeric(String str) {
        try {
            //noinspection ResultOfMethodCallIgnored
            Double.parseDouble(str);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    private static String[] getAllInstance() {
        //noinspection unchecked
        HashSet<String> hosts = (HashSet<String>) Registrars.getRegistrars().clone();
        for (String registrar : Registrars.getRegistrars()) {
            String response = sender.sendMessage("lookup", true, registrar);
            if (response != null && !response.isEmpty()) {
            	
                hosts.addAll(Helpers.arrayToArrayList(response.split(Parameters.ITEM_SEPARATOR)));
            }
        }
        return Helpers.setToStringArray(hosts);
    }
}
