package mcom.wire.util;

// AX3 State implementation.
public final class ClientConnectionDetails {
    private int serverConnectionId;
    private ConnectionType connectionType;

    public ClientConnectionDetails(ConnectionType connectionType, int serverConnectionId) {
        this.serverConnectionId = serverConnectionId;
        this.connectionType = connectionType;
    }

    public int getServerConnectionId() {
        return serverConnectionId;
    }

    public ConnectionType getConnectionType() {
        return connectionType;
    }
}
