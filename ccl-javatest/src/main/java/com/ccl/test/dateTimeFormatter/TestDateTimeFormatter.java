package com.ccl.test.dateTimeFormatter;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.junit.Test;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by ccl on 16/12/12.
 */
public class TestDateTimeFormatter {
    @Test
    public void test(){
        DateTime dateTime = new DateTime(DateTimeZone.UTC);
        Map<String, String> time = new LinkedHashMap<>();
        DateTimeFormatter formatter = ISODateTimeFormat.dateTimeNoMillis();
        String queryTime = formatter.print(dateTime);
        String substring = queryTime.substring(0, queryTime.length() - 9);
        time.putIfAbsent(substring + "00:00:00Z", substring + "23:59:59Z");
        System.out.println(time);
    }

    @Test
    public void mapEntity(){
        Map<String, String> time = new LinkedHashMap<>();
        time.put("zhang","zhangsan");
        time.put("lisi","liiii");
        time.put("wang","wangwu");
        Iterator<Map.Entry<String, String>> iterator = time.entrySet().iterator();
        Map.Entry<String, String> result = null;
        while (iterator.hasNext()) {
            result = iterator.next();
        }
    }
}
