package com.seele.utils;

public class HexUtil {
    public static String hex(byte[] arr) {
        StringBuffer hexString = new StringBuffer();
        for (int i = 0; i < arr.length; i++) {
            String hex = Integer.toHexString(arr[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}

