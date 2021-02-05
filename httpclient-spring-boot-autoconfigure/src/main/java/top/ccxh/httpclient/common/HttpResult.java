package top.ccxh.httpclient.common;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.util.StringUtils;

import java.io.Closeable;
import java.io.IOException;

/**
 * http统一返回结果集
 * @author ccxh
 */
public class HttpResult implements Closeable {
    private Integer httpStatus;
    private CloseableHttpResponse response;
    private String entityStr;

    public HttpResult() {
        httpStatus = 0;
    }

    public HttpResult(CloseableHttpResponse response) {

        this.response = response;
        if (this.response != null) {
            this.httpStatus = response.getStatusLine().getStatusCode();
        }
    }

    public Integer getHttpStatus() {
        return httpStatus;
    }

    public void setHttpStatus(Integer httpStatus) {
        this.httpStatus = httpStatus;
    }

    public CloseableHttpResponse getResponse() {
        return response;
    }


    /**
     * 输出字符串 形式
     *
     * @param encode 字符编码
     * @return String
     */
    public String getEntityStr(String encode) {
        HttpEntity entity = null;
        try {
            if (httpStatus.equals(HttpStatus.SC_OK)&&StringUtils.isEmpty(this.entityStr)) {
                entity = this.response.getEntity();
                this.entityStr = EntityUtils.toString(this.response.getEntity(), encode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        consumeHttpEntity(entity);
        return this.entityStr;
    }

    public String getEntityStr() {
        return this.getEntityStr("UTF-8");
    }


    @Override
    protected void finalize() throws Throwable {
        consume();
        super.finalize();

    }

    private void consumeHttpEntity(HttpEntity httpEntity) {
        if (httpEntity != null) {
            try {
                EntityUtils.consume(httpEntity);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                httpEntity = null;
            }
        }
    }

    private void consume() {
        if (response != null) {
            consumeHttpEntity(response.getEntity());
            try {
                response.close();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                response = null;
            }
        }
    }

    @Override
    public void close() throws IOException {
        this.consume();
    }
}
