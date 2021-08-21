package com.github.shy526.samples;


import com.github.shy526.common.HttpResult;
import com.github.shy526.service.HttpClientService;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpRequestBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.HashMap;

/**
 * @author shy526
 */

@SpringBootApplication
@Slf4j
public class SampleMapperApplication implements CommandLineRunner {

    @Autowired
    private HttpClientService httpClientService;

    @Autowired
    @Qualifier("myHttp")
    private HttpClientService httpClientService2;


    public static void main(String[] args) {
        SpringApplication.run(SampleMapperApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        HashMap<String, String> params = new HashMap<>(2);
        params.put("ie", "UTF-8");
        params.put("wd", "111");
        String url = "https://www.baidu.com/s";
        httpClientService.get(url, params);
        HttpRequestBase httpRequestBase = httpClientService.buildGet(url, params, null);
        httpClientService.execute(httpRequestBase);
        String urlP = httpClientService.buildUrlParams(url, params);
        log.info(urlP);
    }


}