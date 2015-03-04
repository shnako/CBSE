package mcom.wire.util;

public class ServerConnectionDetails {
    private int id;
    private int callCounter;
    private ConnectionType connectionType;

    public int getId() {
        return id;
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

    public ServerConnectionDetails(int id, int callCounter, ConnectionType connectionType) {

        this.id = id;
        this.callCounter = callCounter;
        this.connectionType = connectionType;
    }
}
