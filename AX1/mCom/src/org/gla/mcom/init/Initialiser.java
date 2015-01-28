/**
 * mComponent Initialiser:
 * (1) initialise local host address
 * (2) start a Receiver to listen to incoming messages
 * (3) creates a display for console interaction
 */

package org.gla.mcom.init;

import org.gla.mcom.impl.ReceiverImpl;
import org.gla.mcom.util.Display;
import org.gla.mcom.util.IPResolver;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class Initialiser {

    public static String receiver_ip;
    public static int receiver_listening_port = -1;

    public static InetAddress local_address = null;
    public static ReceiverImpl receiver;

    public static void main(String args[]) {
        try {
            local_address = IPResolver.getLocalHostLANAddress();
            startReceiver();
            new Display();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    private static void startReceiver() {
        receiver = new ReceiverImpl();
        receiver.receiveMessage();
    }
}
