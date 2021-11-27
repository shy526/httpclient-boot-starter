package com.github.shy526.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.message.BasicNameValuePair;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * request 包装
 *
 * @author Administrator
 */
@Slf4j
public class RequestPack {
    private static final String DEFAULT_ENCODING = "UTF-8";

    private final HttpRequestBase requestBase;

    public HttpRequestBase getRequestBase() {
        return requestBase;
    }

    private static boolean mapIsEmpty(Map<?, ?> map) {
        return map == null || map.isEmpty();
    }

    public RequestPack(HttpRequestBase requestBase) {
        this.requestBase = requestBase;
    }

    public RequestPack setHeader(Map<String, String> header) {
        if (mapIsEmpty(header)) {
            return this;
        }
        header.forEach(requestBase::setHeader);
        return this;
    }

    /**
     * 生成表单
     *
     * @param format 参数
     * @param encode 字符编码
     * @return RequestPack
     */
    public RequestPack setFormat(Map<String, String> format, String encode) {
        if (mapIsEmpty(format)) {
            return this;
        }
        if (!(requestBase instanceof HttpEntityEnclosingRequestBase)) {
            log.error("request type error");
            return this;
        }
        if (encode == null || "".equals(encode.trim())) {
            encode = DEFAULT_ENCODING;
        }
        List<NameValuePair> parameters = new ArrayList<>(format.size());
        format.forEach((k, v) -> parameters.add(new BasicNameValuePair(k, v)));

        try {
            ((HttpEntityEnclosingRequestBase) requestBase).setEntity(new UrlEncodedFormEntity(parameters, encode));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return this;
    }

    public RequestPack setRequestConfig(RequestConfig requestConfig) {
        requestBase.setConfig(RequestConfig.copy(requestConfig).build());
        return this;
    }

    /**
     * url参数贬值
     *
     * @param url    url
     * @param params params
     * @return String
     */
    public static String buildUrlParams(String url, Map<String, String> params) {
        if (mapIsEmpty(params)) {
            return url;
        }
        URIBuilder builder;
        try {
            builder = new URIBuilder(url);
            params.forEach(builder::setParameter);
            url = builder.build().toString();
        } catch (URISyntaxException e) {
            log.error(e.getMessage(), e);
        }
        return url;
    }


    /**
     * 生成使用代理的设置代理
     *
     * @param hostName      代理主机名
     * @param port          代理端口
     * @param requestConfig requestConfig null 时使用requestBase.getConfig()
     * @return Message
     */
    public RequestPack setProxy(String hostName, Integer port, RequestConfig requestConfig) {
        HttpHost proxy = new HttpHost(hostName, port);
        RequestConfig.Builder configBuilder = null;
        if (requestConfig == null) {
            configBuilder = RequestConfig.copy(requestBase.getConfig());
        } else {
            configBuilder = RequestConfig.copy(requestConfig);
        }
        requestBase.setConfig(configBuilder.setProxy(proxy).build());
        return this;
    }

    /**
     * 生产RequestPack
     *
     * @param url    url
     * @param params params
     * @param tclass tclass
     * @return RequestPack
     */
    public static RequestPack produce(String url, Map<String, String> params, Class<? extends HttpRequestBase> tclass) {
        HttpRequestBase httpRequestBase = null;
        try {
            Constructor<? extends HttpRequestBase> constructor = tclass.getConstructor(String.class);
            httpRequestBase = constructor.newInstance(buildUrlParams(url, params));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return new RequestPack(httpRequestBase);
    }
}
