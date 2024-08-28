package xyz.idaoteng.audiotag.dialog;

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
import xyz.idaoteng.audiotag.Utils;
import xyz.idaoteng.audiotag.bean.AudioMetaData;
import xyz.idaoteng.audiotag.bean.Filename;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class Preview {
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
        checkColumn.setPrefWidth(80);

        TABLE.getColumns().add(oldFilenameColumn);
        TABLE.getColumns().add(newFilenameColumn);
        TABLE.getColumns().add(checkColumn);
        TABLE.setOnMouseClicked(event -> TABLE.refresh());

        Button confirm = new Button("开始重命名");
        configConfirmButton(confirm);
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
        Scene scene = new Scene(BODY, 580, 350);
        STAGE.setMinWidth(580);
        STAGE.setTitle("预览");
        STAGE.setScene(scene);
    }

    private static void configConfirmButton(Button confirm) {
        confirm.setOnAction(event -> {
            ObservableList<Filename> list = TABLE.getItems();
            List<String> failedPaths = new ArrayList<>(list.size());
            for (Filename f : list) {
                if (f.isNeedToRename()) {
                    AudioMetaData metaData = f.getMetaData();
                    if (!rename(metaData, f.getFile())) {
                        failedPaths.add(metaData.getAbsolutePath());
                    }
                }
            }

            if (!failedPaths.isEmpty()) {
                Alert alert = Utils.generateBasicErrorAlert("以下文件重命名失败");
                alert.setContentText(String.join("\n", failedPaths));
                alert.show();
            }

            STAGE.close();
        });
    }

    private static boolean rename(AudioMetaData data, File newFile) {
        File originalFile = new File(data.getAbsolutePath());

        if (data.getFilename().equals(newFile.getName())) {
            return true;
        }

        try {
            Files.move(originalFile.toPath(), newFile.toPath());
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static void show(List<Filename> filenames) {
        TABLE.getItems().clear();
        TABLE.getItems().addAll(filenames);
        TABLE.refresh();
        STAGE.show();
    }
}
