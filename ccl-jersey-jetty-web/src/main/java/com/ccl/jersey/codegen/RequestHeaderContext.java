package com.ccl.jersey.codegen;

import com.google.common.collect.Maps;

import java.util.Map;
import java.util.Objects;

/**
 * Created by ccl on 2015/8/2.
 */
public class RequestHeaderContext {
    public static final String REQUEST_ID = "requestId";
    public static final String ACCESS_TOKEN = "accessToken";
    public static final String LANG = "lang";

    private static final ThreadLocal<Map<String, String>> headerContext = new ThreadLocal<>();

    public static void setRequestHeaders(Map<String, String> requestHeaders) {
        headerContext.set(requestHeaders);
    }

    public static Map<String, String> getRequestHeaders() {
        Map<String, String> map = headerContext.get();
        if (Objects.isNull(map)){
            map= Maps.newConcurrentMap();
            setRequestHeaders(map);
        }
        return map;
    }
}
