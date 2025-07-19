package xyz.idaoteng.audiotag.api.netease;

import com.google.gson.JsonObject;
import xyz.idaoteng.audiotag.api.MusicApi;
import xyz.idaoteng.audiotag.api.netease.dto.Artist;
import xyz.idaoteng.audiotag.api.netease.dto.SearchResult;
import xyz.idaoteng.audiotag.api.netease.dto.Song;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 网易云音乐API工具类，用于搜索歌曲和获取歌词。
 */
public class NetEaseMusicApi implements MusicApi {

    // --- 常量定义 ---
    private static final String SEARCH_API_URL = "https://music.163.com/api/cloudsearch/pc?csrf_token=";
    private static final String LYRIC_API_URL = "https://music.163.com/weapi/song/lyric?csrf_token=";

    // 歌词结果的顶层结构
    private record LyricResult(int code, LyricData lrc, LyricData tlyric) {
    }

    private record LyricData(String lyric) {
    }

    // 加密相关常量
    private static final String G_KEY = "0CoJUm6Qyw8W8jud"; // First AES key
    private static final String I_KEY = "KYWx6w2CgEfW4QB8"; // Second AES key
    private static final String IV = "0102030405060708"; // AES IV
    private static final String ENC_SEC_KEY = "d79c9344cfcd6cf951345a79c697436935f7c3a11e6a8a8222983a80a5b51feb9a55161a17531e12207b841c2f02b108e56ef57c1dbf2cc00c76dd4e726669cec3b6d854b6812499f3ff37a687d688ef14bbc15076f7ce1dd00ad0ae058b3eb3e577a28a3af4e043807e35021c8608ad24a4ab74cd0fb3f56bd2b2cc0cf7a703";

    private static final List<String> USER_AGENTS = Arrays.asList(
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/136.0.0.0 Safari/537.36 Edg/136.0.0.0",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:109.0) Gecko/20100101 Firefox/114.0",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 13_4) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/16.5 Safari/605.1.15"
    );

    private static final Random RANDOM = new Random();

    @Override
    public List<byte[]> getCover(String title, String artist, String album) {
        if (title == null || title.trim().equals("")) {
            return Collections.emptyList();
        }

        ArrayList<byte[]> covers = new ArrayList<>();
        List<Song> searchResult = searchSongs(title);
        for (Song song : searchResult) {
            if (song.getName().contains(title)) {
                if (song.getAl().getPicUrl() != null) {
                    Optional<byte[]> optionalBytes = getBytes(song.getAl().getPicUrl());
                    optionalBytes.ifPresent(covers::add);
                }
            }
        }
        return covers;
    }

    /**
     * 搜索歌曲。
     *
     * @param songName 歌曲名称
     * @return 歌曲信息列表，如果搜索失败或无结果则返回空列表
     */
    public static List<Song> searchSongs(String songName) {
        String encodedSongName = encodeURIComponent(songName);
        String url = String.format("%s&s=%s&type=1&offset=0&limit=10", SEARCH_API_URL, encodedSongName);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("User-Agent", getRandomUserAgent())
                .GET()
                .build();

        HttpResponse<String> response;
        try {
            response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            return Collections.emptyList();
        }

        if (response.statusCode() != 200) {
            return Collections.emptyList();
        }

        SearchResult searchResult = GSON.fromJson(response.body(), SearchResult.class);
        if (searchResult != null && searchResult.getResult() != null) {
            if (searchResult.getResult().getSongCount() > 0) {
                return searchResult.getResult().getSongs();
            } else {
                return Collections.emptyList();
            }
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public List<String> getLyric(String title, String artist, String album) {
        if (title == null || title.trim().isEmpty()) {
            return Collections.emptyList();
        }

        List<String> lyricList = new ArrayList<>();
        List<Song> songs = searchSongs(title);
        for (Song song : songs) {
            if (song.getName().contains(title) && artist != null && !artist.trim().isEmpty()) {
                if (song.getAr().stream().map(Artist::getName).anyMatch(name -> name.contains(artist))) {
                    Optional<String> lyrics = fetchLyrics(Long.parseLong(song.getId()));
                    lyrics.ifPresent(lyricList::add);
                }
            }
        }
        return lyricList;
    }

    /**
     * 获取歌曲歌词。
     *
     * @param songId 歌曲ID
     * @return 歌词文本，如果获取失败或无歌词则返回 Optional.empty()
     */
    public static Optional<String> fetchLyrics(long songId) {
        JsonObject requestData = new JsonObject();
        requestData.addProperty("csrf_token", "");
        requestData.addProperty("id", songId);
        requestData.addProperty("lv", "-1");
        requestData.addProperty("tv", "-1");

        String params = getEncryptedParams(requestData.toString());

        // 使用 Map 构建表单数据
        Map<String, String> formData = new HashMap<>();
        formData.put("params", params);
        formData.put("encSecKey", ENC_SEC_KEY);

        // 将 Map 转换为 application/x-www-form-urlencoded 格式
        String requestBody = formData.entrySet().stream()
                .map(entry -> URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8) + "=" +
                        URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(LYRIC_API_URL))
                .header("User-Agent", getRandomUserAgent())
                .header("Referer", "https://music.163.com/") // 某些API可能需要Referer
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response;
        try {
            response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            return Optional.empty();
        }

        if (response.statusCode() != 200) {
            return Optional.empty();
        }

        LyricResult lyricResult = GSON.fromJson(response.body(), LyricResult.class);

        if (lyricResult.code() == 200 && lyricResult.lrc() != null && lyricResult.lrc().lyric() != null) {
            return Optional.of(lyricResult.lrc().lyric());
        } else {
            return Optional.empty();
        }
    }

    /**
     * 获取随机User-Agent。
     */
    private static String getRandomUserAgent() {
        return USER_AGENTS.get(RANDOM.nextInt(USER_AGENTS.size()));
    }

    /**
     * 对应Python的get_params函数，执行两次AES加密。
     *
     * @param data 原始数据字符串
     * @return 加密后的字符串
     */
    private static String getEncryptedParams(String data) {
        String firstEnc = encryptAes(data, G_KEY);
        return encryptAes(firstEnc, I_KEY);
    }

    /**
     * 对应Python的enc_params函数，执行AES加密。
     *
     * @param data 待加密数据
     * @param key  加密密钥
     * @return Base64编码的加密结果
     */
    private static String encryptAes(String data, String key) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "AES");
            IvParameterSpec ivParameterSpec = new IvParameterSpec(IV.getBytes(StandardCharsets.UTF_8));

            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);
            byte[] encryptedBytes = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 模拟JavaScript的encodeURIComponent，对URL组件进行编码。
     * URLEncoder.encode默认会将空格转为'+'，这里转回'%20'，并保留一些特殊字符。
     *
     * @param s 待编码字符串
     * @return 编码后的字符串
     */
    private static String encodeURIComponent(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8)
                .replaceAll("\\+", "%20") // 空格
                .replaceAll("\\*", "%2A") // 星号
                .replaceAll("%21", "!")   // 感叹号
                .replaceAll("%27", "'")   // 单引号
                .replaceAll("%28", "(")   // 左括号
                .replaceAll("%29", ")");  // 右括号
    }
}
