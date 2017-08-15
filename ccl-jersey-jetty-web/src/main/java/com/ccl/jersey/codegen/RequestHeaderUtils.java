package com.ccl.jersey.codegen;

import java.util.Map;

/**
 * Created by ccl on 2015/8/2.
 */
public final class RequestHeaderUtils {

    public static String getRequestId() {
        if (null == getRequestHeaders()) {
            return null;
        }
        return getRequestHeaders().get(RequestHeaderContext.REQUEST_ID);
    }

    public static String getAccessToken() {
        if (null == getRequestHeaders()) {
            return null;
        }
        return getRequestHeaders().get(RequestHeaderContext.ACCESS_TOKEN);
    }

    public static String getLang() {
        if (null == getRequestHeaders()) {
            return null;
        }
        return getRequestHeaders().get(RequestHeaderContext.LANG);
    }

    public static Map<String, String> getRequestHeaders() {
        return RequestHeaderContext.getRequestHeaders();
    }
}
