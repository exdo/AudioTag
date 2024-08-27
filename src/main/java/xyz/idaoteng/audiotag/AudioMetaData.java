package xyz.idaoteng.audiotag;

public class AudioMetaData implements Cloneable {
    private String absolutePath;
    private String filename;
    private String artist = "";
    private String title = "";
    private String album = "";
    private String date = "";
    private String genre = "";
    private String track = "";
    private String comment = "";
    private String bitrate;
    private String length;
    private byte[] cover = null;

    private AudioMetaData backup = null;

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

    public void backup() {
        this.backup = clone();
    }

    public void restore() {
        if (this.backup != null) {
            this.absolutePath = backup.absolutePath;
            this.filename = backup.filename;
            this.artist = backup.artist;
            this.title = backup.title;
            this.album = backup.album;
            this.date = backup.date;
            this.genre = backup.genre;
            this.track = backup.track;
        }
    }

    public void clearBackup() {
        this.backup = null;
    }

    @Override
    public AudioMetaData clone() {
        try {
            return (AudioMetaData) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
