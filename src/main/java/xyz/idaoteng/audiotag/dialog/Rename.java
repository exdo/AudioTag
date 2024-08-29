package xyz.idaoteng.audiotag.dialog;

import javafx.scene.control.Alert;
import javafx.scene.control.TextInputDialog;
import xyz.idaoteng.audiotag.ImageInApp;
import xyz.idaoteng.audiotag.Utils;
import xyz.idaoteng.audiotag.bean.AudioMetaData;
import xyz.idaoteng.audiotag.component.Center;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Optional;

public class Rename {
    public static void show(AudioMetaData metaData) {
        File originalFile = new File(metaData.getAbsolutePath());
        String filenameWithoutExtension = Utils.getFilenameWithoutExtension(metaData.getFilename());

        TextInputDialog dialog = new TextInputDialog(filenameWithoutExtension);
        dialog.setTitle("确认重命名");
        dialog.setHeaderText("原文件名：" + filenameWithoutExtension);
        dialog.setContentText("新文件名：");
        dialog.setGraphic(ImageInApp.getRenameIcon());

        Optional<String> newName = dialog.showAndWait();
        if (newName.isPresent()) {
            if (!newName.get().equals(metaData.getFilename())) {
                String newFilename = newName.get() + "."  + Utils.getExtension(originalFile);
                File newFile = new File(originalFile.getParentFile(), newFilename);
                if (newFile.exists()) {
                    Alert alert = Utils.generateBasicErrorAlert("文件重命名失败");
                    alert.setContentText("文件："  + newFilename + "已存在");
                    alert.show();
                } else {
                    try {
                        Files.move(originalFile.toPath(), newFile.toPath());

                        metaData.setAbsolutePath(newFile.getAbsolutePath());
                        metaData.setFilename(newFilename);
                        Center.updateTableView(null);
                    } catch (IOException e) {
                        Alert alert = Utils.generateBasicErrorAlert("文件重命名失败");
                        alert.setContentText(e.getMessage());
                        alert.show();
                    }
                }
            }
        }
    }
}
