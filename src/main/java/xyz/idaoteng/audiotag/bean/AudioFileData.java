package xyz.idaoteng.audiotag.bean;

public class AudioFileData {
    // 文件信息
    // 文件的绝对路径
    private String absolutePath;
    // 文件名(不包含拓展名)
    private String filename;
    // 音频格式（文件拓展名)
    private String format;
    // 音频文件大小
    private String size;

    // 可编辑的标签数据
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
    // 备注
    private String comment = "";
    // 封面
    private byte[] cover = null;
    // 歌词
    private String lyric = "";

    // 不可编辑数据
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

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
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

    public byte[] getCover() {
        return cover;
    }

    public void setCover(byte[] cover) {
        this.cover = cover;
    }

    public String getLyric() {
        return lyric;
    }

    public void setLyric(String lyric) {
        this.lyric = lyric;
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
}
