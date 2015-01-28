package org.gla.mcom.impl;

import java.util.HashSet;

public abstract class Registrars {
    private static HashSet<String> registrars = new HashSet<String>();

    public static HashSet<String> getRegistrars() {
        return registrars;
    }

    public static void setRegistrars(HashSet<String> registrars) {
        Registrars.registrars = registrars;
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
        Registrars.registrars = new HashSet<String>(registrarArray.length);

        for (String registrar : registrarArray) {
            addRegistrar(registrar);
        }
    }
}
