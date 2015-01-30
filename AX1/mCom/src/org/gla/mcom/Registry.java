package org.gla.mcom;

import java.util.HashSet;

public interface Registry {
    public HashSet<String> lookup();//returns a HashSet<[ip:port]> of advertised recipients

    public boolean register(String ip_port);//registers a recipient ip:port

    public boolean deregister(String ip_port);//deregisters a recipient ip:port
}
