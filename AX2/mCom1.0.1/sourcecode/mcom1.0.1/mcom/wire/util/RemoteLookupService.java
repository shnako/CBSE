package mcom.wire.util;
/**
 * @Author Inah Omoronyia School of Computing Science, University of Glasgow 
 */

import mcom.init.Initialiser;
import mcom.wire.impl.SenderImpl;

import java.util.HashMap;
import java.util.Map;

public class RemoteLookupService {
    private static Map<String, String> lookedUpAdverts;//<String:header, String:body>

    public static void doRemoteLookup() {
        lookedUpAdverts = new HashMap<String, String>();

        //lookup on all known Registers
        String message = "LOOKUPADVERTS-" + Initialiser.getLocalIpPort();
        if (DynamicRegistrarDiscovery.getActiveRegistrars() == null || DynamicRegistrarDiscovery.getActiveRegistrars().size() == 0) {
            System.err.println("No known Registrar");
        } else {
            for (String regip_port : DynamicRegistrarDiscovery.getActiveRegistrars()) {
                String[] res = Helpers.splitIpPort(regip_port);
                String serviceip = res[0];
                String serviceport = res[1];
                new SenderImpl().sendMessage(serviceip, new Integer(serviceport), message);
            }
        }
    }

    public static void addLookupResult(String header, String body) {
        lookedUpAdverts.put(header, body);
    }

    public static void removeLookupResult(String header) {
        lookedUpAdverts.remove(header);
    }

    public static Map<String, String> getLookupResults() {
        return lookedUpAdverts;
    }

}
