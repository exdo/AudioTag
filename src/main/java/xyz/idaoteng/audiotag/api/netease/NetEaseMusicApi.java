package xyz.idaoteng.audiotag.api.netease;

import com.google.gson.Gson;
import xyz.idaoteng.audiotag.api.Api;
import xyz.idaoteng.audiotag.api.netease.dto.SearchResult;
import xyz.idaoteng.audiotag.api.netease.dto.Song;
import xyz.idaoteng.audiotag.exception.ApiException;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NetEaseMusicApi implements Api {
    private static final Gson gson = new Gson();

    public static SearchResult searchMusic(String keywords) throws Exception {
        // Validate parameters
        if (keywords == null || keywords.trim().isEmpty()) {
            throw new IllegalArgumentException("Song name cannot be empty");
        }

        // Build request data
        Map<String, Object> data = new HashMap<>();
        data.put("s", keywords);
        data.put("limit", 5);
        data.put("offset", 0);
        data.put("type", 1);

        // Encrypt and send request
        Map<String, String> encryptedData = ApiTool.encryptParams(data);
        String response = sendRequest("https://music.163.com/weapi/cloudsearch/pc", encryptedData);
        return gson.fromJson(response, SearchResult.class);
    }

    public static String getLyric(String id) throws Exception {
        // Validate parameters
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Song ID cannot be empty");
        }

        // Build request data
        Map<String, Object> data = new HashMap<>();
        data.put("id", id);
        data.put("lv", -1);
        data.put("tv", -1);

        // Encrypt and send request
        Map<String, String> encryptedData = ApiTool.encryptParams(data);
        return sendRequest("https://music.163.com/weapi/song/lyric", encryptedData);
    }

    private static String sendRequest(String url, Map<String, String> data) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();

        try {
            // Set request properties
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("User-Agent", ApiTool.getRandomUserAgent());
            conn.setRequestProperty("Referer", "https://music.163.com");
            conn.setRequestProperty("X-Real-IP", ApiTool.getRandomIP());
            conn.setRequestProperty("Client-IP", ApiTool.getRandomIP());
            conn.setRequestProperty("X-Forwarded-For", ApiTool.getRandomIP());

            // Write request body
            String postData = "params=" + data.get("params") + "&encSecKey=" + data.get("encSecKey");
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = postData.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            // Read response
            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new ApiException("HTTP error code: " + conn.getResponseCode());
            }

            StringBuilder response = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
            }

            return response.toString();
        } finally {
            conn.disconnect();
        }
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
                throw new RuntimeException(e);
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