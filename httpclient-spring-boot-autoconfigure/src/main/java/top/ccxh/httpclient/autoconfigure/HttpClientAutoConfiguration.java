package top.ccxh.httpclient.autoconfigure;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @author ccxh
 */
@ConditionalOnClass
@Configuration
@Import(HttpClientServiceRegistrar.class)
@EnableConfigurationProperties(HttpClientServiceProperties.class)
public class HttpClientAutoConfiguration {
    @Autowired
    private HttpClientServiceProperties httpClientServiceProperties;


}
