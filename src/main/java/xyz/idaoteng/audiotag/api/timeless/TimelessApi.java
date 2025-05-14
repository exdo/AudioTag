package xyz.idaoteng.audiotag.api.timeless;

import com.google.gson.Gson;
import xyz.idaoteng.audiotag.api.Api;
import xyz.idaoteng.audiotag.api.timeless.dto.LyricResult;
import xyz.idaoteng.audiotag.api.timeless.dto.SongDetail;
import xyz.idaoteng.audiotag.api.timeless.dto.SongsResult;
import xyz.idaoteng.audiotag.exception.ApiException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TimelessApi implements Api {
    private static final String SEARCH_URL = "https://api.timelessq.com/music/tencent/search?keyword=%s&page=1&pageSize=10";
    private static final Gson gson = new Gson();

    private static SongsResult searchSong(String keyword) throws ApiException {
        keyword = keyword.replaceAll(" ", "%20");
        String url = String.format(SEARCH_URL, keyword);
        HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
        try {
            HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
            return gson.fromJson(response.body(), SongsResult.class);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            throw new ApiException();
        }
    }

    private static final String LYRIC_URL = "https://api.timelessq.com/music/tencent/lyric?songmid=%s";
    private static LyricResult searchLyric(String songMid) throws ApiException {
        String url = String.format(LYRIC_URL, songMid);
        HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
        try {
            HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
            System.out.println(response.body());

            return gson.fromJson(response.body(), LyricResult.class);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            throw new ApiException();
        }
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
        } catch (ApiException e) {
            return null;
        }
        return covers;
    }

    @Override
    public String getLyric(String title, String artist, String album) {
        try {
            SongsResult result = searchSong(title);
            SongDetail[] details = result.getData().getList();
            for (SongDetail detail : details) {
                if (!title.equals(detail.getSongname())) {
                    continue;
                }

                if (!"".equals(artist)) {
                    if (Arrays.stream(detail.getSinger()).noneMatch(s -> s.getName().equals(artist))) {
                        continue;
                    }
                }

                if (!"".equals(album) && !"".equals(detail.getAlbumname())) {
                    if (!album.equals(detail.getAlbumname())) {
                        continue;
                    }
                }

                LyricResult lyricResult = searchLyric(detail.getSongmid());
                return lyricResult.getLyric().getLyric();
            }
        } catch (ApiException e) {
            return "";
        }
        return null;
    }
}
