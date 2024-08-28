package xyz.idaoteng.audiotag.dialog;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import xyz.idaoteng.audiotag.bean.AudioMetaData;
import xyz.idaoteng.audiotag.ImageInApp;
import xyz.idaoteng.audiotag.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Delete {
    public static List<AudioMetaData> show(List<AudioMetaData> dataList) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("确认删除文件");
        alert.setGraphic(ImageInApp.getDeleteIcon());
        alert.setHeaderText("确认删除所有选中的文件？\n注意：文件会直接删除而不是移至回收站");
        List<String> paths = dataList.stream().map(AudioMetaData::getAbsolutePath).toList();
        alert.setContentText(String.join("\n", paths));
        Optional<ButtonType> buttonType = alert.showAndWait();
        List<String> failed = new ArrayList<>(dataList.size());
        List<AudioMetaData> succeed = new ArrayList<>(dataList.size());
        if (buttonType.isPresent() && buttonType.get() == ButtonType.OK) {
            for (AudioMetaData metaData : dataList) {
                try {
                    File file = new File(metaData.getAbsolutePath());
                    if (file.delete()) {
                        succeed.add(metaData);
                    } else {
                        failed.add(metaData.getAbsolutePath());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        if (!failed.isEmpty()) {
            alert = Utils.generateBasicErrorAlert("以下文件删除失败");
            alert.setContentText(String.join("\n", failed));
            alert.show();
        }

        return succeed;
    }
}
