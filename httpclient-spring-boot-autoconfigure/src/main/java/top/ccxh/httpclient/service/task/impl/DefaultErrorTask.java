package top.ccxh.httpclient.service.task.impl;

import top.ccxh.httpclient.service.task.HttpErrorTask;

public class DefaultErrorTask implements HttpErrorTask<String> {
    @Override
    public String run(Integer httpStatus) {
        if (httpStatus==null){
            return "0000";
        }
        return httpStatus.toString();
    }
}
