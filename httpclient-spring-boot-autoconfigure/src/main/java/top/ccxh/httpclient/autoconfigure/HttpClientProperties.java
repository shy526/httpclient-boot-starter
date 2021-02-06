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
     * 连接池获取请求超时时间
     */
    private Integer connectionRequestTimeout = 50000;

    /**
     * socket超时时间
     */
    private Integer socketTimeout = 30000;

    /**
     * 空闲永久连接检查间隔
     */
    private Integer validateAfterInactivity = 2000;

    /**
     * 请求头
     */
    private Map<String, String> header;

    /**
     * 同类型优先
     */
    private Boolean primary=Boolean.FALSE;

    public Boolean getPrimary() {
        return primary;
    }

    public void setPrimary(Boolean primary) {
        this.primary = primary;
    }

    private CloseTask closeTask=new CloseTask();

    public CloseTask getCloseTask() {
        return closeTask;
    }

    public void setCloseTask(CloseTask closeTask) {
        this.closeTask = closeTask;
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


    public Map<String, String> getHeader() {
        return header;
    }

    public void setHeader(Map<String, String> header) {
        if (header == null) {
            header = new HashMap<>(1);
        }
        this.header = header;
    }

    public Integer getValidateAfterInactivity() {
        return validateAfterInactivity;
    }

    public void setValidateAfterInactivity(Integer validateAfterInactivity) {
        this.validateAfterInactivity = validateAfterInactivity;
    }

    public static class CloseTask {

        public CloseTask() {}

        public CloseTask(String name,Integer idleTime, Long initialDelay, Long delay) {
            this.idleTime = idleTime;
            this.initialDelay = initialDelay;
            this.delay = delay;
        }
        private String name="closeTask";

        /**
         * 清理多少毫秒内部活动的链接
         */
        private Integer idleTime=3000;
        /**
         * 第一次延时的时间
         */
        private Long initialDelay=idleTime.longValue();
        /**
         * 之后延时的时间
         */
        private Long delay=idleTime.longValue();

        public Integer getIdleTime() {
            return idleTime;
        }

        public void setIdleTime(Integer idleTime) {
            this.idleTime = idleTime;
        }

        public Long getInitialDelay() {
            return initialDelay;
        }

        public void setInitialDelay(Long initialDelay) {
            this.initialDelay = initialDelay;
        }

        public Long getDelay() {
            return delay;
        }

        public void setDelay(Long delay) {
            this.delay = delay;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return "CloseTask{" +
                    "name='" + name + '\'' +
                    ", idleTime=" + idleTime +
                    ", initialDelay=" + initialDelay +
                    ", delay=" + delay +
                    '}';
        }
    }
}
