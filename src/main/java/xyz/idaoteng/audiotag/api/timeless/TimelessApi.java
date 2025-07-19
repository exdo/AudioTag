package xyz.idaoteng.audiotag.api.timeless;

import xyz.idaoteng.audiotag.api.MusicApi;
import xyz.idaoteng.audiotag.api.timeless.dto.LyricResult;
import xyz.idaoteng.audiotag.api.timeless.dto.SongDetail;
import xyz.idaoteng.audiotag.api.timeless.dto.SongsResult;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TimelessApi implements MusicApi {
    private static final String SEARCH_URL = "https://api.timelessq.com/music/tencent/search?keyword=%s&page=1&pageSize=10";
    private static final String LYRIC_URL = "https://api.timelessq.com/music/tencent/lyric?songmid=%s";

    private Optional<SongsResult> searchSong(String keyword) {
        if (keyword == null || keyword.trim().equals("")) {
            return Optional.empty();
        }

        keyword = URLEncoder.encode(keyword, StandardCharsets.UTF_8);
        String url = String.format(TimelessApi.SEARCH_URL, keyword);
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();

        HttpResponse<String> response;
        try {
            response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            SongsResult songsResult = GSON.fromJson(response.body(), SongsResult.class);
            return Optional.of(songsResult);
        } catch (IOException | InterruptedException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<byte[]> getCover(String title, String artist, String album) {
        List<byte[]> covers = new ArrayList<>();

        Optional<SongsResult> optionalResult = searchSong(title);
        if (optionalResult.isPresent()) {
            SongDetail[] details = optionalResult.get().getData().getList();
            for (SongDetail detail : details) {
                if (detail.getSongname().contains(title)) {
                    if (!"".equals(detail.getAlbumcover())) {
                        Optional<byte[]> optionalBytes = getBytes(detail.getAlbumcover());
                        optionalBytes.ifPresent(covers::add);
                    }
                }
            }
        }

        return covers;
    }

    @Override
    public List<String> getLyric(String title, String artist, String album) {
        List<String> lyrics = new ArrayList<>();

        Optional<SongsResult> optionalResult = searchSong(title);
        if (optionalResult.isPresent()) {
            SongDetail[] details = optionalResult.get().getData().getList();
            for (SongDetail detail : details) {
                if (detail.getSongname().contains(title) && artist != null && !artist.trim().isEmpty()) {
                    Optional<String> lyric = fetchLyric(detail.getSongmid());
                    lyric.ifPresent(lyrics::add);
                }
            }
        }

        return lyrics;
    }

    private Optional<String> fetchLyric(String id) {
        String url = String.format(LYRIC_URL, id);
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
        HttpResponse<String> response;
        try {
            response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            LyricResult lyricResult = GSON.fromJson(response.body(), LyricResult.class);
            String lyric = lyricResult.getLyric().getLyric();
            return lyric == null ? Optional.empty() : Optional.of(lyric);
        } catch (IOException | InterruptedException e) {
            return Optional.empty();
        }
    }
}