package xyz.idaoteng.audiotag.bean;

import javafx.scene.control.CheckBox;

import java.io.File;

public class Filename {
    private final AudioMetaData metaData;
    private final File file;
    private final String oldName;
    private final String newName;
    private final CheckBox checkBox = new CheckBox();
    private boolean needToRename = true;

    public Filename(AudioMetaData data, File newFile) {
        metaData = data;
        file = newFile;
        oldName = data.getFilename();
        newName = newFile.getName();
        checkBox.setSelected(true);
        checkBox.setOnAction(event -> {
            needToRename = checkBox.isSelected();
        });
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
