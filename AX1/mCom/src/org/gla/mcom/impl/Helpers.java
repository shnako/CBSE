package org.gla.mcom.impl;

import java.util.*;

public class Helpers {
    public static String setToString(AbstractCollection<String> collection) {
        String result = "";

        for (String element : collection) {
            result += element + Parameters.ITEM_SEPARATOR;
        }

        if (result.isEmpty()) return "";
        return result.substring(0, result.length() - Parameters.ITEM_SEPARATOR.length());
    }
}

