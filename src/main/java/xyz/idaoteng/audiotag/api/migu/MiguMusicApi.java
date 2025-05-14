package xyz.idaoteng.audiotag.api.migu;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import xyz.idaoteng.audiotag.api.Api;
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

public class MiguMusicApi implements Api {
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/136.0.0.0 Safari/537.36 Edg/136.0.0.0";

    private final Gson gson = new Gson();
    private String sendRequest(String keyword) {
        HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

        String url = "https://app.u.nf.migu.cn/pc/resource/song/item/search/v1.0?text=" +
                decodeKeyword(keyword) +
                "&pageNo=1&pageSize=20";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/json, text/plain, */*")
                .header("Activityid", "MUSIC-WWW")
                .header("Appid", "h5")
                .header("Platform", "H5")
                .header("User-Agent", USER_AGENT)
                .GET().build();

        try {
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.body();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<byte[]> getCover(String title, String artist, String album) {
        ArrayList<byte[]> covers = new ArrayList<>();
        String response = sendRequest(title);
        if (response != null && !"".equals(response.trim())) {
            MiguSong[] songs = gson.fromJson(response, new TypeToken<MiguSong[]>(){}.getType());
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
                            // migu的图片是webp，需要转成jpg
                            BufferedImage bufferedImage;
                            try {
                                bufferedImage = ImageIO.read(new ByteArrayInputStream(cover));
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }

                            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                            try {
                                ImageIO.write(bufferedImage, "jpg", outputStream);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                            covers.add(outputStream.toByteArray());
                        }
                    }
                }
            }
        }

        return covers;
    }

    @Override
    public String getLyric(String title, String artist, String album) {
        return null;
    }
}
