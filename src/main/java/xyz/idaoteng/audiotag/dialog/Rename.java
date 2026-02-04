package xyz.idaoteng.audiotag.dialog;

import javafx.scene.control.TextInputDialog;
import xyz.idaoteng.audiotag.util.ImageInApp;

import java.util.Optional;

public class Rename {
    private static final TextInputDialog DIALOG = new TextInputDialog("");
    static {
        DIALOG.setTitle("重命名");
        DIALOG.setHeaderText("重命名");
        DIALOG.setGraphic(ImageInApp.getRenameIcon());
        DIALOG.setContentText("新文件名：");
        DIALOG.getEditor().setMinWidth(235);
    }
    public static String show(String oldName) {
        DIALOG.getEditor().setText(oldName);
        Optional<String> albumName = DIALOG.showAndWait();
        return albumName.orElse(oldName);
    }
}
