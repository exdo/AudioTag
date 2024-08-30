package xyz.idaoteng.audiotag.dialog;

import javafx.scene.control.TextInputDialog;
import xyz.idaoteng.audiotag.ImageInApp;

import java.util.Optional;

public class PackageToAlbum {
    public static String show() {
        TextInputDialog dialog = new TextInputDialog("");
        dialog.setTitle("设为同一专辑");
        dialog.setHeaderText("请输入专辑名");
        dialog.setGraphic(ImageInApp.getAlbumIcon());
        dialog.setContentText("专辑名：");
        dialog.getEditor().setMinWidth(235);

        Optional<String> albumName = dialog.showAndWait();
        return albumName.orElse(null);
    }
}
