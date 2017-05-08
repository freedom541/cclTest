package com.ccl.http;

import org.junit.Test;

import java.util.HashMap;

/**
 * Created by ccl on 17/5/6.
 */
public class TestClientHelper {
    @Test
    public void testHttpClient(){
        String url = "http://192.168.10.2:8182/resources/request/NormalModule@ping";
        String charSet = "UTF-8";
        String str = HttpClientHelper.httpClientPost(url,new HashMap<String, Object>(),charSet);
        System.out.println(str);
    }
}
