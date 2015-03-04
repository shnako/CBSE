package mcom.wire.util;

public enum ConnectionType {
    STATELESS("l"),
    STATEFUL("f"),
    PERSISTENT("p");

    private String text;

    ConnectionType(String text) {
        this.text = text;
    }

    public static ConnectionType fromString(String text) {
        if (text != null) {
            for (ConnectionType reportType : ConnectionType.values()) {
                if (text.equalsIgnoreCase(reportType.text)) {
                    return reportType;
                }
            }
        }
        throw new IllegalArgumentException("No ConnectionType of type " + text + " found!");
    }

    public String getText() {
        return this.text;
    }
}
