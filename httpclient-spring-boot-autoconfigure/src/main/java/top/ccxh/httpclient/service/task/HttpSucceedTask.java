package top.ccxh.httpclient.service.task;

import org.apache.http.HttpEntity;

/**
 * 成功后运行
 * @author admin
 */
public interface HttpSucceedTask<T>  extends HttpTask{
    /**
     * 处理接口
     * @param entity entity
     * @return
     */
     T run(HttpEntity entity);

}
