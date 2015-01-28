package org.gla.mcom;

public interface Sender {
    public boolean makeConnection();

    public String sendMessage(String message, boolean expectResponse);

    public void broadcastMessage(String message, String[] clients);
}
