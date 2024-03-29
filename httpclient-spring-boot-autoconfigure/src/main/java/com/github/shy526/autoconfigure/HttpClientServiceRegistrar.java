package com.github.shy526.autoconfigure;

import com.github.shy526.http.HttpClientFactory;
import com.github.shy526.http.HttpClientProperties;
import com.github.shy526.http.HttpClientService;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.context.properties.bind.BindResult;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;

import java.util.HashMap;
import java.util.Map;

/**
 * 注册httpClientService
 * @author shy526
 */
@Deprecated
public class HttpClientServiceRegistrar implements ImportBeanDefinitionRegistrar, EnvironmentAware {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpClientServiceRegistrar.class);
    private HttpClientServiceProperties httpClientsProperties;

    @Override
    public void registerBeanDefinitions(AnnotationMetadata annotationMetadata, BeanDefinitionRegistry registry) {
        if (httpClientsProperties == null || httpClientsProperties.getClientConfig() == null) {
            return;
        }
        Map<String, String> commHeader = httpClientsProperties.getCommHeader();
        commHeader = commHeader == null ? new HashMap<>(10) : commHeader;
        Map<String, HttpClientProperties> httpClientConfigMap = httpClientsProperties.getClientConfig();
        for (Map.Entry<String, HttpClientProperties> item : httpClientConfigMap.entrySet()) {
            BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(HttpClientService.class);
            CloseableHttpClient httpClient = HttpClientFactory.getHttpClient(item.getValue());
            beanDefinitionBuilder.addConstructorArgValue(httpClient);
            RequestConfig httpRequestConfig = HttpClientFactory.getRequestConfig(item.getValue());
            beanDefinitionBuilder.addConstructorArgValue(httpRequestConfig);
            Map<String, String> header = item.getValue().getHeader();
            header=header==null?new HashMap<>(10):header;
            header.putAll(commHeader);
            beanDefinitionBuilder.addConstructorArgValue(header);
            AbstractBeanDefinition beanDefinition = beanDefinitionBuilder.getBeanDefinition();
            /*beanDefinition.setPrimary(item.getValue().getPrimary());*/
            registry.registerBeanDefinition(item.getKey(), beanDefinition);
            LOGGER.info("HttpClientService-->{}-->setting:{}", item.getKey(), item.getValue().toString());
        }
    }

    @Override
    public void setEnvironment(Environment environment) {
        BindResult<HttpClientServiceProperties> bind = Binder.get(environment).bind(HttpClientServiceProperties.PREFIX, HttpClientServiceProperties.class);
        this.httpClientsProperties = bind.get();
    }

}
