package org.gla.mcom;

import java.util.HashSet;

public interface Sender {
    public boolean makeConnection();

    public String sendMessage(String message, boolean expectResponse, boolean showErrorMessage);
    public String sendMessage(String message, boolean expectResponse, boolean showErrorMessage, String ip_port);
    public String sendMessage(String message, boolean expectResponse, boolean showErrorMessage, String ip, int port);

    public void broadcastMessage(String message, HashSet<String> clients);
}
