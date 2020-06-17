package top.ccxh.httpclient.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;


/**
 * @author sjq
 */

public class HttpClientProperties implements Serializable {

    /**
     * 连接池最大连接数
     */
    private Integer maxTotal = 200;

    /**
     * 默认路由最大连接数
     */
    private Integer defaultMaxPerRoute = 100;

    /**
     * 请求连接超时时间
     */
    private Integer connectTimeout = 30000;

    /**
     * 设置请求超时时间
     */
    private Integer connectionRequestTimeout = 50000;

    /**
     *  socket超时时间
     */
    private Integer socketTimeout = 30000;


    private Boolean staleConnectionCheckEnabled = Boolean.TRUE;

    private Map<String, String> header;


    public Integer getMaxTotal() {
        return maxTotal;
    }

    public void setMaxTotal(Integer maxTotal) {
        this.maxTotal = maxTotal;
    }

    public Integer getDefaultMaxPerRoute() {
        return defaultMaxPerRoute;
    }

    public void setDefaultMaxPerRoute(Integer defaultMaxPerRoute) {
        this.defaultMaxPerRoute = defaultMaxPerRoute;
    }

    public Integer getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(Integer connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public Integer getConnectionRequestTimeout() {
        return connectionRequestTimeout;
    }

    public void setConnectionRequestTimeout(Integer connectionRequestTimeout) {
        this.connectionRequestTimeout = connectionRequestTimeout;
    }

    public Integer getSocketTimeout() {
        return socketTimeout;
    }

    public void setSocketTimeout(Integer socketTimeout) {
        this.socketTimeout = socketTimeout;
    }

    public Boolean getStaleConnectionCheckEnabled() {
        return staleConnectionCheckEnabled;
    }

    public void setStaleConnectionCheckEnabled(boolean staleConnectionCheckEnabled) {
        this.staleConnectionCheckEnabled = staleConnectionCheckEnabled;
    }


    public Map<String, String> getHeader() {
        return header;
    }

    public void setHeader(Map<String, String> header) {
        if (header == null) {
            header = new HashMap<>(1);
        }
        this.header = header;
    }

    public void setStaleConnectionCheckEnabled(Boolean staleConnectionCheckEnabled) {
        this.staleConnectionCheckEnabled = staleConnectionCheckEnabled;
    }
}
