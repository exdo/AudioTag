package xyz.idaoteng.audiotag.api;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

public interface Api {
    List<byte[]> getCover(String title, String artist, String album);

    String getLyric(String title, String artist, String album);

    default byte[] fetchCover(String url) {
        HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
        try {
            HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
            return response.body();
        } catch (IOException | InterruptedException e) {
            return null;
        }
    }

    default String decodeKeyword(String original) {
        if (original == null || original.trim().equals("")) {
            return original;
        }

        try {
            return URLEncoder.encode(original, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            // UTF-8 应该总是可用
            throw new RuntimeException(e);
        }
    }
}
