package xyz.idaoteng.audiotag.api.timeless;

import com.google.gson.Gson;
import xyz.idaoteng.audiotag.api.CoverApi;
import xyz.idaoteng.audiotag.api.timeless.dto.SongDetail;
import xyz.idaoteng.audiotag.api.timeless.dto.SongsResult;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class TimelessApi implements CoverApi {
    private static final String SEARCH_URL = "https://api.timelessq.com/music/tencent/search?keyword=%s&page=1&pageSize=10";
    //private static final String LYRIC_URL = "https://api.timelessq.com/music/tencent/lyric?songmid=%s";
    private static final Gson gson = new Gson();
    private static final HttpClient CLIENT = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();


    private SongsResult searchSong(String keyword) throws IOException, InterruptedException {
        keyword = encodeKeyword(keyword);
        return gson.fromJson(sendRequest(keyword), SongsResult.class);
    }

    private String sendRequest(String arg) throws IOException, InterruptedException {
        String url = String.format(TimelessApi.SEARCH_URL, arg);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            return "";
        }
        return response.body();
    }


    @Override
    public List<byte[]> getCover(String title, String artist, String album) {
        List<byte[]> covers = new ArrayList<>();
        try {
            SongsResult result = searchSong(title);
            SongDetail[] details = result.getData().getList();
            for (SongDetail detail : details) {
                if (detail.getSongname().contains(title)) {
                    if (!"".equals(detail.getAlbumcover())) {
                        byte[] cover = fetchCover(detail.getAlbumcover());
                        if (cover != null) {
                            covers.add(cover);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return covers;
        }
        return covers;
    }
}