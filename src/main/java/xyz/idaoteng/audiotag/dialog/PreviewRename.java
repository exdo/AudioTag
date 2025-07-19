package xyz.idaoteng.audiotag.dialog;

import atlantafx.base.theme.Styles;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import xyz.idaoteng.audiotag.UiCoordinator;
import xyz.idaoteng.audiotag.bean.AudioFileData;
import xyz.idaoteng.audiotag.bean.Filename;
import xyz.idaoteng.audiotag.util.Utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;

public class PreviewRename {
    private static final Stage STAGE = new Stage();
    private static final BorderPane BODY = new BorderPane();
    private static final Font FONT = new Font(13);

    private static final TableView<Filename> TABLE = new TableView<>();

    static {
        Label resultLabel = new Label("预览:");
        resultLabel.setPadding(new Insets(3, 0, 3, 2));
        resultLabel.setFont(FONT);

        TableColumn<Filename, String> oldFilenameColumn = new TableColumn<>("原文件名");
        oldFilenameColumn.setCellValueFactory(new PropertyValueFactory<>("oldName"));
        oldFilenameColumn.setPrefWidth(235);

        TableColumn<Filename, String> newFilenameColumn = new TableColumn<>("新文件名");
        newFilenameColumn.setCellValueFactory(new PropertyValueFactory<>("newName"));
        newFilenameColumn.setPrefWidth(235);

        TableColumn<Filename, CheckBox> checkColumn = new TableColumn<>("确定重命名");
        checkColumn.setCellValueFactory(new PropertyValueFactory<>("checkBox"));
        checkColumn.setStyle("-fx-alignment: CENTER");
        checkColumn.setPrefWidth(100);

        TABLE.getStyleClass().add(Styles.BORDERED);
        TABLE.getColumns().add(oldFilenameColumn);
        TABLE.getColumns().add(newFilenameColumn);
        TABLE.getColumns().add(checkColumn);

        Button confirm = new Button("开始重命名");
        confirm.setOnAction(event -> startRename());
        Button cancel = new Button("取消");
        cancel.setOnAction(event -> STAGE.close());

        HBox confirmAndCancel = new HBox(15);
        confirmAndCancel.setPadding(new Insets(15, 15, 15, 0));
        confirmAndCancel.setAlignment(Pos.CENTER_RIGHT);
        confirmAndCancel.getChildren().addAll(confirm, cancel);

        BODY.setTop(resultLabel);
        BODY.setCenter(TABLE);
        BODY.setBottom(confirmAndCancel);

        STAGE.initModality(Modality.APPLICATION_MODAL);
        Scene scene = new Scene(BODY, 600, 350);
        STAGE.setMinWidth(580);
        STAGE.setTitle("重命名预览");
        STAGE.setScene(scene);
    }

    private static void startRename() {
        ObservableList<Filename> list = TABLE.getItems();
        HashMap<String, String> failedPath_Reason = new HashMap<>(list.size());
        for (Filename f : list) {
            if (f.isNeedToRename()) {
                AudioFileData data = f.getMetaData();
                String message = rename(data.getAbsolutePath(), f.getFile().getAbsolutePath());
                if (message != null) {
                    failedPath_Reason.put(data.getAbsolutePath(), message);
                } else {
                    data.setFilename(f.getNewName());
                    data.setAbsolutePath(f.getFile().getAbsolutePath());
                }
            }
        }

        if (!failedPath_Reason.isEmpty()) {
            Alert alert = Utils.errorAlert("以下文件重命名失败");
            StringBuilder content = new StringBuilder();
            for (String path : failedPath_Reason.keySet()) {
                content.append(path).append(": ").append(failedPath_Reason.get(path)).append("\n");
            }
            TextArea textArea = new TextArea(content.toString());
            textArea.setEditable(false);
            textArea.setWrapText(true);
            textArea.setMaxHeight(500);
            alert.getDialogPane().setContent(textArea);
            alert.show();
        } else {
            UiCoordinator.showNotification("已全部重命名");
        }
        STAGE.close();
    }

    private static String rename(String originPath, String targetPath) {
        if (originPath == null || targetPath == null) throw new RuntimeException();

        if ("".equals(originPath) || "".equals(targetPath)) throw new RuntimeException();

        if (originPath.equals(targetPath)) return null;

        File origin = new File(originPath);
        if (!origin.exists()) return "无法找到源文件";

        File target = new File(targetPath);
        if (target.exists()) return "目标路径已有相同文件名的文件";

        try {
            Files.move(origin.toPath(), target.toPath(), StandardCopyOption.ATOMIC_MOVE);
            return null;
        } catch (IOException e) {
            return e.getMessage();
        }
    }

    public static void show(List<Filename> filenames) {
        TABLE.getItems().clear();
        TABLE.getItems().addAll(filenames);
        TABLE.refresh();
        STAGE.show();
    }
}
