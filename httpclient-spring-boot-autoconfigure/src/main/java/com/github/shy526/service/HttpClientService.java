package com.github.shy526.service;

import org.apache.http.HttpHost;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.github.shy526.common.HttpResult;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author ccxh
 */
public class HttpClientService {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpClientService.class);
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
     * 执行提交
     *
     * @param httpMethod httpMethod
     * @return HttpResult
     */
    public HttpResult execute(HttpRequestBase httpMethod) {
        return execute(httpMethod, true);
    }

    public HttpResult execute(HttpRequestBase httpMethod, boolean logFlag) {
        HttpResult result = null;
        LOGGER.debug("执行{}请求，URL = {}", httpMethod.getMethod(), httpMethod.getURI());
        try {
            CloseableHttpResponse response = httpClient.execute(httpMethod);
            result = new HttpResult(response);
        } catch (Exception e) {
            if (logFlag) {
                LOGGER.error(e.getMessage(), e);
            }
            result = new HttpResult(e);
        }
        return result;
    }

    /**
     * url参数贬值
     *
     * @param url    url
     * @param params params
     * @return String
     */
    public String buildUrlParams(String url, Map<String, String> params) {
        if (null != params) {
            URIBuilder builder = null;
            try {
                builder = new URIBuilder(url);
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    builder.setParameter(entry.getKey(), entry.getValue());
                }
                url = builder.build().toString();
            } catch (URISyntaxException e) {
                LOGGER.error(e.getMessage(), e);
            }

        }
        return url;
    }


    /**
     * 设置请求头
     *
     * @param header  请求头
     * @param request request
     */
    public HttpRequestBase httpSetHeader(Map<String, String> header, HttpRequestBase request) {
        if (null != defaultHeader) {
            if (null != header) {
                for (Map.Entry<String, String> entry : defaultHeader.entrySet()) {
                    request.setHeader(entry.getKey(), entry.getValue());
                }
            }
        }
        if (null != header) {
            for (Map.Entry<String, String> entry : header.entrySet()) {
                request.setHeader(entry.getKey(), entry.getValue());
            }
        }
        return request;
    }

    /**
     * 生成表单
     *
     * @param format 参数
     * @param encode 字符编码
     * @return StringEntity
     */
    public HttpRequestBase httpSetFormat(Map<String, String> format, String encode, HttpEntityEnclosingRequestBase requestBase) {
        UrlEncodedFormEntity result = null;
        try {
            if (format != null) {
                List<NameValuePair> parameters = new ArrayList<NameValuePair>(0);
                for (Map.Entry<String, String> entry : format.entrySet()) {
                    parameters.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
                }
                if (encode != null) {
                    result = new UrlEncodedFormEntity(parameters, encode);
                } else {
                    result = new UrlEncodedFormEntity(parameters);
                }
            }
            requestBase.setEntity(result);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }

        return requestBase;
    }


    /**
     * 生成使用代理的设置代理
     *
     * @param hostName 代理主机名
     * @param port     代理端口
     * @return Message
     */
    public HttpRequestBase buildProxy(String hostName, Integer port, HttpRequestBase httpRequestBase) {
        HttpHost proxy = new HttpHost(hostName, port);
        RequestConfig requestConfig = RequestConfig.custom().setProxy(proxy).build();
        httpRequestBase.setConfig(requestConfig);
        return httpRequestBase;
    }

    /**
     * 设置 requestConfig
     *
     * @param httpRequestBase 请求
     * @return httpRequestBase
     */
    public HttpRequestBase httpSetRequestConfig(HttpRequestBase httpRequestBase) {
        httpRequestBase.setConfig(RequestConfig.copy(this.requestConfig).build());
        return httpRequestBase;
    }


    public HttpRequestBase buildGet(String url, Map<String, String> params, Map<String, String> header) {
        url = this.buildUrlParams(url, params);
        HttpGet httpGet = new HttpGet(url);
        return httpSetRequestConfig(httpSetHeader(header, httpGet));
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
        return this.execute(buildGet(url, params, header));
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
     * 生成post,并设置请求头和requestConfig
     *
     * @param url    url
     * @param params params
     * @param header header
     * @param format format
     * @param encode encode
     * @return HttpRequestBase
     */
    public HttpRequestBase buildPost(String url, Map<String, String> params, Map<String, String> header, Map<String, String> format, String encode) {
        HttpPost httpPost = new HttpPost(buildUrlParams(url, params));
        return httpSetRequestConfig(httpSetHeader(header, httpSetFormat(format, encode, httpPost)));

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
    public HttpResult post(String url, Map<String, String> params, Map<String, String> header, Map<String, String> format, String encode) {
        return execute(buildPost(url, params, header, format, encode));
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
        return post(url, params, header, null, null);
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
    public HttpResult post(String url, Map<String, String> header, Map<String, String> format, String encode) {
        return post(url, null, header, format, encode);
    }

    /**
     * 执行POST请求
     *
     * @param url url
     * @return HttpResult
     */
    public HttpResult post(String url, Map<String, String> params) {
        return post(url, params, null, null, null);
    }

    /**
     * 执行POST请求
     *
     * @param url url
     * @return HttpResult
     */
    public HttpResult post(String url) {
        return post(url, null, null, null, null);
    }

    /**
     * 生成代理配置
     *
     * @param hostName hostName
     * @param port     port
     * @return RequestConfig
     */
    public RequestConfig buildProxyConfig(String hostName, Integer port) {
        return buildProxyConfig(hostName, port, null);
    }

    /**
     * 生成代理配置
     *
     * @param hostName      hostName
     * @param port          port
     * @param requestConfig requestConfig
     * @return RequestConfig
     */
    public RequestConfig buildProxyConfig(String hostName, Integer port, RequestConfig requestConfig) {
        if (requestConfig == null) {
            requestConfig = this.requestConfig;
        }
        HttpHost proxy = new HttpHost(hostName, port);
        return RequestConfig.copy(requestConfig).setProxy(proxy).build();
    }

    /**
     * 使用代理
     *
     * @param hostName 代理主机名
     * @param port     代理端口
     * @param url      访问的url
     * @param params   url参数
     * @param header   求情头
     * @return requestBase
     */
    public HttpRequestBase buildProxyGet(String hostName, Integer port, String url, Map<String, String> params, Map<String, String> header) {
        HttpRequestBase requestBase = buildGet(url, params, header);
        requestBase.setConfig(buildProxyConfig(hostName, port));
        return requestBase;
    }


    /**
     * 使用代理
     *
     * @param hostName 代理主机名
     * @param port     代理端口
     * @param url      访问的url
     * @param params   url参数
     * @param header   求情头
     * @param format   format
     * @param encode   format编码
     * @return requestBase
     */
    public HttpRequestBase buildProxyPost(String hostName, Integer port, String url, Map<String, String> params,
                                          Map<String, String> header, Map<String, String> format, String encode) {
        HttpRequestBase requestBase = buildPost(url, params, header, format, encode);
        requestBase.setConfig(buildProxyConfig(hostName, port));
        return requestBase;
    }

    /**
     * @param fileUpLoadName         长传文件名称
     * @param file                   file
     * @param multipartEntityBuilder multipartEntityBuilder null时自动创建
     * @return MultipartEntityBuilder
     */
    public MultipartEntityBuilder buildFile(String fileUpLoadName, File file, MultipartEntityBuilder multipartEntityBuilder) {
        if (multipartEntityBuilder != null) {
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
