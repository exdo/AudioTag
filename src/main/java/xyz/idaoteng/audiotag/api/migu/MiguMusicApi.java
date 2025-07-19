package xyz.idaoteng.audiotag.api.migu;

import xyz.idaoteng.audiotag.api.MusicApi;
import xyz.idaoteng.audiotag.api.migu.dto.MiguSong;
import xyz.idaoteng.audiotag.api.migu.dto.SingerList;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class MiguMusicApi implements MusicApi {
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/136.0.0.0 Safari/537.36 Edg/136.0.0.0";

    private List<MiguSong> searchSongs(String keyword) {
        String url = "https://app.u.nf.migu.cn/pc/resource/song/item/search/v1.0?text="
                + URLEncoder.encode(keyword, StandardCharsets.UTF_8)
                + "&pageNo=1&pageSize=20";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/json, text/plain, */*")
                .header("Activityid", "MUSIC-WWW")
                .header("Appid", "h5")
                .header("Platform", "H5")
                .header("User-Agent", USER_AGENT)
                .GET()
                .build();

        HttpResponse<String> response;
        try {
            response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            MiguSong[] songs = GSON.fromJson(response.body(), MiguSong[].class);
            return new ArrayList<>(Arrays.asList(songs));
        } catch (IOException | InterruptedException e) {
            return Collections.emptyList();
        }
    }

    @Override
    public List<byte[]> getCover(String title, String artist, String album) {
        if (title == null || title.trim().equals("")) {
            return Collections.emptyList();
        }

        ArrayList<byte[]> covers = new ArrayList<>();
        List<MiguSong> songs = searchSongs(title);
        for (MiguSong song : songs) {
            if (song.getSongName().contains(title)) {
                Optional<String> bestUrl = getBestImgUrl(song);
                if (bestUrl.isPresent()) {
                    Optional<byte[]> jpgCover = getJpgCover(bestUrl.get());
                    jpgCover.ifPresent(covers::add);
                }
            }
        }
        return covers;
    }

    @Override
    public List<String> getLyric(String title, String artist, String album) {
        if (title == null || title.trim().equals("")) {
            return Collections.emptyList();
        }

        List<String> lyrics = new ArrayList<>();
        List<MiguSong> miguSongs = searchSongs(title);
        for (MiguSong song : miguSongs) {
            if (song.getSongName().contains(title) && artist != null && !artist.trim().isEmpty()) {
                boolean match = Arrays.stream(song.getSingerList())
                        .map(SingerList::getName)
                        .anyMatch(name -> name.contains(artist));

                if (match) {
                    if (song.getExt().getLrcURL() != null) {
                        Optional<byte[]> optionalBytes = getBytes(song.getExt().getLrcURL());
                        optionalBytes.ifPresent(bytes -> lyrics.add(new String(bytes)));
                    }
                }
            }
        }
        return lyrics;
    }

    private Optional<String> getBestImgUrl(MiguSong song) {
        if (song.getImg3() != null && song.getImg3().startsWith("http")) {
            return Optional.of(song.getImg3());
        } else if (song.getImg2() != null && song.getImg2().startsWith("http")) {
            return Optional.of(song.getImg2());
        } else if (song.getImg1() != null && song.getImg1().startsWith("http")) {
            return Optional.of(song.getImg1());
        }
        return Optional.empty();
    }

    private Optional<byte[]> getJpgCover(String url) {
        Optional<byte[]> optionalBytes = getBytes(url);
        if (optionalBytes.isPresent()) {
            // 从咪咕音乐获取的图片多为 webp 格式，
            // 需要转换成 jpg 格式才能在 ImagView 中显示
            BufferedImage bufferedImage;
            try {
                bufferedImage = ImageIO.read(new ByteArrayInputStream(optionalBytes.get()));
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                ImageIO.write(bufferedImage, "jpg", outputStream);
                return Optional.of(outputStream.toByteArray());
            } catch (IOException e) {
                return Optional.empty();
            }
        } else {
            return Optional.empty();
        }
    }
}