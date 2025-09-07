package xyz.idaoteng.audiotag.api;

import com.google.gson.Gson;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public interface MusicApi {
    HttpClient HTTP_CLIENT = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    Gson GSON = new Gson();

    default List<byte[]> getCover(String title, String artist, String album) {
        return Collections.emptyList();
    }

    default List<String> getLyric(String title, String artist, String album) {
        return Collections.emptyList();
    }

    /**
     * 模拟JavaScript的encodeURIComponent，对URL组件进行编码。
     * URLEncoder.encode默认会将空格转为'+'，这里转回'%20'，并保留一些特殊字符。
     *
     * @param s 待编码字符串
     * @return 编码后的字符串
     */
    default String encodeURIComponent(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8)
                .replaceAll("\\+", "%20") // 空格
                .replaceAll("\\*", "%2A") // 星号
                .replaceAll("%21", "!")   // 感叹号
                .replaceAll("%27", "'")   // 单引号
                .replaceAll("%28", "(")   // 左括号
                .replaceAll("%29", ")");  // 右括号
    }

    default Optional<byte[]> getBytes(String url) {
        if (url == null || !url.startsWith("http")) {
            return Optional.empty();
        }

        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
        try {
            HttpResponse<byte[]> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofByteArray());
            if (response.statusCode() == 200) {
                return Optional.of(response.body());
            } else {
                return Optional.empty();
            }
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}