package com.github.shy526.autoconfigure;

import com.github.shy526.http.HttpClientFactory;
import com.github.shy526.http.HttpClientProperties;
import com.github.shy526.http.HttpClientService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * httpClientService 自动装配
 * @author shy526
 */
@ConditionalOnClass
@Configuration
public class HttpClientAutoConfiguration {

    @Bean("httpClientProperties")
    @ConfigurationProperties(prefix = HttpClientProperties.PREFIX)
    public HttpClientProperties geHttpClientProperties() {
        return new HttpClientProperties();
    }

    @Bean
    @Primary
    HttpClientService getHttpClientService(@Qualifier("httpClientProperties") HttpClientProperties httpClientProperties) {
        return HttpClientFactory.getHttpClientService(httpClientProperties);
    }

}
