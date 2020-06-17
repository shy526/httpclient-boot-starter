package top.ccxh.samples;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import top.ccxh.httpclient.common.HttpResult;
import top.ccxh.httpclient.service.HttpClientService;

import javax.annotation.Resource;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.FileReader;

/**
 * @author admin
 */
@SpringBootApplication
public class SampleMapperApplication implements CommandLineRunner {

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private HttpClientService httpClientService;


    @Autowired
    ApplicationContext applicationContext;


 /*   @Autowired
    @Qualifier("testClient")
    @Qualifier("testClient")
    private HttpClientService httpClientService;*/



    private String s_url = "https://c.y.qq.com/soso/fcgi-bin/client_search_cp?aggr=1&cr=1&flag_qc=0&p=%s&n=%s&w=%s";

    public static void main(String[] args) {
        SpringApplication.run(SampleMapperApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("args = " + httpClientService);
        HttpResult httpResult = httpClientService.get(String.format(s_url, 1, 1, "残雪"));
        System.out.println("httpClientService = " + httpClientService);
       // System.out.println("httpClientService = " + httpClientService1);
        System.out.println("httpResult = " + httpResult);
    }


}