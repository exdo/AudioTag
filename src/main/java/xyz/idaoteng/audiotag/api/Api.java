package xyz.idaoteng.audiotag.api;

import java.util.List;

public interface Api {
    List<byte[]> getCover(String title, String artist, String album);

    String getLyric(String title, String artist, String album);
}
