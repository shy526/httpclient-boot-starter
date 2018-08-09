package top.ccxh.httpclient.service.task.impl;

import org.apache.http.HttpEntity;
import org.apache.http.util.EntityUtils;
import top.ccxh.httpclient.service.HttpClientService;
import top.ccxh.httpclient.service.task.HttpSucceedTask;

import java.io.IOException;

public class RetunStringTask implements HttpSucceedTask<String> {
    @Override
    public String run(HttpEntity entity) {
        try {
            return EntityUtils.toString(entity, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
