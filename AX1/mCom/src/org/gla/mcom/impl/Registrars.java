package org.gla.mcom.impl;

import java.util.HashSet;

public abstract class Registrars {
    private static HashSet<String> registrars = new HashSet<String>();

    public static HashSet<String> getRegistrars() {
        // Send a cloned object to ensure the integrity of the stored one.
        //noinspection unchecked
        return (HashSet<String>) registrars.clone();
    }

    public static int getRegistrarCount() {
        return registrars.size();
    }

    public static void addRegistrar(String registrar) {
        registrars.add(registrar);
    }

    public static void removeRegistrar(String registrar) {
        registrars.remove(registrar);
    }

    public static String getStringRepresentation() {
        return Helpers.setToString(getRegistrars());
    }

    public static void initializeRegistrars(String[] registrarArray) {
        if (registrarArray.length == 1 && registrarArray[0].equals("")) {
            registrarArray = new String[0];
        }

        Registrars.registrars = new HashSet<String>(registrarArray.length);

        for (String registrar : registrarArray) {
            addRegistrar(registrar);
        }
    }
}
