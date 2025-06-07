package xyz.idaoteng.audiotag.api.migu;

import com.google.gson.Gson;
import xyz.idaoteng.audiotag.api.CoverApi;
import xyz.idaoteng.audiotag.api.migu.dto.MiguSong;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class MiguMusicApi implements CoverApi {
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/136.0.0.0 Safari/537.36 Edg/136.0.0.0";
    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    private final Gson gson = new Gson();

    private String sendRequest(String keyword) throws IOException, InterruptedException {
        String url = "https://app.u.nf.migu.cn/pc/resource/song/item/search/v1.0?text=" +
                encodeKeyword(keyword) +
                "&pageNo=1&pageSize=20";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/json, text/plain, */*")
                .header("Activityid", "MUSIC-WWW")
                .header("Appid", "h5")
                .header("Platform", "H5")
                .header("User-Agent", USER_AGENT)
                .GET()
                .build();

        HttpResponse<String> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

        return response.body();
    }


    @Override
    public List<byte[]> getCover(String title, String artist, String album) {
        ArrayList<byte[]> covers = new ArrayList<>();
        try {
            String response = sendRequest(title);
            if (!response.trim().isEmpty()) {
                MiguSong[] songs = gson.fromJson(response, MiguSong[].class);
                if (songs != null && songs.length > 0) {
                    for (MiguSong song : songs) {
                        if (song.getSongName().contains(title)) {
                            String imgUrl = null;
                            if (song.getImg3() != null && song.getImg3().startsWith("http")) {
                                imgUrl = song.getImg3();
                            } else if (song.getImg2() != null && song.getImg2().startsWith("http")) {
                                imgUrl = song.getImg2();
                            } else if (song.getImg1() != null && song.getImg1().startsWith("http")) {
                                imgUrl = song.getImg1();
                            }

                            if (imgUrl != null) {
                                byte[] cover = fetchCover(imgUrl);
                                // 从咪咕音乐获取的图片多为 webp 格式，
                                // 需要转换成 jpg 格式才能在 ImagView 中显示
                                BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(cover));
                                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                                ImageIO.write(bufferedImage, "jpg", outputStream);
                                covers.add(outputStream.toByteArray());
                            }
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