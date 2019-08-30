package com.miz.utils.http;

import com.google.common.collect.Lists;
import com.miz.utils.security.KeyStoreType;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContexts;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLHandshakeException;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.security.KeyStore;
import java.util.*;

public class HttpUtils {

    private static final CloseableHttpClient defaultHttpClient = HttpClients.createDefault();
    private static CloseableHttpClient defaultSSLConnectionHttpClient;
    private static SSLConnectionSocketFactory defaultSSLConnectionSocketFactory;
    private static RequestConfig defaultRequestConfig;
    private static final int defaultSocketTimeout = 15000, defaultConnectTimeout = 15000;

    private static HttpClient getHttpClient(SSLConnectionSocketFactory sslConnectionSocketFactory) {
        // 因为项目没有并发场景,CloseableHttpClient可复用
        // 如果SSL连接工厂为空时, 返回默认HttpClient
        if (sslConnectionSocketFactory == null) {
            return defaultHttpClient;
        }
        // 如果SSL连接工厂为默认SSL连接工厂时, 返回默认的SSL连接HttpClient
        if (defaultSSLConnectionSocketFactory != null && defaultSSLConnectionSocketFactory == sslConnectionSocketFactory) {
            if (defaultSSLConnectionHttpClient == null) {
                defaultSSLConnectionHttpClient = HttpClients.custom().setSSLSocketFactory(sslConnectionSocketFactory).build();
            }
            return defaultSSLConnectionHttpClient;
        }
        // SSL连接工厂为自定义时, 新建一个HttpClient
        return HttpClients.custom().setSSLSocketFactory(sslConnectionSocketFactory).build();
    }

    /**
     * 网络请求
     *
     * @param httpRequestBase            请求
     * @param sslConnectionSocketFactory SSL连接Socket工厂
     * @return HttpEntity
     */
    public static HttpEntity request(HttpRequestBase httpRequestBase, SSLConnectionSocketFactory sslConnectionSocketFactory) throws HttpException {
        boolean isHttps = "https".equalsIgnoreCase(httpRequestBase.getURI().getScheme());
        try {
            HttpResponse response;
            if (isHttps) {
                if (sslConnectionSocketFactory == null) {
                    sslConnectionSocketFactory = sslConnSocketFactory();
                }
                response = getHttpClient(sslConnectionSocketFactory).execute(httpRequestBase);
            } else {
                response = defaultHttpClient.execute(httpRequestBase);
            }
            return response.getEntity();
        } catch (SSLHandshakeException e) {
            if (!isHttps) throw new HttpException("请使用https协议");
            throw new HttpException(e.getMessage(), e);
        } catch (Exception e) {
            throw new HttpException(e.getMessage(), e);
        }
    }

    /**
     * 请求配置
     *
     * @param socketTimeout  socket超时
     * @param connectTimeout 连接超时
     * @return RequestConfig 请求配置
     */
    public static RequestConfig getRequestConfig(int socketTimeout, int connectTimeout) {

        if (socketTimeout == defaultSocketTimeout && connectTimeout == defaultConnectTimeout) {
            if (defaultRequestConfig != null) {
                return defaultRequestConfig;
            }
            defaultRequestConfig = RequestConfig.custom()
                    .setSocketTimeout(socketTimeout).setConnectTimeout(connectTimeout).build();
            return defaultRequestConfig;
        }
        return RequestConfig.custom()
                .setSocketTimeout(socketTimeout).setConnectTimeout(connectTimeout).build();
    }

    public static RequestConfig getRequestConfig() {
        return getRequestConfig(15000, 15000);
    }

    /**
     * GET请求
     *
     * @param url url
     * @return HttpEntity 请求响应实体
     */
    public static HttpEntity get(String url) throws HttpException {
        return get(url, getRequestConfig());
    }


    /**
     * GET请求
     *
     * @param url     url
     * @param timeout 超时时间（单位：毫秒）
     * @return 请求响应实体
     * @throws HttpException
     */
    public static HttpEntity get(String url, int timeout) throws HttpException {
        return get(url, getRequestConfig(timeout, timeout));
    }

    /**
     * GET请求
     *
     * @param url    url
     * @param config 配置信息
     * @return HttpEntity 请求响应实体
     */
    public static HttpEntity get(String url, RequestConfig config) throws HttpException {
        url = url.trim();
        try {
            if (isHttps(url)) {
                SSLConnectionSocketFactory socketFactory = sslConnSocketFactory();
                return get(url, null, config, socketFactory);
            } else {
                return get(url, null, config, null);
            }
        } catch (Exception e) {
            throw new HttpException(e.getMessage(), e);
        }
    }

    /**
     * GET请求
     *
     * @param url                        url
     * @param headers                    请求头部
     * @param config                     配置信息
     * @param sslConnectionSocketFactory SSL连接Socket工厂
     * @return HttpEntity 请求响应实体
     */
    public static HttpEntity get(String url, Map<String, String> headers, RequestConfig config, SSLConnectionSocketFactory sslConnectionSocketFactory) throws HttpException {
        try {
            HttpGet httpGet = new HttpGet(url);
            // 设置Http请求头
            if (headers != null && !headers.isEmpty()) {
                headers.forEach(httpGet::addHeader);
            }
            // 配置Http请求
            if (config != null) {
                httpGet.setConfig(config);
            }

            HttpResponse response;
            if (sslConnectionSocketFactory == null || url.startsWith("http:")) {
                response = defaultHttpClient.execute(httpGet);
            } else {
                response = getHttpClient(sslConnectionSocketFactory).execute(httpGet);
            }
            return response.getEntity();
        } catch (SSLHandshakeException e) {
            if (url.startsWith("http:")) throw new HttpException("请使用https协议");
            throw new HttpException(e.getMessage(), e);
        } catch (Exception e) {
            throw new HttpException(e.getMessage(), e);
        }
    }

    /**
     * @param url                        url
     * @param headers                    请求头部
     * @param postEntity                 请求主体
     * @param config                     配置信息
     * @param sslConnectionSocketFactory SSL连接Socket工厂
     * @return HttpEntity 请求响应实体
     */
    public static HttpEntity post(String url, Map<String, String> headers, HttpEntity postEntity, RequestConfig config, SSLConnectionSocketFactory sslConnectionSocketFactory) throws IOException, HttpException {
        try {
            HttpPost httpPost = new HttpPost(url);
            // 设置Http请求头
            if (headers != null && !headers.isEmpty())
                headers.forEach(httpPost::addHeader);
            // 配置Http请求
            if (config != null)
                httpPost.setConfig(config);
            // 请求体
            httpPost.setEntity(postEntity);

            HttpResponse response;
            if (sslConnectionSocketFactory == null || url.startsWith("http:")) {
                response = defaultHttpClient.execute(httpPost);
            } else {
                response = getHttpClient(sslConnectionSocketFactory).execute(httpPost);
            }
            return response.getEntity();
        } catch (SSLHandshakeException e) {
            if (url.startsWith("http:")) throw new HttpException("请使用https协议");
            throw new HttpException(e.getMessage(), e);
        } catch (Exception e) {
            throw new HttpException(e.getMessage(), e);
        }
    }

    /**
     * 获取SSL连接Socket工厂
     *
     * @return SSL连接Socket工厂
     */
    public static SSLConnectionSocketFactory sslConnSocketFactory() throws HttpException {
        if (defaultSSLConnectionSocketFactory != null)
            return defaultSSLConnectionSocketFactory;
        try {
            SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(null, (c, a) -> true).build();
            defaultSSLConnectionSocketFactory = new SSLConnectionSocketFactory(sslContext, (hostname, session) -> true);
            return defaultSSLConnectionSocketFactory;
        } catch (Exception e) {
            throw new HttpException("SSL连接证书异常 -> " + e.getMessage(), e);
        }
    }

    /**
     * 通过密钥和证书的存储和证书密码获取SSL连接Socket工厂
     *
     * @param keyStore 密钥和证书的存储
     * @param password 证书密码
     * @return SSL连接Socket工厂
     */
    public static SSLConnectionSocketFactory sslConnSocketFactoryOf(KeyStore keyStore, char[] password) throws HttpException {
        try {
            SSLContext sslContext = SSLContexts.custom().loadKeyMaterial(keyStore, password).build();
            return new SSLConnectionSocketFactory(sslContext, new String[]{"TLSv1"}, null, (hostname, session) -> true);
        } catch (Exception e) {
            throw new HttpException("SSL连接证书异常 -> " + e.getMessage(), e);
        }
    }

    /**
     * 获取密钥和证书的存储
     *
     * @param filePath     证书文件路径
     * @param keyStoreType 类型 （PKCS12等）
     * @param password     证书密码
     * @return KeyStore 密钥和证书的存储
     */
    public static KeyStore keyStoreOf(String filePath, KeyStoreType keyStoreType, char[] password) throws HttpException {
        try (FileInputStream inputStream = new FileInputStream(filePath)) {
            KeyStore keyStore = KeyStore.getInstance(keyStoreType.name());
            keyStore.load(inputStream, password);
            return keyStore;
        } catch (Exception e) {
            throw new HttpException("SSL连接证书异常 -> " + e.getMessage(), e);
        }
    }

    public static boolean isHttps(String url) {
        return url.toLowerCase().startsWith("https");
    }


    /**
     * 获取SSL连接Socket工厂
     *
     * @return SSL连接Socket工厂
     */
    public static SSLConnectionSocketFactory getSSLConnSocketFactory() throws HttpException {
        try {
            SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(null, (c, a) -> true).build();
            return new SSLConnectionSocketFactory(sslContext, (hostname, session) -> true);
        } catch (Exception e) {
            throw new HttpException(e.getMessage(), e);
        }
    }

    /**
     * 通过密钥和证书的存储和证书密码获取SSL连接Socket工厂
     *
     * @param keyStore 密钥和证书的存储
     * @param password 证书密码
     * @return SSL连接Socket工厂
     */
    public static SSLConnectionSocketFactory getSSLConnSocketFactory(KeyStore keyStore, char[] password) throws HttpException {
        try {
            SSLContext sslContext = SSLContexts.custom().loadKeyMaterial(keyStore, password).build();
            return new SSLConnectionSocketFactory(sslContext, new String[]{"TLSv1"}, null, (hostname, session) -> true);
        } catch (Exception e) {
            throw new HttpException(e.getMessage(), e);
        }
    }

    /**
     * 获取密钥和证书的存储
     *
     * @param filePath 证书文件路径
     * @param type     类型 （PKCS12等）
     * @param password 证书密码
     * @return KeyStore 密钥和证书的存储
     */
    public static KeyStore getKeyStore(String filePath, String type, char[] password) throws HttpException {
        FileInputStream inputStream = null;
        try {
            KeyStore keyStore = KeyStore.getInstance(type);
            inputStream = new FileInputStream(filePath);
            keyStore.load(inputStream, password);
            return keyStore;
        } catch (Exception e) {
            throw new HttpException(e.getMessage(), e);
        } finally {
            try {
                if (inputStream != null) inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * http请求参数
     */
    public interface HttpRequestParams {
        String getUrl();

        Map<String, String> getHeaders();

        RequestConfig getConfig();

        HttpEntity getEntity();
    }

    public static NameValuePair[] entityToNameValuePairs(Object o) throws Exception {
        Class<?> clazz = o.getClass();
        Field[] fields = clazz.getDeclaredFields();
        List<NameValuePair> pairs = Lists.newArrayList();
        Object v;
        for (Field field : fields) {
            if (Modifier.isStatic(field.getModifiers())) continue;
            field.setAccessible(true);
            v = field.get(o);
            if (v == null) continue;
            pairs.add(new BasicNameValuePair(field.getName(), v + ""));
        }
        return pairs.toArray(new NameValuePair[pairs.size()]);
    }

    public static NameValuePair[] mapToNameValuePairs(Map map) throws Exception {
        List<NameValuePair> pairs = Lists.newArrayList();

        Set<String> set = map.keySet();
        Iterator<String> it = set.iterator();
        while (it.hasNext()) {
            String key = it.next();
            pairs.add(new BasicNameValuePair(key, map.get(key).toString()));

        }

        return pairs.toArray(new NameValuePair[pairs.size()]);
    }

    /**
     * 以post方式提交表单数据
     *
     * @param url  url
     * @param pair 键值对
     * @return HttpEntity 请求响应实体
     */
    public static HttpEntity post(String url, NameValuePair... pair) throws HttpException {
        List<NameValuePair> pairs = Arrays.asList(pair);
        try {
            UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(pairs, "UTF-8");
            RequestConfig requestConfig = RequestConfig.custom()
                    .setConnectTimeout(5000)
                    .setConnectionRequestTimeout(1000)
                    .setSocketTimeout(5000).build();
            return post(url, null, requestConfig, formEntity);
        } catch (Exception e) {
            throw new HttpException(e.getMessage(), e);
        }
    }


    /**
     * 以post方式提交表单数据,没有超时设置
     *
     * @param url     url
     * @param pair    键值对
     * @param timeOut 超时时长 , 传 null 为不限制超时时长
     * @return HttpEntity 请求响应实体
     */
    public static HttpEntity postWithTimeOut(String url, Integer timeOut, NameValuePair... pair) throws HttpException {
        List<NameValuePair> pairs = Arrays.asList(pair);
        try {
            UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(pairs, "UTF-8");
            RequestConfig requestConfig = null;
            if (null != timeOut) {
                requestConfig = RequestConfig.custom()
                        .setConnectTimeout(timeOut * 5)
                        .setConnectionRequestTimeout(timeOut)
                        .setSocketTimeout(timeOut * 5).build();
            }
            return post(url, null, requestConfig, formEntity);
        } catch (Exception e) {
            throw new HttpException(e.getMessage(), e);
        }
    }

    /**
     * 以post方式提交表单数据
     *
     * @param url  url
     * @param pair 键值对
     * @return HttpEntity 请求响应实体
     */
    public static HttpEntity post(String url, Map<String, String> headers, NameValuePair... pair) throws HttpException {
        List<NameValuePair> pairs = Arrays.asList(pair);
        try {
            UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(pairs, "UTF-8");
            RequestConfig requestConfig = RequestConfig.custom()
                    .setConnectTimeout(5000)
                    .setConnectionRequestTimeout(1000)
                    .setSocketTimeout(5000).build();
            return post(url, headers, requestConfig, formEntity);
        } catch (Exception e) {
            throw new HttpException(e.getMessage(), e);
        }
    }

    /**
     * 以post方式提交文本数据(带请求头)
     *
     * @param url  url
     * @param data 请求主体
     * @return HttpEntity 请求响应实体
     */
    public static HttpEntity post(String url, String data, Map<String, String> headers) throws HttpException {
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(5000)
                .setConnectionRequestTimeout(1000)
                .setSocketTimeout(5000).build();
        return post(url, headers, requestConfig, new StringEntity(data, "UTF-8"));
    }

    /**
     * 以post方式提交文本数据(带请求头)
     *
     * @param url     url
     * @param data    请求主体
     * @param timeout 超时时间设置, null时为不设置超时时间
     * @return HttpEntity 请求响应实体
     */
    public static HttpEntity postWithTimeOutOption(String url, String data, Map<String, String> headers, Integer timeout) throws HttpException {
        RequestConfig requestConfig = null;
        if (null != timeout) {
            requestConfig = RequestConfig.custom()
                    .setConnectTimeout(timeout * 5)
                    .setConnectionRequestTimeout(timeout)
                    .setSocketTimeout(timeout * 5).build();
        }
        return post(url, headers, requestConfig, new StringEntity(data, "UTF-8"));
    }

    /**
     * 以post方式提交多媒体数据（带header和config）
     *
     * @param url     url
     * @param headers 头部信息
     * @param config  配置信息
     * @param data    请求主体
     * @return HttpEntity 请求响应实体
     */
    public static HttpEntity post(String url, Map<String, String> headers, RequestConfig config, HttpEntity data) throws HttpException {
        return post(url, headers, config, data, null, null, null);
    }

    /**
     * 以post方式提交数据
     *
     * @param url      url
     * @param headers  头部信息
     * @param config   配置信息
     * @param data     请求主体
     * @param filePath 证书路径
     * @param type     证书类型（如：PKCS12 等）
     * @param password 证书密码
     * @return HttpEntity 请求响应实体
     */
    public static HttpEntity post(final String url, final Map<String, String> headers, final RequestConfig config, final HttpEntity data, String filePath, String type, char[] password) throws HttpException {
        return post(new HttpRequestParams() {
            public String getUrl() {
                return url;
            }

            public Map<String, String> getHeaders() {
                return headers;
            }

            public RequestConfig getConfig() {
                return config;
            }

            public HttpEntity getEntity() {
                return data;
            }
        }, filePath, type, password);
    }

    /**
     * 以post方式提交数据
     *
     * @param params   请求单价
     * @param filePath 证书路径
     * @param type     证书类型（如：PKCS12 等）
     * @param password 证书密码
     * @return HttpEntity 请求响应实体
     */
    public static HttpEntity post(HttpRequestParams params, String filePath, String type, char[] password) throws HttpException {
        String url = params.getUrl().trim();
        try {
            SSLConnectionSocketFactory socketFactory = null;
            //SSL 配置
            if (isHttps(url)) {
                if (filePath != null) {
                    // 添加密钥和证书的存储
                    KeyStore keyStore = getKeyStore(filePath, type, password);
                    socketFactory = getSSLConnSocketFactory(keyStore, password);
                } else {
                    socketFactory = getSSLConnSocketFactory();
                }
            }

            HttpClient client = getHttpClient(socketFactory);
            HttpPost post = new HttpPost(url);

            // 添加头部
            Map<String, String> headers = params.getHeaders();
            if (headers != null) {
                Set<String> keys = headers.keySet();
                for (String key : keys) {
                    post.addHeader(key, headers.get(key));
                }
            }
            // 添加配置
            RequestConfig config = params.getConfig();
            if (config != null) post.setConfig(config);

            HttpEntity postEntity = params.getEntity();
            if (postEntity != null) {
                post.setEntity(postEntity);
            }
            HttpResponse response = client.execute(post);
            return response.getEntity();
        } catch (Exception e) {
            throw new HttpException(e.getMessage(), e);
        }
    }


    /**
     * 以post方式提交文本数据(带请求头)
     *
     * @param url url
     * @return HttpEntity 请求响应实体
     */
    public static HttpEntity post(String url, Map<String, String> headers) throws HttpException {
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(5000)
                .setConnectionRequestTimeout(1000)
                .setSocketTimeout(5000).build();
        HttpEntity resp = post(url, headers, requestConfig, null, null, null, null);
        return resp;
    }

}
