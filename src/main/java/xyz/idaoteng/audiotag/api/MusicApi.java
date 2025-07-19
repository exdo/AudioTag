package xyz.idaoteng.audiotag.api;

import com.google.gson.Gson;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
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