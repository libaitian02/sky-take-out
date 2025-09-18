package com.sky.test;


import com.alibaba.fastjson.JSONObject;
import com.google.gson.JsonObject;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

@SpringBootTest
public class HttpClientTest {

    @Test
    public void testHttpClient() throws IOException {

        CloseableHttpClient httpClient = HttpClients.createDefault();

        HttpGet httpGet = new HttpGet("http://localhost:8080/user/shop/status");

        CloseableHttpResponse response = httpClient.execute(httpGet);

        int statusCode = response.getStatusLine().getStatusCode();

        System.out.println("服务端返回的状态码为："+statusCode);

        //获取响应体
        HttpEntity entity = response.getEntity();

        String string = EntityUtils.toString(entity);
        System.out.println("服务端返回的数据为："+string);

        response.close();
        httpClient.close();
    }

    @Test
    public void testPost() throws IOException {

        CloseableHttpClient httpClient = HttpClients.createDefault();


        HttpPost httpPost = new HttpPost("http://localhost:8080/admin/employee/login");

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("username", "admin");
        jsonObject.put("password", "123456");

        StringEntity stringEntity = new StringEntity(jsonObject.toString());
        stringEntity.setContentType("application/json");
        stringEntity.setContentEncoding("UTF-8");
        httpPost.setEntity(stringEntity);
        CloseableHttpResponse response = httpClient.execute(httpPost);

        int statusCode = response.getStatusLine().getStatusCode();

        System.out.println("22服务端返回的状态码为："+statusCode);

        //获取响应体
        HttpEntity entity = response.getEntity();

        String string = EntityUtils.toString(entity);
        System.out.println("22服务端返回的数据为："+string);

        response.close();
        httpClient.close();
    }
}
