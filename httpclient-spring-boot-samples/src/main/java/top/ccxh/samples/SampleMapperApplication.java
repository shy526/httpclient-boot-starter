package top.ccxh.samples;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import top.ccxh.httpclient.common.HttpResult;
import top.ccxh.httpclient.service.HttpClientService;

/**
 *
 * @author admin
 */
@SpringBootApplication
public class SampleMapperApplication implements CommandLineRunner {
   @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
   @Autowired
    private HttpClientService httpClientService;

    public static void main(String[] args) {
        SpringApplication.run(SampleMapperApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("args = " + httpClientService);
    }

}