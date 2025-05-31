package xyz.idaoteng.audiotag.api.netease;

import com.google.gson.Gson;
import xyz.idaoteng.audiotag.api.Api;
import xyz.idaoteng.audiotag.api.netease.dto.SearchResult;
import xyz.idaoteng.audiotag.api.netease.dto.Song;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class NetEaseMusicApi implements Api {
    private static final Gson gson = new Gson();
    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();


    private SearchResult searchMusic(String keywords) throws Exception {
        Map<String, Object> data = new HashMap<>();
        data.put("s", keywords);
        data.put("limit", 5);
        data.put("offset", 0);
        data.put("type", 1);

        Map<String, String> encryptedData = ApiTool.encryptParams(data);
        String response = sendRequest("https://music.163.com/weapi/cloudsearch/pc", encryptedData);
        return gson.fromJson(response, SearchResult.class);
    }

    private String fetchLyric(String id) throws Exception {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Song ID cannot be empty");
        }

        Map<String, Object> data = new HashMap<>();
        data.put("id", id);
        data.put("lv", -1);
        data.put("tv", -1);

        Map<String, String> encryptedData = ApiTool.encryptParams(data);
        return sendRequest("https://music.163.com/weapi/song/lyric", encryptedData);
    }

    private String sendRequest(String url, Map<String, String> data) throws IOException, InterruptedException {
        String formData = data.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining("&"));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("User-Agent", ApiTool.getRandomUserAgent())
                .header("Referer", "https://music.163.com")
                .header("X-Real-IP", ApiTool.getRandomIP())
                .header("Client-IP", ApiTool.getRandomIP())
                .header("X-Forwarded-For", ApiTool.getRandomIP())
                .POST(HttpRequest.BodyPublishers.ofString(formData))
                .build();

        HttpResponse<String> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

        return response.body();
    }


    @Override
    public List<byte[]> getCover(String title, String artist, String album) {
        ArrayList<byte[]> covers = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            try {
                SearchResult searchResult = searchMusic(title);
                if (searchResult == null) continue;

                for (Song song : searchResult.getResult().getSongs()) {
                    if (song.getName().contains(title)) {
                        if (artist != null && artist.equals(song.getAr().get(0).getName())) {
                            if (song.getAl().getPicUrl() != null) {
                                covers.add(fetchCover(song.getAl().getPicUrl()));
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                return covers;
            }
            if (!covers.isEmpty()) break;
        }
        return covers;
    }

    @Override
    public String getLyric(String title, String artist, String album) {
        return null;
    }
}