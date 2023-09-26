package com.github.shy526.samples;


import com.github.shy526.http.HttpClientService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

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
    private HttpClientService myHttp;


    public static void main(String[] args) {
        SpringApplication.run(SampleMapperApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {


    }


}