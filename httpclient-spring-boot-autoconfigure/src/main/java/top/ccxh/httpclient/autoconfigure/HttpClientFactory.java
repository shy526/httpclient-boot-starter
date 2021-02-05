package top.ccxh.httpclient.autoconfigure;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContextBuilder;
import top.ccxh.httpclient.service.HttpClientService;
import top.ccxh.httpclient.tool.ThreadPoolUtils;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author sjq
 */
public class HttpClientFactory {

    public static CloseableHttpClient getHttpClient(HttpClientProperties properties) {
        PoolingHttpClientConnectionManager manager = getHttpClientConnectionManager(properties);
        SSLConnectionSocketFactory sslFactory = getSslConnectionSocketFactory();
        HttpClientBuilder httpBuilder = getHttpClientBuilder(manager, sslFactory);
        return getCloseableHttpClient(httpBuilder);
    }

    public static HttpClientService getHttpClientService(CloseableHttpClient httpClient, RequestConfig requestConfig) {
        return new HttpClientService(httpClient, requestConfig);
    }

    public static HttpClientService getHttpClientService(HttpClientProperties properties) {
        return new HttpClientService(getHttpClient(properties), getRequestConfig(properties), properties.getHeader());
    }

    public static PoolingHttpClientConnectionManager getHttpClientConnectionManager(HttpClientProperties properties) {
        PoolingHttpClientConnectionManager httpClientConnectionManager = new PoolingHttpClientConnectionManager();
        //最大连接数
        httpClientConnectionManager.setMaxTotal(properties.getMaxTotal());
        //并发数
        httpClientConnectionManager.setDefaultMaxPerRoute(properties.getDefaultMaxPerRoute());
        httpClientConnectionManager.setValidateAfterInactivity(properties.getValidateAfterInactivity());
        CloseExpiredConnectionsTask.start(httpClientConnectionManager,properties.getCloseTask());
        return httpClientConnectionManager;
    }

    /**
     * https 支持
     *
     * @return SSLConnectionSocketFactory
     */
    public static SSLConnectionSocketFactory getSslConnectionSocketFactory() {
        try {
            SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {
                // 信任所有
                @Override
                public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                    return true;
                }
            }).build();
            HostnameVerifier hostnameVerifier = NoopHostnameVerifier.INSTANCE;
            return new SSLConnectionSocketFactory(sslContext, hostnameVerifier);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 实例化连接池，设置连接池管理器。
     *
     * @param poolManager poolManager
     * @param sslFactory  sslFactory
     * @return HttpClientBuilder
     */
    public static HttpClientBuilder getHttpClientBuilder(PoolingHttpClientConnectionManager poolManager, SSLConnectionSocketFactory sslFactory) {
        //HttpClientBuilder中的构造方法被protected修饰，所以这里不能直接使用new来实例化一个HttpClientBuilder，
        //可以使用HttpClientBuilder提供的静态方法create()来获取HttpClientBuilder对象
        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
        httpClientBuilder.setConnectionManager(poolManager);
        httpClientBuilder.setSSLSocketFactory(sslFactory);
        return httpClientBuilder;
    }

    /**
     * 注入连接池，用于获取httpClient
     *
     * @param httpClientBuilder httpClientBuilder
     * @return CloseableHttpClient
     */
    public static CloseableHttpClient getCloseableHttpClient(HttpClientBuilder httpClientBuilder) {
        return httpClientBuilder.build();
    }


    /**
     * Builder是RequestConfig的一个内部类
     * 通过RequestConfig的custom方法来获取到一个Builder对象
     * 设置builder的连接信息
     * 这里还可以设置proxy，cookieSpec等属性。有需要的话可以在此设置
     * 使用builder构建一个RequestConfig对象
     *
     * @param httpClientProperties httpClientProperties
     * @return RequestConfig
     */
    public static RequestConfig getRequestConfig(HttpClientProperties httpClientProperties) {
        RequestConfig.Builder builder = RequestConfig.custom();
        return builder.setConnectTimeout(httpClientProperties.getConnectTimeout())
                .setConnectionRequestTimeout(httpClientProperties.getConnectionRequestTimeout())
                .setSocketTimeout(httpClientProperties.getSocketTimeout()).build();
    }
}
