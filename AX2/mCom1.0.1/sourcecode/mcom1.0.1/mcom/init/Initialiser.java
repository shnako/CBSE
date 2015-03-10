/**
 * mComponent Initialiser:
 * (1) initialise local host address
 * (2) start a Receiver to listen to incoming messages
 * (3) creates a display for console interaction
 *
 * @Author Inah Omoronyia School of Computing Science, University of Glasgow  
 */

package mcom.init;

import mcom.console.Display;
import mcom.kernel.processor.BundleDescriptor;
import mcom.kernel.util.KernelConstants;
import mcom.wire.Receiver;
import mcom.wire.impl.ReceiverImpl;
import mcom.wire.util.*;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Initialiser {

    public static String receiver_ip;
    public static int receiver_listening_port = -1;

    public static InetAddress local_address = null;
    public static Receiver receiver;
    public static RegistrarService reg_ser;
    public static DynamicRegistrarDiscovery dynDis;

    public static BundleDescriptor[] bundleDescriptors;
    
    
    public static void main(String args[]) {
        try {
            local_address = IPResolver.getLocalHostLANAddress();
            createBundleDirectory();
            bundleDescriptors = new BundleDescriptor[0];
            startReceiver();
            
            new Display();
            } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public static String getLocalIpPort() {
        return Helpers.getStringRepresentationOfIpPort(Initialiser.local_address.getHostAddress(), ReceiverImpl.listenSocket.getLocalPort());
    }

    private static void startReceiver() {
        receiver = new ReceiverImpl();
        receiver.receiveMessage();
    }


    /*
     * Creates BundleDir - Directory where all mCom bundles are stored
     */
    private static void createBundleDirectory() {
        File bundleDir = new File(KernelConstants.BUNDLEDIR);

        // if the directory does not exist, create it
        if (!bundleDir.exists()) {
            //System.out.println("creating BundleDirectory: " + bundleDir);
            boolean result = false;
            try {
                //noinspection ResultOfMethodCallIgnored
                bundleDir.mkdir();
                result = true;
            } catch (SecurityException se) {
                //handle it
            }
            if (!result) {
                System.err.println("error creating BundleDirectory");
            }
        }
        createBundleDescDirectory();
        createBundlePersistentDirectory();
    }

    private static void createBundleDescDirectory() {
        File bundleDir = new File(KernelConstants.BUNDLEDESCRIPTORDIR);

        // if the directory does not exist, create it
        if (!bundleDir.exists()) {
            //System.out.println("creating Bundle Descriptor Directory: " + bundleDir);
            boolean result = false;
            try {
                //noinspection ResultOfMethodCallIgnored
                bundleDir.mkdir();
                result = true;
            } catch (SecurityException se) {
                //handle it
            }
            if (!result) {
                System.err.println("error creating BundleDescriptorDirectory");
            }
        }
    }

    private static void createBundlePersistentDirectory() {
        File bundlePersistentDir = new File(KernelConstants.BUNDLEPERSISTENTDIR);

        // if the directory does not exist, create it
        if (!bundlePersistentDir.exists()) {
            //System.out.println("creating Bundle Persistent Directory: " + bundleDir);
            boolean result = false;
            try {
                //noinspection ResultOfMethodCallIgnored
                bundlePersistentDir.mkdir();
                result = true;
            } catch (SecurityException se) {
                //handle it
            }
            if (!result) {
                System.err.println("error creating " + KernelConstants.BUNDLEPERSISTENTDIR);
            }
        }
    }

    public static void addBundleDescriptor(BundleDescriptor bd) {
        BundleDescriptor[] bd_temp = new BundleDescriptor[bundleDescriptors.length + 1];
        List<BundleDescriptor> bd_t = new LinkedList<BundleDescriptor>();

        for (BundleDescriptor bdt : bundleDescriptors) {
            if (bdt != null) {
                bd_t.add(bdt);
            }
        }

        int i = 0;
        for (BundleDescriptor bdt : bd_t) {
            bd_temp[i] = bdt;
            i = i + 1;
        }

        bd_temp[i] = bd;
        bundleDescriptors = bd_temp;
    }

}
