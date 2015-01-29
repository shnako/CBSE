/**
 * utility class for IP and port configuration
 */
package org.gla.mcom.util;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.util.Enumeration;


public class IPResolver {
    /**
     * Returns an <code>InetAddress</code> object encapsulating what is most likely the machine's LAN IP address.
     *
     * @throws UnknownHostException If the LAN address of the machine cannot be found.
     */
    @SuppressWarnings("rawtypes")
    public static InetAddress getLocalHostLANAddress() throws UnknownHostException {
        try {
            InetAddress candidateAddress = null;
            for (Enumeration ifaces = NetworkInterface.getNetworkInterfaces(); ifaces.hasMoreElements(); ) {
                NetworkInterface iface = (NetworkInterface) ifaces.nextElement();
                for (Enumeration inetAddrs = iface.getInetAddresses(); inetAddrs.hasMoreElements(); ) {
                    InetAddress inetAddr = (InetAddress) inetAddrs.nextElement();
                    if (!inetAddr.isLoopbackAddress()) {

                        if (inetAddr.isSiteLocalAddress()) {
                            return inetAddr;
                        } else if (candidateAddress == null) {
                            candidateAddress = inetAddr;
                        }
                    }
                }
            }
            if (candidateAddress != null) {
                return candidateAddress;
            }
            InetAddress jdkSuppliedAddress = InetAddress.getLocalHost();
            if (jdkSuppliedAddress == null) {
                throw new UnknownHostException("The JDK InetAddress.getLocalHost() method unexpectedly returned null.");
            }
            return jdkSuppliedAddress;
        } catch (Exception e) {
            UnknownHostException unknownHostException = new UnknownHostException("Failed to determine LAN address: " + e);
            unknownHostException.initCause(e);
            throw unknownHostException;
        }
    }

    /**
     * Returns an <code>InetAddress</code>  of a given string representation.
     */
    public static InetAddress getAddress(String address) {
        InetAddress aComputer = null;
        try {
            aComputer = InetAddress.getByName(address);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } finally {
            if (aComputer == null) {
                try {
                    aComputer = InetAddress.getByAddress(address.getBytes());
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                } finally {
                    if (aComputer == null) {
                        System.out.println(Display.ansi_error.colorize("ERROR:unknown address"));
                    }
                }
            }
        }

        return aComputer;
    }

    /**
     * Returns a <code>ServerSocket</code>  listening on a randomly selected free port.
     */
    public static ServerSocket configureHostListeningSocket() {
        ServerSocket s = null;
        try {
            s = new ServerSocket(0); //0 for random free port
        } catch (IOException e) {
            e.printStackTrace();
        }
        return s;
    }
}
