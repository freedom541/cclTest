package com.ccl.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;

/**
 * Created by ccl on 16/11/24.
 */
public class Test {
    @org.junit.Test
    public void test(){
        String str = "https://http//1234567890";
        System.out.println(str);
        str = str.replace("https://http//","http://");
        System.out.println(str);


        LinkedHashMap<String,Object> map = new LinkedHashMap<>();
        map.put("key1","wang");
        map.put("key2",1.1);
        map.put("key3","wangf");
        map.put("key4","3");
        map.put("key5",5);

        System.out.println(map.get("key1"));
        System.out.println(map.get("key2"));
        System.out.println(map.get("key5"));
        System.out.println(map.get("key6"));
    }
    @org.junit.Test
    public void read() throws IOException {
//        while (true){
//            System.out.println("INPUT: ");
//            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
//            int k = Integer.parseInt(in.readLine());
//            if (k == 0){
//                break;
//            }else {
//                System.out.println("Your put " + k);
//            }
//        }
    }

    public static void main(String args[]) throws IOException {
        while (true){
            System.out.println("INPUT: ");
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            int k = Integer.parseInt(in.readLine());
            if (k == 0){
                break;
            }else {
                System.out.println("Your put " + k);
            }
        }
    }
}
