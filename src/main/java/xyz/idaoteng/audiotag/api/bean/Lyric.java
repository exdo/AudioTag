package xyz.idaoteng.audiotag.api.bean;

public class Lyric {
    /**
     * 歌词
     */
    private String lyric;
    /**
     * 歌词翻译
     */
    private String tlyric;

    public String getLyric() { return lyric; }
    public void setLyric(String value) { this.lyric = value; }

    public String getTlyric() { return tlyric; }
    public void setTlyric(String value) { this.tlyric = value; }
}
