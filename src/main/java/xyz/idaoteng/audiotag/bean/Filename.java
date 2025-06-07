package xyz.idaoteng.audiotag.bean;

import javafx.scene.control.CheckBox;

import java.io.File;

// 批量重命名时生成的预览的文件名
public class Filename {
    // 原始的音频元数据
    private final AudioMetaData metaData;
    // 重命名后的文件(此时文件尚未创建)
    private final File file;
    // 原始的文件名
    private final String oldName;
    // 新的文件名
    private final String newName;
    // 勾选框
    private final CheckBox checkBox = new CheckBox();
    // 是否需要重命名
    private boolean needToRename = true;

    public Filename(AudioMetaData data, File newFile) {
        metaData = data;
        file = newFile;
        oldName = data.getFilename();
        newName = newFile.getName();
        checkBox.setSelected(true);
        checkBox.setOnAction(event -> needToRename = checkBox.isSelected());
    }

    public AudioMetaData getMetaData() {
        return metaData;
    }

    public String getOldName() {
        return oldName;
    }

    public String getNewName() {
        return newName;
    }

    public CheckBox getCheckBox() {
        return checkBox;
    }

    public boolean isNeedToRename() {
        return needToRename;
    }

    public File getFile() {
        return file;
    }
}
