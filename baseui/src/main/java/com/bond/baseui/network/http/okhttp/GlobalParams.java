package com.bond.baseui.network.http.okhttp;

import java.util.HashMap;
import java.util.Map;

public class GlobalParams {

    private static final Map<String, String> headers = new HashMap<>();

    private static final Map<String, String> params = new HashMap<>();

    private static String USER_AGENT="";

    public static void putHeader(String key, String value) {
        headers.put(key, value);
    }

    public static void removeHeaders(String... keys) {
        for (String key : keys)
            headers.remove(key);
    }


    public static void removeParams(String... keys) {
        for (String key : keys) {
            params.remove(key);
        }
    }

    public static void putParams(String key, String value) {
        params.put(key, value);
    }

    public static Map<String, String> getHeaders() {
        return headers;
    }

    public static Map<String, String> getParams() {
        return params;
    }

    public static void setUserAgent(String userAgent){
        USER_AGENT=userAgent;
    }
    public static String getUserAgent(){
        return USER_AGENT;
    }
}
