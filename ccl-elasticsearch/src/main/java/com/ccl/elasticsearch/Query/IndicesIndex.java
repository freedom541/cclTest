package com.ccl.elasticsearch.Query;

/**
 * Created by ccl on 16/11/16.
 */
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;

import java.io.IOException;
import java.util.Random;

public class IndicesIndex {
    private static final Random r = new Random();
    private static final String pool = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String symbol = ".*&?-+@%";
    public static void main(String[] args) {
        Client client = null;
        try {
            client = EsUtil.getTransportClient();
            System.out.println("number--------");
            indexNumber(client);
            System.out.println("lower--------");
            indexLower(client);
            System.out.println("upper--------");
            indexUpper(client);
            System.out.println("mixed--------");
            indexMixed(client);
            System.out.println("symbol--------");
            indexSymbol(client);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            EsUtil.close(client);
        }
    }

    private static void indexNumber(Client client) {
        for (int i = 0; i < 10; i++) {
            User u = new User();
            String id = 10000 + i + "";
            String id2 = id;
            String name = "张三";
            int age = r.nextInt(100);
            double salary = r.nextDouble() * 10000;
            u.setId(id);
            u.setId2(id2);
            u.setName(name);
            u.setAge(age);
            u.setSalary(salary);
            String json = generateJson(u);
            System.out.println(i + "==data index begin:" + json);
            IndexResponse response = client.prepareIndex("product", "user")
                    .setSource(json).get();
            System.out.println(i + "==data index end:" + response.getId());
        }
    }

    private static void indexLower(Client client) {
        for (int i = 0; i < 10; i++) {
            User u = new User();
            String id = getRandomLower();
            String id2 = id;
            String name = "李四";
            int age = r.nextInt(100);
            double salary = r.nextDouble() * 10000;
            u.setId(id);
            u.setId2(id2);
            u.setName(name);
            u.setAge(age);
            u.setSalary(salary);
            String json = generateJson(u);
            System.out.println(i + "==data index begin:" + json);
            IndexResponse response = client.prepareIndex("product", "user")
                    .setSource(json).get();
            System.out.println(i + "==data index end:" + response.getId());
        }
    }

    private static void indexUpper(Client client) {
        for (int i = 0; i < 10; i++) {
            User u = new User();
            String id = getRandomUpper();
            String id2 = id;
            String name = "王二麻子";
            int age = r.nextInt(100);
            double salary = r.nextDouble() * 10000;
            u.setId(id);
            u.setId2(id2);
            u.setName(name);
            u.setAge(age);
            u.setSalary(salary);
            String json = generateJson(u);
            System.out.println(i + "==data index begin:" + json);
            IndexResponse response = client.prepareIndex("product", "user")
                    .setSource(json).get();
            System.out.println(i + "==data index end:" + response.getId());
        }
    }

    private static void indexMixed(Client client) {
        for (int i = 0; i < 10; i++) {
            User u = new User();
            String id = getRandomMixed();
            String id2 = id;
            String name = "店小二";
            int age = r.nextInt(100);
            double salary = r.nextDouble() * 10000;
            u.setId(id);
            u.setId2(id2);
            u.setName(name);
            u.setAge(age);
            u.setSalary(salary);
            String json = generateJson(u);
            System.out.println(i + "==data index begin:" + json);
            IndexResponse response = client.prepareIndex("product", "user")
                    .setSource(json).get();
            System.out.println(i + "==data index end:" + response.getId());
        }
    }

    private static void indexSymbol(Client client) {
        for (int i = 0; i < 10; i++) {
            User u = new User();
            String id = getRandomSymbol();
            String id2 = id;
            String name = "屠夫";
            int age = r.nextInt(100);
            double salary = r.nextDouble() * 10000;
            u.setId(id);
            u.setId2(id2);
            u.setName(name);
            u.setAge(age);
            u.setSalary(salary);
            String json = generateJson(u);
            System.out.println(i + "==data index begin:" + json);
            IndexResponse response = client.prepareIndex("product", "user")
                    .setSource(json).get();
            System.out.println(i + "==data index end:" + response.getId());
        }
    }

    private static String getRandomLower() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 20; i++) {
            int cidx = r.nextInt(26);
            char c = (char) ('a' + cidx);
            sb.append(c);
        }
        return sb.toString();
    }

    private static String getRandomUpper() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 20; i++) {
            int cidx = r.nextInt(26);
            char c = (char) ('A' + cidx);
            sb.append(c);
        }
        return sb.toString();
    }

    private static String getRandomMixed() {
        StringBuilder sb = new StringBuilder();
        int psize = pool.length();
        for (int i = 0; i < 20; i++) {
            int cidx = r.nextInt(psize);
            char c = pool.charAt(cidx);
            sb.append(c);
        }
        return sb.toString();
    }

    private static String getRandomSymbol() {
        StringBuilder sb = new StringBuilder();
        int psize = pool.length();
        for (int i = 0; i < 5; i++) {
            int cidx = r.nextInt(psize);
            char c = pool.charAt(cidx);
            sb.append(c);
        }
        int cidx2 = r.nextInt(symbol.length());
        char c2 = symbol.charAt(cidx2);
        sb.append(c2);
        for (int i = 0; i < 14; i++) {
            int cidx = r.nextInt(psize);
            char c = pool.charAt(cidx);
            sb.append(c);
        }
        return sb.toString();
    }

    private static String generateJson(User user) {
        String json = "";
        try {
            XContentBuilder contentBuilder = XContentFactory.jsonBuilder()
                    .startObject();
            contentBuilder.field("id", user.getId());
            contentBuilder.field("id2", user.getId2());
            contentBuilder.field("name", user.getName());
            contentBuilder.field("age", user.getAge());
            contentBuilder.field("salary", user.getSalary());
            json = contentBuilder.endObject().string();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return json;
    }
}
