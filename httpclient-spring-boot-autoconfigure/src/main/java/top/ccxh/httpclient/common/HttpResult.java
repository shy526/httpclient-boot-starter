package top.ccxh.httpclient.common;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;
import sun.nio.ch.IOUtil;

import java.io.IOException;
import java.io.Serializable;

public class HttpResult implements Serializable {
    private HttpEntity httpEntity;
    private Integer httpStatus;
    private CloseableHttpResponse response;

    public HttpResult() {
        httpStatus = 0;
    }

    public HttpResult(CloseableHttpResponse response) {
        this.setResponse(response);
    }

    public HttpEntity getHttpEntity() {
        return httpEntity;
    }

    public void setHttpEntity(HttpEntity httpEntity) {
        this.httpEntity = httpEntity;
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

    public void setResponse(CloseableHttpResponse response) {
        //重新设置response 销毁之前的对象
        consume();
        this.response = response;
        if (this.response != null) {
            this.httpStatus = response.getStatusLine().getStatusCode();
            this.httpEntity = response.getEntity();

        }
    }

    /**
     * 输出字符串 形式
     * @param enCode 字符编码
     * @return String
     */
    private String getEntityString(String enCode) {
        String result = null;
        try {
            result = EntityUtils.toString(this.httpEntity, enCode);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private String getEntityString() {
        return this.getEntityString("UTF-8");
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        consume();
    }

    public void consume() {
        if (httpEntity != null) {
            try {
                EntityUtils.consume(httpEntity);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                httpEntity = null;
            }
        }
        if (response != null) {
            try {
                response.close();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                response = null;
            }
        }
    }
}
