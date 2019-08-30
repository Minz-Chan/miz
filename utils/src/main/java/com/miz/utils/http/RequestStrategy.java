package com.miz.utils.http;

import org.apache.http.client.methods.HttpRequestBase;

import java.util.LinkedHashMap;
import java.util.Map;

public interface RequestStrategy {

    HttpRequestBase toHttpRequestBase(String url, Map<String, String> header, LinkedHashMap<String, String> body);

}
