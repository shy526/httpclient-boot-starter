package top.ccxh.samples;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import top.ccxh.httpclient.autoconfigure.HttpClientFactory;
import top.ccxh.httpclient.autoconfigure.HttpClientProperties;
import top.ccxh.httpclient.service.HttpClientService;

/**
 * 多client配置实例
 * @author qing
 */
@Configuration
public class HttpClientConfig {

    @Bean("my")
    @ConfigurationProperties(prefix = "http-client-service4")
    public HttpClientProperties geHttpClientProperties() {
        return new HttpClientProperties();
    }
    @Bean("myHttp")
    public HttpClientService getHttpClientService(@Qualifier("my") HttpClientProperties httpClientProperties ) {
        return HttpClientFactory.getHttpClientService(httpClientProperties);
    }
}
