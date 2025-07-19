package xyz.idaoteng.audiotag.bean;

import java.util.ArrayList;
import java.util.HashMap;

public class HistorySession {
    // 上次打开的文件夹
    private String lastSelectedFolder;
    // 上次选择的图片所在的文件夹
    private String lastSelectedImageFolder;
    // 上次的图片保存路径
    private String lastImageSavingPath;
    // 上次表格中显示的条目的文件路径
    private ArrayList<String> lastOpenedPaths;
    // 表头的顺序
    private HashMap<Integer, String> columnsOrder;
    // 表头的可视状态
    private HashMap<String, Boolean> columnsVisibleStatus;

    public String getLastSelectedFolder() {
        return lastSelectedFolder;
    }

    public void setLastSelectedFolder(String lastSelectedFolder) {
        this.lastSelectedFolder = lastSelectedFolder;
    }

    public String getLastSelectedImageFolder() {
        return lastSelectedImageFolder;
    }

    public void setLastSelectedImageFolder(String lastSelectedImageFolder) {
        this.lastSelectedImageFolder = lastSelectedImageFolder;
    }

    public String getLastImageSavingPath() {
        return lastImageSavingPath;
    }

    public void setLastImageSavingPath(String lastImageSavingPath) {
        this.lastImageSavingPath = lastImageSavingPath;
    }

    public ArrayList<String> getLastOpenedPaths() {
        return lastOpenedPaths;
    }

    public void setLastOpenedPaths(ArrayList<String> lastOpenedPaths) {
        this.lastOpenedPaths = lastOpenedPaths;
    }

    public HashMap<Integer, String> getColumnsOrder() {
        return columnsOrder;
    }

    public void setColumnsOrder(HashMap<Integer, String> columnsOrder) {
        this.columnsOrder = columnsOrder;
    }

    public HashMap<String, Boolean> getColumnsVisibleStatus() {
        return columnsVisibleStatus;
    }

    public void setColumnsVisibleStatus(HashMap<String, Boolean> columnsVisibleStatus) {
        this.columnsVisibleStatus = columnsVisibleStatus;
    }
}
