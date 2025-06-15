package xyz.idaoteng.audiotag.dialog;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import xyz.idaoteng.audiotag.ImageInApp;
import xyz.idaoteng.audiotag.Utils;
import xyz.idaoteng.audiotag.bean.AudioMetaData;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Delete {
    public static List<AudioMetaData> show(List<AudioMetaData> dataList) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setHeight(500);
        alert.setTitle("删除文件");
        alert.setGraphic(ImageInApp.getDeleteIcon());
        alert.setHeaderText("确认删除所有选中的文件？\n\t注意：文件会直接删除而不是移至回收站");

        List<String> paths = dataList.stream().map(AudioMetaData::getAbsolutePath).toList();
        TextArea textArea = new TextArea(String.join("\n", paths));
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setMaxHeight(300); // 限制最大高度
        alert.getDialogPane().setContent(textArea);

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
            TextArea ta = new TextArea(String.join("\n", failed) + "\n\n请尝试手动删除");
            ta.setEditable(false);
            ta.setWrapText(true);
            ta.setMaxHeight(300);
            alert.getDialogPane().setContent(ta);
            alert.show();
        }

        return succeed;
    }
}
