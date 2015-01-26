package org.gla.mcom;

public interface Sender {
	public boolean makeConnection();
	public void sendMessage(String message, boolean expectResponse);
}
