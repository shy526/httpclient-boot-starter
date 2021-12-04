package com.github.shy526.service;

import com.github.shy526.common.HttpResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Map;

/**
 * HttpClientService 实际实现类
 *
 * @author shy526
 */
@Slf4j
public class HttpClientService {
    private final CloseableHttpClient httpClient;

    private final RequestConfig requestConfig;

    private Map<String, String> defaultHeader;


    public HttpClientService(CloseableHttpClient httpClient, RequestConfig requestConfig) {
        this.httpClient = httpClient;
        this.requestConfig = requestConfig;
    }

    public HttpClientService(CloseableHttpClient httpClient, RequestConfig requestConfig, Map<String, String> defaultHeader) {
        this.httpClient = httpClient;
        this.requestConfig = requestConfig;
        this.defaultHeader = defaultHeader;
    }

    /**
     * get请求
     *
     * @param url    url
     * @param params 参数
     * @param header 请求头
     * @return HttpResult
     */
    public HttpResult get(String url, Map<String, String> params, Map<String, String> header) {
        RequestPack requestPack = RequestPack.produce(url, params, HttpGet.class)
                .setRequestConfig(this.requestConfig).setHeader(defaultHeader).setHeader(header);
        return this.execute(requestPack);
    }

    /**
     * get请求
     *
     * @param url    url
     * @param params 参数
     * @return HttpResult
     */
    public HttpResult get(String url, Map<String, String> params) {
        return this.get(url, params, null);
    }

    /**
     * get请求
     *
     * @param url url
     * @return HttpResult
     */
    public HttpResult get(String url) {
        return this.get(url, null, null);
    }

    /**
     * 执行POST请求
     *
     * @param url    url
     * @param params 表单参数
     * @param header 请求头
     * @param format 表单
     * @param encode 表单编码
     * @return HttpResult
     */
    public HttpResult post(String url, Map<String, String> params, Map<String, String> header,
                           Map<String, String> format, String encode,String bodyStr) {
        RequestPack requestPack = RequestPack.produce(url, params, HttpPost.class).setRequestConfig(requestConfig).setFormat(format, encode)
                .setHeader(defaultHeader).setHeader(header).setBodyStr(bodyStr);
        return execute(requestPack);
    }

    /**
     * 执行POST请求
     *
     * @param url url
     * @return HttpResult
     */
    public HttpResult post(String url) {
        return post(url, null, null, null, null,null);
    }


    /**
     * 执行POST请求
     *
     * @param url url
     * @return HttpResult
     */
    public HttpResult post(String url, Map<String, String> params) {
        return post(url, params, null, null, null,null);
    }

    /**
     * 执行POST请求
     *
     * @param url    url
     * @param params 表单参数
     * @param header 请求头
     * @return HttpResult
     */
    public HttpResult post(String url, Map<String, String> params, Map<String, String> header) {
        return post(url, params, header, null, null,null);
    }

    /**
     * 执行POST请求
     *
     * @param url    url
     * @param header 请求头
     * @param format 表单
     * @param encode 表单编码
     * @return HttpResult
     */
    public HttpResult postFormat(String url, Map<String, String> header, Map<String, String> format, String encode) {
        return post(url, null, header, format, encode,null);
    }


    /**
     * 执行POST请求
     *
     * @param url    url
     * @param format 表单
     * @param encode 表单编码
     * @return HttpResult
     */
    public HttpResult postFormat(String url, Map<String, String> format, String encode) {
        return post(url, null, null, format, encode,null);
    }

    /**
     * 执行POST请求
     *
     * @param url    url
     * @param bodyStr body
     * @return HttpResult
     */
    public HttpResult postStr(String url, String bodyStr) {
        return post(url, null, null, null, null, bodyStr);
    }


    /**
     * 执行POST请求
     *
     * @param url    url
     * @param bodyStr body
     * @return HttpResult
     */
    public HttpResult postStr(String url,  Map<String, String> header,String bodyStr) {
        return post(url, null, header, null, null, bodyStr);
    }

    /**
     * 执行提交
     *
     * @param requestPack requestPack
     * @return HttpResult
     */
    public HttpResult execute(RequestPack requestPack) {
        return execute(requestPack, true);
    }

    /**
     * 执行提交
     *
     * @param requestPack requestPack
     * @param logFlag     logFlag
     * @return HttpResult
     */
    public HttpResult execute(RequestPack requestPack, boolean logFlag) {
        return execute(requestPack.getRequestBase(), logFlag);
    }


    /**
     * 执行提交
     *
     * @param requestBase requestBase
     * @param logFlag     logFlag
     * @return HttpResult
     */
    public HttpResult execute(HttpRequestBase requestBase, boolean logFlag) {
        HttpResult result = null;
        try {
            if (log.isDebugEnabled()) {
                long startTime = System.currentTimeMillis();
                result = new HttpResult(httpClient.execute(requestBase));
                log.debug("{}:{}({}ms)---->{} ", requestBase.getMethod(), result.getHttpStatus(), System.currentTimeMillis() - startTime,
                        requestBase.getURI());
            } else {
                result = new HttpResult(httpClient.execute(requestBase));
            }
        } catch (Exception e) {
            result = new HttpResult(e);
            if (logFlag) {
                log.error(e.getMessage(), e);
            }
        }
        return result;
    }


    /**
     * 上传文件
     *
     * @param fileUpLoadName         长传文件名称
     * @param file                   file
     * @param multipartEntityBuilder multipartEntityBuilder null时自动创建
     * @return MultipartEntityBuilder
     */
    public MultipartEntityBuilder buildFile(String fileUpLoadName, File file, MultipartEntityBuilder multipartEntityBuilder) {
        if (multipartEntityBuilder == null) {
            multipartEntityBuilder = MultipartEntityBuilder.create();
        }
        try {
            multipartEntityBuilder.addBinaryBody(fileUpLoadName, new FileInputStream(file), ContentType.MULTIPART_FORM_DATA, file.getName());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return multipartEntityBuilder;
    }

}
