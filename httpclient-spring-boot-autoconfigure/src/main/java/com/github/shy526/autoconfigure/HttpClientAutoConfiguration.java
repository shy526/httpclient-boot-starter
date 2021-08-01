package com.github.shy526.autoconfigure;

import com.github.shy526.service.HttpClientService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
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
