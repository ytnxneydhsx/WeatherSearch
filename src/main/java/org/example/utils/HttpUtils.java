package org.example.utils;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.zip.GZIPInputStream;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;

public class HttpUtils {
    private static final String API_HOST = "nj33jpxhva.re.qweatherapi.com";
    private static final String API_KEY = "a24944f7c59c43798ce802e99a1f6f2b";

    // 1. 创建全局唯一的 HttpClient
    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10)) // 设置连接超时
            .build();

    private HttpUtils() {}

    /**
     * 使用 Java 原生 HttpClient 发送 GET 请求
     */
    public static JSONObject getWeatherDataJson(String localtion) throws Exception {

        String URL = "https://" + API_HOST + "/v7/weather/3d?location="+localtion+"&key=" + API_KEY;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(URL))
                .header("Accept-Encoding", "gzip")
                .GET()
                .build();

        HttpResponse<byte[]> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofByteArray());
        byte[] body = response.body();
        String encoding = response.headers().firstValue("Content-Encoding").orElse("");
        String resultString;
        if ("gzip".equalsIgnoreCase(encoding)) {
            resultString = decompressGzip(body);
        } else {
            resultString = new String(body, StandardCharsets.UTF_8);
        }

        JSONObject jsonObject = com.alibaba.fastjson2.JSON.parseObject(resultString);
        String apiCode = jsonObject.getString("code");

        if (!"200".equals(apiCode)) {
            throw new RuntimeException("API 业务逻辑错误，返回码: " + apiCode + "。请检查参数或 Key。");
        }
        return jsonObject;
    }

    private static String decompressGzip(byte[] compressed) throws Exception {
        try (GZIPInputStream gis = new GZIPInputStream(new ByteArrayInputStream(compressed))) {
            return new String(gis.readAllBytes(), StandardCharsets.UTF_8);
        }
    }


}
