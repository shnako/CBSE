package mcom.wire;

import mcom.wire.util.ConnectionType;

public interface Sender {
	public boolean makeConnection();
	public void sendMessage(String message);
	public boolean sendMessage(String ip, int port, String message);
	public boolean sendMessage(String ip, int port, String message, ConnectionType connectionType);
}
