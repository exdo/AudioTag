package xyz.idaoteng.audiotag.api.migu.dto;

public class MiguSong {
    private String songName;
    private String album;
    private String img1;
    private String img2;
    private String img3;
    private SingerList[] singerList;
    private Lyric ext;

    public String getSongName() {
        return songName;
    }

    public void setSongName(String songName) {
        this.songName = songName;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getImg1() {
        return img1;
    }

    public void setImg1(String img1) {
        this.img1 = img1;
    }

    public String getImg2() {
        return img2;
    }

    public void setImg2(String img2) {
        this.img2 = img2;
    }

    public String getImg3() {
        return img3;
    }

    public void setImg3(String img3) {
        this.img3 = img3;
    }

    public SingerList[] getSingerList() {
        return singerList;
    }

    public void setSingerList(SingerList[] singerList) {
        this.singerList = singerList;
    }

    public Lyric getExt() {
        return ext;
    }

    public void setExt(Lyric ext) {
        this.ext = ext;
    }
}
