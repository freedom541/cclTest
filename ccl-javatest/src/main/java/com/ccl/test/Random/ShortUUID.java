package com.ccl.test.Random;

import org.joda.time.DateTime;

import java.util.UUID;

/**
 * Created by ccl on 16/8/7.
 */
public class ShortUUID {
    private static String[] chars = new String[] { "a", "b", "c", "d", "e", "f",
            "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s",
            "t", "u", "v", "w", "x", "y", "z", "0", "1", "2", "3", "4", "5",
            "6", "7", "8", "9", "A", "B", "C", "D", "E", "F", "G", "H", "I",
            "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V",
            "W", "X", "Y", "Z" };

    public static String getShortUuid() {
        StringBuffer shortBuffer = new StringBuffer();
        String uuid = UUID.randomUUID().toString().replace("-", "");
        for (int i = 0; i < 8; i++) {
            String str = uuid.substring(i * 4, i * 4 + 4);
            int x = Integer.parseInt(str, 16);
            shortBuffer.append(chars[x % 0x3E]);
        }
        return shortBuffer.toString();

    }
    public static String getMiniUuid() {
        StringBuffer shortBuffer = new StringBuffer();
        String uuid = UUID.randomUUID().toString().replace("-", "");
        for (int i = 0; i < 6; i++) {
            String str = uuid.substring(i * 4, i * 4 + 4);
            int x = Integer.parseInt(str, 16);
            shortBuffer.append(chars[x % 0x3E]);
        }
        return shortBuffer.toString();

    }
    public static String getUuidCode(int count) {
        StringBuffer shortBuffer = new StringBuffer();
        String uuid = UUID.randomUUID().toString().replace("-", "");
        for (int i = 0; i < count; i++) {
            String str = uuid.substring(i * 4, i * 4 + 4);
            int x = Integer.parseInt(str, 16);
            shortBuffer.append(chars[x % 0x3E]);
        }
        return shortBuffer.toString();

    }

    public static String getRandomCode(int count) {
        StringBuffer shortBuffer = new StringBuffer();
        DateTime time = new DateTime();
        String uuid = String.valueOf(time.getMillis());
        int l = (uuid.length()-6)/6;
        for (int i = 0; i < count; i++) {
            String str = uuid.substring(i * l, i * l + 4);
            int x = Integer.parseInt(str, 16);
            shortBuffer.append(chars[x % 0x3E]);
        }
        return shortBuffer.toString();

    }

    public static void main(String[] args) {
//        System.out.println(ShortUUID.getShortUuid());
//        System.out.println(ShortUUID.getMiniUuid());
//        System.out.println(ShortUUID.getRandomCode(4));
//        DateTime time = new DateTime();
//        System.out.println(time.getMillis());
//        System.out.println(time.hashCode());
//
//        System.out.println(getUuidCode(4)+getRandomCode(4));

        for (int i = 0; i< 100; i++){

        }
    }
}
