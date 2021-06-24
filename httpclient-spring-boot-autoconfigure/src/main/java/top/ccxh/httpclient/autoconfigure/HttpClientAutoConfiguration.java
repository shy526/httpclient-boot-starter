package top.ccxh.httpclient.autoconfigure;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import top.ccxh.httpclient.service.HttpClientService;

/**
 * @author ccxh
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
