package xyz.idaoteng.audiotag;

public class AudioMetaData {
    private String absolutePath;
    private String filename;
    private String artist = "";
    private String title = "";
    private String album = "";
    private String genre = "";
    private String track = "";
    private String bitrate;
    private String length;
    private byte[] cover;

    public enum Editable {
        ARTIST, TITLE, ALBUM, GENRE, TRACK
    }

    public void set(Editable type, String value) {
        switch (type) {
            case ARTIST -> this.artist = value;
            case TITLE -> this.title = value;
            case ALBUM -> this.album = value;
            case GENRE -> this.genre = value;
            case TRACK -> this.track = value;
        }
    }

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
