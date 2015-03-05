package mcom.wire.util;
/**
 * @Author Inah Omoronyia School of Computing Science, University of Glasgow 
 */

import mcom.init.Initialiser;
import mcom.wire.impl.ReceiverImpl;
import mcom.wire.impl.SenderImpl;

import java.io.IOException;
import java.net.*;
import java.util.HashMap;
import java.util.Map;

public class RegistrarService {
    public static boolean isRegistrarService = false;
    private static Map<String, String> registedAdverts;//<String:header, String:body>
    private static MulticastSocket socket;
    private static Thread t_thread;

    public void startRegistrarService() {
        isRegistrarService = true;
        registedAdverts = new HashMap<String, String>();
        InetAddress multicastAddressGroup;
        try {
            if (Initialiser.local_address instanceof Inet6Address) {
                multicastAddressGroup = InetAddress.getByName(RegistrarConstants.MULTICAST_ADDRESS_GROUP_IPV6);
            } else {
                multicastAddressGroup = InetAddress.getByName(RegistrarConstants.MULTICAST_ADDRESS_GROUP_IPV4);
            }
            int multicastPort = RegistrarConstants.MULTICAST_PORT;

            socket = new MulticastSocket(multicastPort);
            socket.joinGroup(multicastAddressGroup);

        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        t_thread = new Thread(new RegistrarService().new RegistrarServiceRunner());
        t_thread.start();

        System.out.println("RegistrarService active: " + isRegistrarService);
    }

    public static void addAdvert(String header, String body) {
        if (isRegistrarService) {
            registedAdverts.put(header, body);
        }
    }

    public static void removeAdvert(String header) {
        registedAdverts.remove(header);
    }

    public static Map<String, String> getAdverts() {
        return registedAdverts;
    }

    class RegistrarServiceRunner implements Runnable {
        public void run() {
            try {
                while (isRegistrarService) {
                    byte[] buf = new byte[1000];
                    DatagramPacket receivedPacket = new DatagramPacket(buf, buf.length);

                    socket.receive(receivedPacket);
                    String recStr = new String(receivedPacket.getData()).trim(); //valid format: REGPING

                    System.out.println(recStr.trim());

                    if (recStr.startsWith("REGPING")) {
                        //respond to request
                        String[] res0 = recStr.split("REGPING-");
                        String[] res = Helpers.splitIpPort(res0[1]);
                        String serviceip = res[0].trim();
                        String serviceport = res[1].trim();

                        String resStr = "REGACCEPT-" + Initialiser.getLocalIpPort();
                        new SenderImpl().sendMessage(serviceip, new Integer(serviceport), resStr);
                    }
                }
            } catch (IOException ioe) {
            }
        }
    }

    public void stopRegistrarService() {
        isRegistrarService = false;
        registedAdverts = null;
        t_thread.interrupt();
        socket.close();
        System.out.println("RegistrarService active: " + isRegistrarService);

    }
}
