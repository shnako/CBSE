package mcom.wire.util;

// AX3 State implementation.
public final class ServerConnectionDetails {
    private String clientId;
    private int callCounter;
    private ConnectionType connectionType;

    public String getClientIp() {
        return clientId;
    }

    public int getCallCounter() {
        return callCounter;
    }

    public int incrementCallCounter() {
        return ++callCounter;
    }

    public ConnectionType getConnectionType() {
        return connectionType;
    }

    public ServerConnectionDetails(String clientId, ConnectionType connectionType) {
        this.clientId = clientId;
        this.callCounter = 0;
        this.connectionType = connectionType;
    }
}
