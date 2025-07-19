package xyz.idaoteng.audiotag.bean;

public class Configuration {
    // 是否需要重绘封面
    private Boolean needRetouch = false;
    // 重绘时的图片格式
    private String format = "jpg";
    // 重绘时的图片宽度
    private int width = 360;
    // 重绘时的图片高度
    private int height = 360;

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
}
