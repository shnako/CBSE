package mcom.wire;

public interface Sender {

    @SuppressWarnings("UnusedDeclaration")
    public boolean makeConnection();

    @SuppressWarnings("UnusedDeclaration")
    public void sendMessage(String message);

    public boolean sendMessage(String ip, int port, String message, boolean expectResponse);
}
