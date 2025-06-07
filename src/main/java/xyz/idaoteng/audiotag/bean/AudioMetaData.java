package xyz.idaoteng.audiotag.bean;

public class AudioMetaData {
    // 文件的绝对路径
    private String absolutePath;
    // 文件名(不包含拓展名)
    private String filename;

    // 可编辑的元数据标签
    // 艺术家
    private String artist = "";
    // 标题
    private String title = "";
    // 专辑
    private String album = "";
    //专辑日期
    private String date = "";
    // 风格
    private String genre = "";
    // 音轨序号
    private String track = "";
    // 评论(备注)
    private String comment = "";
    // 封面
    private byte[] cover = null;


    // 不可编辑的元数据标签
    // 比特率
    private String bitrate;
    // 时长
    private String length;

    public String getAbsolutePath() {
        return absolutePath;
    }

    public void setAbsolutePath(String absolutePath) {
        this.absolutePath = absolutePath;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getTrack() {
        return track;
    }

    public void setTrack(String track) {
        this.track = track;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getBitrate() {
        return bitrate;
    }

    public void setBitrate(String bitrate) {
        this.bitrate = bitrate;
    }

    public String getLength() {
        return length;
    }

    public void setLength(String length) {
        this.length = length;
    }

    public byte[] getCover() {
        return cover;
    }

    public void setCover(byte[] cover) {
        this.cover = cover;
    }
}
