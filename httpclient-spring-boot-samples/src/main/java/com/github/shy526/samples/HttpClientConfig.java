package com.github.shy526.samples;

import com.github.shy526.http.HttpClientFactory;
import com.github.shy526.http.HttpClientProperties;
import com.github.shy526.http.HttpClientService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 多client配置实例
 * @author shy526
 */
@Configuration
public class HttpClientConfig {

    @Bean("myProperties")
    @ConfigurationProperties(prefix = "my-http")
    public HttpClientProperties geHttpClientProperties() {
        return new HttpClientProperties();
    }
    @Bean("myHttp")
    public HttpClientService getHttpClientService(@Qualifier("myProperties") HttpClientProperties httpClientProperties ) {
        return HttpClientFactory.getHttpClientService(httpClientProperties);
    }
}
