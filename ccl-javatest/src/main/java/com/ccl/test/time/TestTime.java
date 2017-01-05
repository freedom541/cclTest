package com.ccl.test.time;

import org.joda.time.DateTime;
import org.junit.Test;

/**
 * Created by ccl on 16/12/14.
 */
public class TestTime {
    @Test
    public void testTime(){
        DateTime dateTime = new DateTime();
        System.out.println(dateTime.getMillis());
        long expiration = dateTime.getMillis() / 1000;
        System.out.println(expiration);

        long rentTime = 1 * 3600;
        expiration = expiration + rentTime;

        System.out.println(expiration);


        DateTime a1 = new DateTime(1486107802000l);
        System.out.println("nide shi jian =" + a1);


        DateTime dd = new DateTime("2016-12-20T05:41:03.000Z");
        System.out.println(dd);
        System.out.println(dd.getMillis()/1000);
    }
}
