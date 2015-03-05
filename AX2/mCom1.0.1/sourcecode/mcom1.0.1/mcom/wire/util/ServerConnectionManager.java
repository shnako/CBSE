package mcom.wire.util;

import java.util.HashMap;

// AX3 State implementation.
public final class ServerConnectionManager {
    private static ServerConnectionManager serverConnectionManager;
    private HashMap<Integer, ServerConnectionDetails> connections;
    private int lastConnectionId;

    private ServerConnectionManager() {
        connections = new HashMap<Integer, ServerConnectionDetails>();
    }

    public static ServerConnectionManager getServerConnectionManager() {
        if (serverConnectionManager == null) {
            serverConnectionManager = new ServerConnectionManager();
        }

        return serverConnectionManager;
    }

    public ServerConnectionDetails useConnection(Integer connectionId, String clientIp) throws IllegalAccessException {
        ServerConnectionDetails connectionDetails = connections.get(connectionId);
        if (!connectionDetails.getClientIp().equals(clientIp)) {
            throw new IllegalAccessException("Client with IP " + clientIp + " is not the owner of connection with ID " + connectionId + "!");
        }
        connectionDetails.incrementCallCounter();
        return connectionDetails;
    }

    public int addConnection(ServerConnectionDetails connectionDetails) {
        int connectionId = getNextConnectionId();
        connections.put(connectionId, connectionDetails);
        return connectionId;
    }

    private int getNextConnectionId() {
        return ++lastConnectionId;
    }
}
