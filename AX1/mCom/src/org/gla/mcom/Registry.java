package org.gla.mcom;

public interface Registry {
	public String[] lookup();//returns a list[ip:port] of advertised recipients
	public boolean register(String ip_port);//registers a recipient ip:port
}
