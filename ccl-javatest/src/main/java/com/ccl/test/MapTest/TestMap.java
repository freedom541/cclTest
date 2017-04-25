package com.ccl.test.MapTest;

import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ccl on 17/3/27.
 */
public class TestMap {
    @Test
    public void mapt(){
        List<String> list = new ArrayList<>();
        list.add("1");
        list.add("2");
        list.add("3");
        list.add("4");
        System.out.println(list);
        for (String str : list){
            if ("1".equals(str)){
                list.remove("1");
                break;
            }
        }
        System.out.println(list);
    }


    @Test
    public void testlist1(){
        List<String> list1 = new ArrayList<String>();
        list1.add("A");
        list1.add("B");

        List<String> list2 = new ArrayList<String>();
        list2.add("C");
        list2.add("B");

        // 2个集合的并集
        list1.addAll(list2);
        System.out.println("并集:" + list1);
    }
    @Test
    public void testlist2(){
        List<String> list1 = new ArrayList<String>();
        list1.add("A");
        list1.add("B");

        List<String> list2 = new ArrayList<String>();
        list2.add("C");
        list2.add("B");

        // 2个集合的并集
        list1.retainAll(list2);
        System.out.println("交集:" + list1);
    }
    @Test
    public void testlist3(){
        List<String> list1 = new ArrayList<String>();
        list1.add("A");
        list1.add("B");

        List<String> list2 = new ArrayList<String>();
        list2.add("C");
        list2.add("B");

        // 2个集合的并集
        System.out.println(list1.removeAll(list2));
        System.out.println("差集:" + list1);
    }

    @Test
    public void map(){
        Map<String,Integer> map = new HashMap<>();
        map.put("two",2);
        map.put("one",1);
        map.put("four",4);
        map.put("three",3);
        map.put("five",5);

        System.out.println(map);
        System.out.println();

        map.entrySet().stream().sorted((k,v)->{
            return  k.getValue()-v.getValue();
        }).forEach(p->{
            System.out.println(p);
        });
    }
    @Test
    public void map2(){
        Map<String,Long> map = new HashMap<>();
        map.put("two",2l);
        map.put("one",1l);
        map.put("four",4l);
        map.put("three",3l);
        map.put("five",5l);

        System.out.println(map);
        System.out.println();

        map.entrySet().stream().sorted((k,v)->{
            return  ((k.getValue()-v.getValue())>0)?-1:1;
        }).forEach(p->{
            if (p.getValue() == 4){
                return;
            }else {
                System.out.println(p);
            }
        });
    }


    @Test
    public void test(){
        long l1 = 827830829056l;
        long l2 = 2992518463488l;
        double l = (double) l1/l2;
        System.out.println(l);
    }
}
