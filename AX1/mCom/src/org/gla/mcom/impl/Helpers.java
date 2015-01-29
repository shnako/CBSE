package org.gla.mcom.impl;

import java.util.*;
import java.util.stream.Stream;

public class Helpers {
    public static String setToString(Set<String> set) {
        String result = "";

        for (String element : set) {
            result += element + Parameters.ITEM_SEPARATOR;
        }

        if (result.isEmpty()) return "";
        return result.substring(0, result.length() - Parameters.ITEM_SEPARATOR.length());
    }

    public static ArrayList<String> arrayToArrayList(String[] array) {
        ArrayList<String> result = new ArrayList<String>(array.length);

        Collections.addAll(result, array);

        return result;
    }

    public static String[] setToStringArray(Set<String> set) {
        String[] result = new String[set.size()];
        int i = 0;
        for (String str : set) {
            result[i++] = str;
        }
        return result;
    }

    public static String[] concatStringArrays(String[] a1, String[] a2) {
        HashSet<String> result = new HashSet<String>(a1.length + a2.length);

        result.addAll(arrayToArrayList(a1));
        result.addAll(arrayToArrayList(a2));

        return setToStringArray(result);
    }
}

