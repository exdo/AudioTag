package xyz.idaoteng.audiotag.bean;

import xyz.idaoteng.audiotag.constant.DefaultColumnOrder;

import java.util.ArrayList;
import java.util.HashMap;

public class Preferences {
    // 上次点击 打开文件 按钮时选择的文件夹路径
    private String lastSelectedFile = System.getProperty("user.home");
    // 上次点击 打开文件夹 按钮时选择的文件夹路径
    private String lastSelectedFolder = System.getProperty("user.home");
    // 上次点击 更换 按钮时选择的图片路径
    private String lastSelectedImage = System.getProperty("user.home");
    // 上次点击 提取 按钮时选择的图片保存路径
    private String imageSavingPath = System.getProperty("user.home");
    // 当前表格中显示的文件路径
    private ArrayList<String> currentPaths = new ArrayList<>();
    // 表头的顺序
    private HashMap<Integer, String> columnsOrder = DefaultColumnOrder.defaultOrder();
    // 是否需要重绘封面（重绘的图片统一为 250 * 250 的 jpg）
    private Boolean retouchCover = false;

    public String getLastSelectedFile() {
        return lastSelectedFile;
    }

    public void setLastSelectedFile(String lastSelectedFile) {
        this.lastSelectedFile = lastSelectedFile;
    }

    public String getLastSelectedFolder() {
        return lastSelectedFolder;
    }

    public void setLastSelectedFolder(String lastSelectedFolder) {
        this.lastSelectedFolder = lastSelectedFolder;
    }

    public String getLastSelectedImage() {
        return lastSelectedImage;
    }

    public void setLastSelectedImage(String lastSelectedImage) {
        this.lastSelectedImage = lastSelectedImage;
    }

    public String getImageSavingPath() {
        return imageSavingPath;
    }

    public void setImageSavingPath(String imageSavingPath) {
        this.imageSavingPath = imageSavingPath;
    }

    public ArrayList<String> getCurrentPaths() {
        return currentPaths;
    }

    public void setCurrentPaths(ArrayList<String> currentPaths) {
        this.currentPaths = currentPaths;
    }

    public HashMap<Integer, String> getColumnsOrder() {
        return columnsOrder;
    }

    public void setColumnsOrder(HashMap<Integer, String> columnsOrder) {
        this.columnsOrder = columnsOrder;
    }

    public Boolean getRetouchCover() {
        return retouchCover;
    }

    public void setRetouchCover(Boolean retouchCover) {
        this.retouchCover = retouchCover;
    }
}
