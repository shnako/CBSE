package mcom.wire.util;

import java.util.HashMap;

public abstract class ServerConnectionManager {
    private static HashMap<Integer, ServerConnectionDetails> connections = new HashMap<Integer, ServerConnectionDetails>();

    public static ServerConnectionDetails useConnection(Integer connectionId) {
        ServerConnectionDetails connectionDetails = connections.get(connectionId);
        connectionDetails.incrementCallCounter();
        return connectionDetails;
    }

    public static void addConection(ServerConnectionDetails connectionDetails) {
        connections.put(connectionDetails.getId(), connectionDetails);
    }
}
