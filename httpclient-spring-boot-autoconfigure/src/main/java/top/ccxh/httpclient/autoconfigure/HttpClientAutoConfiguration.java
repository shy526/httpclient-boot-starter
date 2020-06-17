package top.ccxh.httpclient.autoconfigure;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContextBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import top.ccxh.httpclient.service.HttpClientService;

import javax.annotation.PostConstruct;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;


/**
 * @author admin
 */
@ConditionalOnClass
@Configuration
@Import(ExtraHttpRegistrar.class)
@EnableConfigurationProperties(HttpClientProperties.class)
public class HttpClientAutoConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpClientService.class);

    public HttpClientAutoConfiguration() {
    }

    public HttpClientAutoConfiguration(HttpClientProperties httpConfigProperties) {
        this.httpConfigProperties=httpConfigProperties;
    }

    @Autowired
    private HttpClientProperties httpConfigProperties;

    /**
     * 首先实例化一个连接池管理器，设置最大连接数、并发连接数
     *
     * @return PoolingHttpClientConnectionManager
     */
    @Bean("httpClientConnectionManager")
    public PoolingHttpClientConnectionManager getHttpClientConnectionManager() {
        PoolingHttpClientConnectionManager httpClientConnectionManager = new PoolingHttpClientConnectionManager();
        //最大连接数
        httpClientConnectionManager.setMaxTotal(httpConfigProperties.getMaxTotal());
        //并发数
        httpClientConnectionManager.setDefaultMaxPerRoute(httpConfigProperties.getDefaultMaxPerRoute());
        return httpClientConnectionManager;
    }

    /**
     * https 支持
     *
     * @return SSLConnectionSocketFactory
     */
    @Bean("sslconnectionsocketfactory")
    public SSLConnectionSocketFactory getSSLConnectionSocketFactory() {
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
            // HttpClients.custom().setSSLSocketFactory(sslsf).build();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 实例化连接池，设置连接池管理器。
     * 这里需要以参数形式注入上面实例化的连接池管理器
     *
     * @param phccm
     *
     * @return HttpClientBuilder
     */
    @Bean("httpClientBuilder")
    public HttpClientBuilder getHttpClientBuilder(@Qualifier("httpClientConnectionManager") PoolingHttpClientConnectionManager phccm, @Qualifier("sslconnectionsocketfactory") SSLConnectionSocketFactory sslcsf) {

        //HttpClientBuilder中的构造方法被protected修饰，所以这里不能直接使用new来实例化一个HttpClientBuilder，可以使用HttpClientBuilder提供的静态方法create()来获取HttpClientBuilder对象
        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();

        httpClientBuilder.setConnectionManager(phccm);
        httpClientBuilder.setSSLSocketFactory(sslcsf);

        return httpClientBuilder;
    }

    /**
     * 注入连接池，用于获取httpClient
     *
     * @param httpClientBuilder
     *
     * @return CloseableHttpClient
     */
    @Bean("closeableHttpClient")
    public CloseableHttpClient getCloseableHttpClient(@Qualifier("httpClientBuilder") HttpClientBuilder httpClientBuilder) {
        return httpClientBuilder.build();
    }

    /**
     * Builder是RequestConfig的一个内部类
     * 通过RequestConfig的custom方法来获取到一个Builder对象
     * 设置builder的连接信息
     * 这里还可以设置proxy，cookieSpec等属性。有需要的话可以在此设置
     *
     * @return RequestConfig.Builder
     */
    @Bean("builder")
    public RequestConfig.Builder getBuilder() {
        RequestConfig.Builder builder = RequestConfig.custom();
        return builder.setConnectTimeout(httpConfigProperties.getConnectTimeout())
                .setConnectionRequestTimeout(httpConfigProperties.getConnectionRequestTimeout())
                .setSocketTimeout(httpConfigProperties.getSocketTimeout())
                .setStaleConnectionCheckEnabled(httpConfigProperties.getStaleConnectionCheckEnabled());
    }

    /**
     * 使用builder构建一个RequestConfig对象
     *
     * @param builder
     *
     * @return RequestConfig
     */
    @Bean("requestConfig")
    public RequestConfig getRequestConfig(@Qualifier("builder") RequestConfig.Builder builder) {
        return builder.build();
    }

    @Bean("httpClientService")
    public HttpClientService getHttpClientService(@Qualifier("closeableHttpClient") CloseableHttpClient httpClient, @Qualifier("requestConfig") RequestConfig requestConfig) {
        HttpClientService httpClientService = new HttpClientService(httpClient, requestConfig, httpConfigProperties.getDefaultHeader());
        LOGGER.info("HttpClientService-->{}", httpClientService);
        return httpClientService;
    }




}
