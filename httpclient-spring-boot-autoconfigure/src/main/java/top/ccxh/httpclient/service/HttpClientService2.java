package top.ccxh.httpclient.service;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.ccxh.httpclient.service.task.HttpErrorTask;
import top.ccxh.httpclient.service.task.HttpSucceedTask;
import top.ccxh.httpclient.service.task.impl.DefaultErrorTask;
import top.ccxh.httpclient.service.task.impl.RetunStringTask;
import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HttpClientService2 {
    private static  final Logger LOGGER=LoggerFactory.getLogger(HttpClientService2.class);
    private CloseableHttpClient httpClient;

    private RequestConfig requestConfig;

    public HttpClientService2(CloseableHttpClient httpClient, RequestConfig requestConfig) {
        this.httpClient = httpClient;
        this.requestConfig = requestConfig;
    }

    /**
     * 执行提交
     *
     * @param httpMethod
     * @return
     */
    private Object execute(HttpRequestBase httpMethod, HttpErrorTask errorTask, HttpSucceedTask succeedTask) {
        CloseableHttpResponse response = null;
        HttpEntity entity = null;
        Integer statusCode = null;
        LOGGER.debug("执行{}请求，URL = {}",httpMethod.getMethod(),httpMethod.getURI());
        // 执行请求
        try {
            response = httpClient.execute(httpMethod);
            // 判断返回状态是否为200
             statusCode = response.getStatusLine().getStatusCode();
            if ( statusCode== HttpStatus.SC_OK) {
                entity = response.getEntity();
                if (entity==null) {
                      LOGGER.error("entiyt is null");
                }
                return  succeedTask.run(entity);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            consumeEntity(entity);
            closeIO(response);
        }
          LOGGER.info("HttpStatus is {}",statusCode);
        return   errorTask.run(statusCode);
    }


    /**
     * 默认处理方式
     * @param httpMethod
     * @return
     */
    private  String executeDefault(HttpRequestBase httpMethod){
       return (String) execute(httpMethod,new DefaultErrorTask(),new RetunStringTask());
    }


    /**
     * 消费entity
     *
     * @param entity
     */
    private void consumeEntity(HttpEntity entity) {
        if (entity != null) {
            try {
                EntityUtils.consume(entity);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                entity = null;
            }
        }
    }

    /**
     * 关闭输入输出流
     * @param response
     */
    private void closeIO(Closeable response) {
        if (response != null) {
            try {
                response.close();
            } catch (IOException e) {
            } finally {
                response = null;
            }
        }
    }


    /**
     * 执行 get请求
     * @param url url
     * @param params 参数
     * @param header  请求头
     * @param encode 编码方式
     * @return
     * @throws Exception
     */
    public String doGet(String url, Map<String, String> params, Map<String, String> header, String encode) throws Exception {
        if (null != params) {
            URIBuilder builder = new URIBuilder(url);
            for (Map.Entry<String, String> entry : params.entrySet()) {
                builder.setParameter(entry.getKey(), entry.getValue());
            }
            url = builder.build().toString();
        }
        // 创建http GET请求
        HttpGet httpGet = new HttpGet(url);
        httpSetHeader(header, httpGet);
        httpGet.setConfig(requestConfig);
        return executeDefault(httpGet);
    }

    private void httpSetHeader(Map<String, String> header, HttpRequestBase httpGet) {
        if (null != header) {
            for (Map.Entry<String, String> entry : header.entrySet()) {
                httpGet.setHeader(entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * 执行 get请求
     * @param url
     * @return
     * @throws Exception
     */
    public String doGet(String url) throws Exception {
        return this.doGet(url, null, null, null);
    }

    /**
     * 执行 get请求
     * @param url url
     * @param header 头文件 键值对
     * @return
     * @throws Exception
     */
    public String doGetHaeader(String url, Map<String, String> header) throws Exception {
        return this.doGet(url, null, header, null);
    }

    /**
     * 带参数的get请求
     *
     * @param url url
     * @param params 参数
     * @return
     * @throws Exception
     */
    public String doGet(String url, Map<String, String> params) throws Exception {
        return this.doGet(url, params, null, null);
    }

    /**
     * 执行POST请求
     *
     * @param url url
     * @param params 表单参数
     * @param encode
     * @param header
     * @return
     * @throws Exception
     */
    public String doPost(String url, Map<String, String> params, String encode,Map<String, String> header)  {
        // 创建http POST请求
        HttpPost httpPost = new HttpPost(url);
        httpPost.setConfig(requestConfig);
        httpSetHeader(header, httpPost);
        setFormEntity(params, encode, httpPost);
        return executeDefault(httpPost);
    }

    /**
     *
     * @param params
     * @param encode
     * @param httpPost
     */
    private void setFormEntity(Map<String, String> params, String encode, HttpPost httpPost)   {
        try {
            if (null != params) {
                // 设置2个post参数，一个是scope、一个是q
                List<NameValuePair> parameters = new ArrayList<NameValuePair>(0);
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    parameters.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
                }
                // 构造一个form表单式的实体
                UrlEncodedFormEntity formEntity = null;
                if (encode != null) {
                    formEntity = new UrlEncodedFormEntity(parameters, encode);
                } else {
                    formEntity = new UrlEncodedFormEntity(parameters);
                }
                // 将请求实体设置到httpPost对象中
                httpPost.setEntity(formEntity);
            }
        }catch (Exception e){

        }

    }

    /**
     * 使用代理
     * @param hostName  代理主机名
     * @param port   代理端口
     * @param url  访问的url
     * @return
     */
    public String proxyGet(String hostName,Integer port,String url){
        HttpGet httpGet = new HttpGet(url);
        HttpHost proxy=new HttpHost(hostName, port);
        RequestConfig requestConfig=RequestConfig.custom().setProxy(proxy).build();
        httpGet.setConfig(requestConfig);
        CloseableHttpResponse response = null;
        return executeDefault(httpGet);
    }

}
