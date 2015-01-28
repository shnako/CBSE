package org.gla.mcom;

public interface Sender {
    public boolean makeConnection();

    public String sendMessage(String message, boolean expectResponse);
    public String sendMessage(String message, boolean expectResponse, String ip_port);
    public String sendMessage(String message, boolean expectResponse, String ip, int port);

    public void broadcastMessage(String message, String[] clients);
}
