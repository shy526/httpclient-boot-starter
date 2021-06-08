package top.ccxh.httpclient.autoconfigure;


import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.ccxh.httpclient.tool.ThreadPoolUtils;

import java.util.concurrent.TimeUnit;

/**
 * @author ccxh
 */
public class CloseExpiredConnectionsTask implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(CloseExpiredConnectionsTask.class);
    private final HttpClientConnectionManager manager;
    private final HttpClientProperties.CloseTask closeTask;
    public CloseExpiredConnectionsTask(HttpClientConnectionManager manager,HttpClientProperties.CloseTask closeTask) {
        this.manager = manager;
        this.closeTask=closeTask;
    }

    public static void start(PoolingHttpClientConnectionManager httpClientConnectionManager, HttpClientProperties.CloseTask closeTask) {
        ThreadPoolUtils.getScheduledThreadPoolExecutor(closeTask.getName(),1).scheduleWithFixedDelay(
                new CloseExpiredConnectionsTask(httpClientConnectionManager,closeTask),closeTask.getInitialDelay(),closeTask.getDelay(), TimeUnit.MILLISECONDS);
    }

    @Override
    public void run() {
        //关闭失效的连接
        manager.closeExpiredConnections();
        //不活动的连接
        manager.closeIdleConnections(closeTask.getIdleTime(), TimeUnit.MILLISECONDS);
        LOGGER.debug("{}--->closeTask-{}-{}",closeTask.getName(),closeTask.getInitialDelay(),closeTask.getDelay());
    }
}

