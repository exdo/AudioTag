package xyz.idaoteng.audiotag.api.bean;

public class LyricResult {
    private Lyric lyric;
    private String errmsg;
    private long errno;

    public Lyric getLyric() { return lyric; }
    public void setLyric(Lyric value) { this.lyric = value; }

    public String getErrmsg() { return errmsg; }
    public void setErrmsg(String value) { this.errmsg = value; }

    public long getErrno() { return errno; }
    public void setErrno(long value) { this.errno = value; }
}
