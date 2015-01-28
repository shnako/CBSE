package org.gla.mcom.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

public class Helpers {
    public static String setToString(Set<String> set) {
        String result = "";

        for (String element : set) {
            result += element + Parameters.ITEM_SEPARATOR;
        }

        if (result.isEmpty()) return "";
        return result.substring(0, result.length() - 1);
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
}

