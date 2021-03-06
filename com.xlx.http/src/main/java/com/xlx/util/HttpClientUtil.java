package com.xlx.util;

import java.io.*;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;


/**
 * @Auther: 徐林啸
 * @Date: 2019/6/4 22:06
 * @Description:
 */
@Slf4j
public class HttpClientUtil {

    //统一配置
    private static PoolingHttpClientConnectionManager connMgr;
    private static RequestConfig requestConfig;
    private static final int MAX_TIMEOUT = 7000;

    static {
        // 设置连接池
        connMgr = new PoolingHttpClientConnectionManager();
        // 设置连接池大小
        connMgr.setMaxTotal(100);
        connMgr.setDefaultMaxPerRoute(connMgr.getMaxTotal());

        RequestConfig.Builder configBuilder = RequestConfig.custom();
        // 设置连接超时
        configBuilder.setConnectTimeout(MAX_TIMEOUT);
        // 设置读取超时
        configBuilder.setSocketTimeout(MAX_TIMEOUT);
        // 设置从连接池获取连接实例的超时
        configBuilder.setConnectionRequestTimeout(MAX_TIMEOUT);
        // 在提交请求之前 测试连接是否可用
        configBuilder.setStaleConnectionCheckEnabled(true);
        requestConfig = configBuilder.build();
    }

    /**
     * 发送get请求不带参数
     *
     * @param url
     * @return
     */
    public static String sendGet(String url) {

        return sendGet(url, null);
    }

    /**
     * 发送get请求
     *
     * @param url
     * @param map
     * @return
     */
    public static String sendGet(String url, Map<String, String> map) {

        String resulrStr = null;
        StringBuffer parms = null;
        //创建HttpClient对象
        CloseableHttpClient httpClient = HttpClients.createDefault();
        //设置请求参数
        if (null != map && !map.isEmpty()) {
            //遍历map
            parms = new StringBuffer();
            for (Entry<String, String> entry : map.entrySet()) {
                parms.append("&" + entry.getKey() + "=" + entry.getValue());
            }
            url = url + parms.toString();
        }
        //创建GET请求方法的实例，并填充url
        HttpGet httpGet = new HttpGet(url);
        httpGet.setConfig(requestConfig);
        try {
            //发送（执行）请求
            CloseableHttpResponse response = httpClient.execute(httpGet);
            //获取响应头、内容
            int statusCode = response.getStatusLine().getStatusCode();
            //打印状态码
            log.info("执行状态码：" + statusCode);
            HttpEntity entity = response.getEntity();
            resulrStr = IOUtils.toString(entity.getContent(), "UTF-8");
            response.close();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            close(httpClient);
        }
        return resulrStr;
    }

    /**
     * 发送post请求
     *
     * @param url
     * @param map
     * @return
     */
    public static String sendPost(String url, Map<String, String> map) {

        String resultStr = null;
        //创建HttpClient对象
        CloseableHttpClient httpClient = HttpClients.createDefault();
        //创建请求方法实例，填充url
        HttpPost httpPost = new HttpPost(url);
        httpPost.setConfig(requestConfig);
        //设置请求参数（构造参数队列）
        List<NameValuePair> parmsForm = new ArrayList<NameValuePair>();
        parmsForm = createUrl(parmsForm, map);
        UrlEncodedFormEntity entity;
        try {
            entity = new UrlEncodedFormEntity(parmsForm, "UTF-8");
            //为方法实例设置参数队列实体
            httpPost.setEntity(entity);
            //发送（执行）
            CloseableHttpResponse response = httpClient.execute(httpPost);
            //获取状态码
            int statusCode = response.getStatusLine().getStatusCode();
            System.out.println("执行状态码：" + statusCode);
            //获取响应内容
            HttpEntity httpEntity = response.getEntity();
            resultStr = IOUtils.toString(httpEntity.getContent(), "UTF-8");
            response.close();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            close(httpClient);
        }
        return resultStr;
    }


    public static List<NameValuePair> createUrl(List<NameValuePair> parmsForm, Map<String, String> map) {
        for (Entry<String, String> entry : map.entrySet()) {
            parmsForm.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
        }
        return parmsForm;
    }

    /**
     * 发送put请求
     *
     * @param url
     * @param map
     * @return
     */
    public static String sendPut(String url, Map<String, String> map) {

        String resultStr = null;
        //新建httpClient对象
        CloseableHttpClient httpClient = HttpClients.createDefault();
        //创建请求方法实例并填充url
        HttpPut httpPut = new HttpPut(url);
        httpPut.setConfig(requestConfig);
        //创建参数队列
        List<NameValuePair> parmsForm = new ArrayList<NameValuePair>();
        parmsForm = createUrl(parmsForm, map);
        UrlEncodedFormEntity entity;
        try {
            entity = new UrlEncodedFormEntity(parmsForm);
            //将参数加入方法实例
            httpPut.setEntity(entity);
            //发送
            CloseableHttpResponse response = httpClient.execute(httpPut);
            //获取状态码
            int statusCode = response.getStatusLine().getStatusCode();
            System.out.println(statusCode);
            //获取响应内容
            HttpEntity httpEntity = response.getEntity();
            resultStr = IOUtils.toString(httpEntity.getContent(), "UTF-8");
            //关闭响应
            response.close();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            close(httpClient);
        }
        return resultStr;
    }

    /**
     * 发送delete请求
     *
     * @param url
     * @return
     */
    public static String sendDelete(String url,Map<String, String> param) {

        // 创建Httpclient对象
        CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpResponse response = null;
        String resultString = "";
        try {
            // 创建uri
            URIBuilder builder = new URIBuilder(url);
            if (param != null) {
                for (String key : param.keySet()) {
                    builder.addParameter(key, param.get(key));
                }
            }
            URI uri = builder.build();
            // 创建Http Delete请求
            HttpDelete httpDelete = new HttpDelete(uri);
            // 执行http请求
            response = httpClient.execute(httpDelete);
            resultString = EntityUtils.toString(response.getEntity(), "UTF-8");
        } catch (Exception e) {
            log.error("" + e);
        } finally {
            try {
                if (response != null) {
                    response.close();
                }
            } catch (IOException e) {
                log.error("" + e);
            }
        }
        return resultString;


    }

    public static void close(CloseableHttpClient httpClient) {
        if (null != httpClient) {
            try {
                httpClient.close();
            } catch (Throwable e) {
                log.error("fail to close HttpClient" + e);
            }
        }
    }

    public static int saveFile(String url, File file) {
        int code = 1;
        try {
            CloseableHttpClient httpClient = HttpClientBuilder.create().build();
            CloseableHttpResponse httpResponse = null;
            HttpPost httpPost = new HttpPost(url);
            httpPost.setConfig(requestConfig);
            MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create().setMode(HttpMultipartMode.RFC6532);
            multipartEntityBuilder.setCharset(Charset.forName("UTF-8"));
            multipartEntityBuilder.addBinaryBody("file", file);
            httpPost.setEntity(multipartEntityBuilder.build());
            httpResponse = httpClient.execute(httpPost);
            HttpEntity responseEntity = httpResponse.getEntity();
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            if (statusCode == 200) {
                String s = EntityUtils.toString(responseEntity,"UTF-8");
                JSONObject jsonObject = JSON.parseObject(s);
                code = (int) jsonObject.get("code");
            }
            httpClient.close();
            if (httpResponse != null) {
                httpResponse.close();
            }
        } catch (Throwable e) {
            log.warn("fail to execute htpPost" + e);
        }
        return code;
    }


    public static void download(String remoteFileName, String localFileName,String url){
        DefaultHttpClient httpClient = new DefaultHttpClient();
        OutputStream out = null;
        InputStream in = null;

        try {
            HttpGet httpGet = new HttpGet(url+"?fileName=" + remoteFileName);
            HttpResponse httpResponse = httpClient.execute(httpGet);
            HttpEntity entity = httpResponse.getEntity();
            String s = EntityUtils.toString(entity,"UTF-8");
            byte[] bytes = s.getBytes();
            FileUtils.getFile(bytes, "E:\\file\\test", localFileName);
        }catch (Throwable e){
            log.error("fail to write file[{}] ex[{}]" , new Object[]{localFileName},e);
        }
    }

}
