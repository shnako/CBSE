package mcom.wire.util;

// AX3 State implementation.
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

    public String getFullText() {
        switch (this) {
            case PERSISTENT: return "persistent";
            case STATEFUL: return "stateful";
            case STATELESS: return "stateless";
            default: throw new EnumConstantNotPresentException(getDeclaringClass(), "No functionality implemented for enum " + this.getText());
        }
    }

    public String getText() {
        return this.text;
    }
}
