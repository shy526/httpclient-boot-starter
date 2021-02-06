package top.ccxh.samples;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import top.ccxh.httpclient.common.HttpResult;
import top.ccxh.httpclient.service.HttpClientService;

/**
 * @author admin
 */
@SpringBootApplication
public class SampleMapperApplication implements CommandLineRunner {

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    @Qualifier("httpClient")
    private HttpClientService httpClientService;

    @Autowired
    @Qualifier("httpClient2")
    private HttpClientService httpClientService2;

    @Autowired
    private HttpClientService httpClientService5;

    @Autowired
    ApplicationContext applicationContext;





    private String s_url = "https://c.y.qq.com/soso/fcgi-bin/client_search_cp?aggr=1&cr=1&flag_qc=0&p=%s&n=%s&w=%s";

    public static void main(String[] args) {
        SpringApplication.run(SampleMapperApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("httpClientService = " + httpClientService);
        System.out.println("httpClientService = " + httpClientService5);
        System.out.println("httpClientService = " + httpClientService2);
    }


}