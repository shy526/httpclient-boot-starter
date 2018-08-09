package top.ccxh.httpclient.service.task;

/**
 *非200 状态处理接口
 * @author admin
 */
public interface HttpErrorTask<T>  extends HttpTask {
    /**
     * 处理接口
     * @param httpStatus http枚举类
     * @return
     */
     T run(Integer httpStatus);

}
