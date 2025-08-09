package xyz.idaoteng.audiotag.bean;

public class AppConfiguration {
    // 是否需要重绘封面
    private Boolean needRetouch = false;
    // 重绘时的图片格式
    private String format = "jpg";
    // 重绘时的图片宽度
    private int width = 360;
    // 重绘时的图片高度
    private int height = 360;
    // 歌词保存策略
    private boolean writeInTag = true; // 写入标签中
    private boolean writeInFile = true; // 写入 .lrc 文件中
    private boolean createLyricsFolder = false; // 将歌词文件放在 lyrics 文件夹中（否则放在歌曲文件所在文件夹）

    public Boolean getNeedRetouch() {
        return needRetouch;
    }

    public void setNeedRetouch(Boolean needRetouch) {
        this.needRetouch = needRetouch;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public boolean isWriteInTag() {
        return writeInTag;
    }

    public void setWriteInTag(boolean writeInTag) {
        this.writeInTag = writeInTag;
    }

    public boolean isWriteInFile() {
        return writeInFile;
    }

    public void setWriteInFile(boolean writeInFile) {
        this.writeInFile = writeInFile;
    }

    public boolean isCreateLyricsFolder() {
        return createLyricsFolder;
    }

    public void setCreateLyricsFolder(boolean createLyricsFolder) {
        this.createLyricsFolder = createLyricsFolder;
    }
}
