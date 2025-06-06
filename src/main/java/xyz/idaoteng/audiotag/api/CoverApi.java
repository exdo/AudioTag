package xyz.idaoteng.audiotag.api;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

public interface CoverApi {
    List<byte[]> getCover(String title, String artist, String album);

    HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    default byte[] fetchCover(String url) {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();

        try {
            HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
            if (response.statusCode() == 200) {
                return response.body();
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    default String encodeKeyword(String original) {
        if (original == null || original.trim().equals("")) {
            return original;
        }

        return URLEncoder.encode(original, StandardCharsets.UTF_8);
    }
}