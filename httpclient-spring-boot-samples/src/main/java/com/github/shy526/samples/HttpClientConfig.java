package com.github.shy526.samples;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.github.shy526.autoconfigure.HttpClientFactory;
import com.github.shy526.autoconfigure.HttpClientProperties;
import com.github.shy526.service.HttpClientService;

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
