package org.gla.mcom.impl;

import org.gla.mcom.Registry;

import java.util.HashSet;

public class RegistryImpl implements Registry {
    private static RegistryImpl registrySingleton;
    private HashSet<String> ip_ports;

    private RegistryImpl() {
        ip_ports = new HashSet<String>();

    }

    public static Registry getRegistryInstance() {
        return registrySingleton;
    }

    public static boolean startRegistrar() {
        if (registrySingleton != null) {
            return false;
        }

        registrySingleton = new RegistryImpl();

        return true;
    }

    public static boolean stopRegistrar() {
        if (registrySingleton == null) {
            return false;
        }

        registrySingleton = null;

        return true;
    }


    @SuppressWarnings("UnusedDeclaration")
    public static boolean isRegistrar() {
        return registrySingleton != null;
    }

    @Override
    public String[] lookup() {
        String[] result = new String[ip_ports.size()];

        int i = 0;
        for (String ip_port : ip_ports) {
            result[i++] = ip_port;
        }

        return result;
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
