package mcom.wire.util;

/**
 * @Author Inah Omoronyia School of Computing Science, University of Glasgow 
 */

import mcom.init.Initialiser;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;

public class DynamicRegistrarDiscovery {

    private static ArrayList<String> activeMComRegistrars; //String - ip:port of active mCom registrars

    public static void addActiveRegistrar(String regip_port) {
        if (!activeMComRegistrars.contains(regip_port)) {
            activeMComRegistrars.add(regip_port);
        }

    }

    public static ArrayList<String> getActiveRegistrars() {
        return activeMComRegistrars;
    }

    public void doDynamicRegistersDiscovery() {
        activeMComRegistrars = new ArrayList<String>();
        Thread t_thread = new Thread(new DynamicRegistrarDiscovery().new DynamicRegistrarDiscoveryRunner());
        t_thread.start();
    }

    class DynamicRegistrarDiscoveryRunner implements Runnable {
        public void run() {
            try {
                InetAddress multicastAddressGroup;

                if (Initialiser.local_address instanceof Inet6Address) {
                    multicastAddressGroup = InetAddress.getByName(RegistrarConstants.MULTICAST_ADDRESS_GROUP_IPV6);
                } else {
                    multicastAddressGroup = InetAddress.getByName(RegistrarConstants.MULTICAST_ADDRESS_GROUP_IPV4);
                }

                int multicastPort = RegistrarConstants.MULTICAST_PORT;

                MulticastSocket socket = new MulticastSocket(multicastPort);

                String msg = "REGPING-" + Initialiser.getLocalIpPort();

                DatagramPacket sentPacket = new DatagramPacket(msg.getBytes(), msg.length());
                sentPacket.setAddress(multicastAddressGroup);
                sentPacket.setPort(multicastPort);
                socket.send(sentPacket);

                socket.close();
            } catch (IOException ioe) {
                System.err.println("drs failed!");
                ioe.printStackTrace();
            }
        }
    }

}
