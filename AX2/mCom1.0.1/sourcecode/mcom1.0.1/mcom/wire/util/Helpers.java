package mcom.wire.util;

// AX3 implementation.
public abstract class Helpers {
    private static final String IP_PORT_SEPARATOR = "__";

    public static String getStringRepresentationOfIpPort(String ip, int port) {
        return ip + IP_PORT_SEPARATOR + port;
    }

    public static String[] splitIpPort(String ipPort) {
        return ipPort.split(IP_PORT_SEPARATOR);
    }

    public static String capitalizeString(String string) {
        return string.substring(0, 1).toUpperCase() + string.substring(1);
    }
}
