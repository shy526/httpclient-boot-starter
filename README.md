# httpclient-boot-starter

基于httpClient的spring-boot-starter项目

- 用于简化httpClient的配置过程,优化了httpClient代码

- 顺带学习了spring-boot-starter项目的知识

- 支持多个client的配置

- 配置项优化

## 配置项

```yaml
httpClientService:
  #连接池获取请求超时时间
  connectionRequestTimeout: 500
  #请求连接超时时间
  connectTimeout: 5000
  #socket超时时间
  socketTimeout: 30000
  #空闲永久连接检查间隔
  validateAfterInactivity: 2000
  #连接池最大连接数
  maxTotal: 200
  # 默认路由最大连接数
  defaultMaxPerRoute: 100
  #请求头
  header: { "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.116 Safari/537.36" }
  #是否重试
  automaticRetries: false
  closeTask:
    idleTime: 4000
    delay: 4000
```


## 多个HttpClientService 配置

```yaml
httpClientService:
  connectionRequestTimeout: 500
  connectTimeout: 5000
  socketTimeout: 30000
  validateAfterInactivity: 2000
  maxTotal: 200
  defaultMaxPerRoute: 100
  automaticRetries: false
  closeTask:
    idleTime: 4000
    delay: 4000
myHttp:
  connectionRequestTimeout: 1
  connectTimeout: 5000
  socketTimeout: 30000
  validateAfterInactivity: 2000
  maxTotal: 200
  defaultMaxPerRoute: 100
  automaticRetries: false
  closeTask:
    idleTime: 4000
    delay: 4000
```


```java
@Configuration
public class HttpClientConfig {

    @Bean("myProperties")
    @ConfigurationProperties(prefix = "myHttp")
    public HttpClientProperties geHttpClientProperties() {
        return new HttpClientProperties();
    }
    @Bean("myHttp")
    public HttpClientService getHttpClientService(@Qualifier("myProperties") HttpClientProperties httpClientProperties ) {
        return HttpClientFactory.getHttpClientService(httpClientProperties);
    }
}
```