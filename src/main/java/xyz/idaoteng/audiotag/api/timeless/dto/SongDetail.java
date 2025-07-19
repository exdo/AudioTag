package xyz.idaoteng.audiotag.api.timeless.dto;

public class SongDetail {
    private String albumcover;
    private String albumname;
    private Singer[] singer;
    private String songmid;
    private String songname;

    public String getAlbumcover() {
        return albumcover;
    }

    public void setAlbumcover(String value) {
        this.albumcover = value;
    }

    public String getAlbumname() {
        return albumname;
    }

    public void setAlbumname(String value) {
        this.albumname = value;
    }

    public Singer[] getSinger() {
        return singer;
    }

    public void setSinger(Singer[] value) {
        this.singer = value;
    }

    public String getSongmid() {
        return songmid;
    }

    public void setSongmid(String value) {
        this.songmid = value;
    }

    public String getSongname() {
        return songname;
    }

    public void setSongname(String value) {
        this.songname = value;
    }
}
