package mcom.wire;

public interface Sender {
	public boolean makeConnection();
	public void sendMessage(String message);
	public boolean sendMessage(String ip, int port, String message);
}
