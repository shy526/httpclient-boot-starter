package top.ccxh.httpclient.autoconfigure;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;


/**
 * @author ccxh
 */
@ConfigurationProperties(prefix = "http.client")
public class HttpClientProperties {
    private Integer maxTotal=200;

    private Integer defaultMaxPerRoute=100;

    private Integer connectTimeout=30000;


    private Integer connectionRequestTimeout=50000;


    private Integer socketTimeout=30000;


    private boolean staleConnectionCheckEnabled=false;

    private Map<String,String>  defaultHeader;

    public boolean isStaleConnectionCheckEnabled() {
        return staleConnectionCheckEnabled;
    }

    public Map<String, String> getDefaultHeader() {
        return defaultHeader;
    }

    public void setDefaultHeader(Map<String, String> defaultHeader) {
        this.defaultHeader = defaultHeader;
    }

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

    public boolean getStaleConnectionCheckEnabled() {
        return staleConnectionCheckEnabled;
    }

    public void setStaleConnectionCheckEnabled(boolean staleConnectionCheckEnabled) {
        this.staleConnectionCheckEnabled = staleConnectionCheckEnabled;
    }
}
