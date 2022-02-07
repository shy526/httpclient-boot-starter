package com.github.shy526.autoconfigure;

import com.github.shy526.http.HttpClientProperties;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 *-@ConfigurationProperties(prefix = HttpClientServiceProperties.PREFIX)
 *@author shy526
 */
@Deprecated
public class HttpClientServiceProperties implements Serializable {
    public static final String PREFIX = "http";
    /**
     * httpClient配置
     */
    private Map<String, HttpClientProperties> clientConfig;
    /**
     * 公共请求头
     */
    private Map<String, String> commHeader;

    public Map<String, HttpClientProperties> getClientConfig() {
        return clientConfig;
    }

    public void setClientConfig(Map<String, HttpClientProperties> clientConfig) {
        this.clientConfig = clientConfig;
    }

    public Map<String, String> getCommHeader() {
        return commHeader;
    }

    public void setCommHeader(Map<String, String> commHeader) {
        if (commHeader == null) {
            commHeader = new HashMap<>(20);
        }
        this.commHeader = commHeader;
    }
}
