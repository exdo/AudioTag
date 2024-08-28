package xyz.idaoteng.audiotag.bean;

import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.CheckBox;

import java.io.File;

public class Filename {
    private final AudioMetaData metaData;
    private final File file;
    private final SimpleStringProperty oldName;
    private final SimpleStringProperty newName;
    private final CheckBox checkBox = new CheckBox();
    private boolean needToRename = true;

    public Filename(AudioMetaData data, File newFile) {
        metaData = data;
        file = newFile;
        oldName = new SimpleStringProperty(data.getFilename());
        newName = new SimpleStringProperty(newFile.getName());
        checkBox.setSelected(true);
        checkBox.setOnAction(event -> {
            needToRename = checkBox.isSelected();
        });
    }

    public AudioMetaData getMetaData() {
        return metaData;
    }

    public String getOldName() {
        return oldName.get();
    }

    public SimpleStringProperty oldNameProperty() {
        return oldName;
    }

    public String getNewName() {
        return newName.getName();
    }

    public SimpleStringProperty newNameProperty() {
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
