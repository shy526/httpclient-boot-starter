package top.ccxh.httpclient.autoconfigure;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.context.properties.bind.BindResult;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;
import top.ccxh.httpclient.service.HttpClientService;

import java.util.Map;

/**
 * @author admin
 */
public class ExtraHttpRegistrar implements ImportBeanDefinitionRegistrar, EnvironmentAware {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExtraHttpRegistrar.class);
    private HttpClientProperties httpClientProperties;

    @Override
    public void registerBeanDefinitions(AnnotationMetadata annotationMetadata, BeanDefinitionRegistry registry) {
        if (httpClientProperties == null) {
            return;
        }
        Map<String, HttpClientProperties> extraHttpClient = httpClientProperties.getExtraHttpClient();
        for (Map.Entry<String, HttpClientProperties> item : extraHttpClient.entrySet()) {
            HttpClientProperties value = item.getValue();
            HttpClientAutoConfiguration tempAuto = new HttpClientAutoConfiguration(value);
            PoolingHttpClientConnectionManager httpClientConnectionManager = tempAuto.getHttpClientConnectionManager();
            SSLConnectionSocketFactory sslConnectionSocketFactory = tempAuto.getSSLConnectionSocketFactory();
            HttpClientBuilder httpClientBuilder = tempAuto.getHttpClientBuilder(httpClientConnectionManager, sslConnectionSocketFactory);
            CloseableHttpClient closeableHttpClient = tempAuto.getCloseableHttpClient(httpClientBuilder);
            RequestConfig.Builder builder = tempAuto.getBuilder();
            RequestConfig requestConfig = tempAuto.getRequestConfig(builder);
            BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(HttpClientService.class);
            beanDefinitionBuilder.addConstructorArgValue(closeableHttpClient);
            beanDefinitionBuilder.addConstructorArgValue(requestConfig);
            beanDefinitionBuilder.addConstructorArgValue(value.getDefaultHeader());
            registry.registerBeanDefinition(item.getKey(), beanDefinitionBuilder.getBeanDefinition());
            LOGGER.info("HttpClientService-->{}", item.getKey());
        }

    }

    @Override
    public void setEnvironment(Environment environment) {
        BindResult<HttpClientProperties> bind = Binder.get(environment).bind("http.client", HttpClientProperties.class);
        this.httpClientProperties = bind.get();
    }
}
