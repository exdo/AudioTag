package xyz.idaoteng.audiotag.dialog;

import javafx.scene.control.TextInputDialog;
import xyz.idaoteng.audiotag.util.ImageInApp;

import java.util.Optional;

public class PackageToAlbum {
    private static final TextInputDialog DIALOG = new TextInputDialog("");
    static {
        DIALOG.setTitle("设为同一专辑");
        DIALOG.setHeaderText("请输入专辑名");
        DIALOG.setGraphic(ImageInApp.getAlbumIcon());
        DIALOG.setContentText("专辑名：");
        DIALOG.getEditor().setMinWidth(235);
    }
    public static String show() {
        DIALOG.getEditor().setText(null);
        Optional<String> albumName = DIALOG.showAndWait();
        return albumName.orElse(null);
    }
}
