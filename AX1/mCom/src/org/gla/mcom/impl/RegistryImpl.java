package org.gla.mcom.impl;

import org.gla.mcom.Registry;

import java.util.HashSet;

public class RegistryImpl implements Registry{
	private static RegistryImpl registrySingleton;
	private HashSet<String> ip_ports;

	private RegistryImpl() {
		ip_ports = new HashSet<String>();

	}

	public static RegistryImpl getRegistry() {
		if (registrySingleton != null) {
			return registrySingleton;
		}

		return new RegistryImpl();
	}

	@Override
	public String[] lookup() {
		return (String[])ip_ports.toArray();
	}

	@Override
	public boolean register(String ip_port) {
		return ip_ports.add(ip_port);
	}

	@Override
	public boolean deregister(String ip_port) {
		return ip_ports.remove(ip_port);
	}
}
