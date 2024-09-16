package xyz.idaoteng.audiotag.api.bean;

public class SongDetail {
    /**
     * 专辑图片
     */
    private String albumcover;
    /**
     * 专辑描述
     */
    private String albumdesc;
    /**
     * 专辑名称
     */
    private String albumname;
    /**
     * 是否免费
     */
    private boolean free;
    /**
     * 歌曲时长
     */
    private long interval;
    /**
     * 歌手
     */
    private Singer[] singer;
    /**
     * 歌曲mid
     */
    private String songmid;
    /**
     * 歌曲名称
     */
    private String songname;

    public String getAlbumcover() { return albumcover; }
    public void setAlbumcover(String value) { this.albumcover = value; }

    public String getAlbumdesc() { return albumdesc; }
    public void setAlbumdesc(String value) { this.albumdesc = value; }

    public String getAlbumname() { return albumname; }
    public void setAlbumname(String value) { this.albumname = value; }

    public boolean getFree() { return free; }
    public void setFree(boolean value) { this.free = value; }

    public long getInterval() { return interval; }
    public void setInterval(long value) { this.interval = value; }

    public Singer[] getSinger() { return singer; }
    public void setSinger(Singer[] value) { this.singer = value; }

    public String getSongmid() { return songmid; }
    public void setSongmid(String value) { this.songmid = value; }

    public String getSongname() { return songname; }
    public void setSongname(String value) { this.songname = value; }
}
