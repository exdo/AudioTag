package xyz.idaoteng.audiotag.dialog;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import xyz.idaoteng.audiotag.UiCoordinator;
import xyz.idaoteng.audiotag.bean.AudioFileData;
import xyz.idaoteng.audiotag.constant.EditableTag;
import xyz.idaoteng.audiotag.jaudiotagger.AudioFileWriter;

import java.util.function.Consumer;

public class EditLyric {
    private static final TextArea TEXT_AREA = new TextArea();
    private static final Stage STAGE = new Stage();

    private static AudioFileData data;

    static {
        TEXT_AREA.setPrefWidth(450);
        TEXT_AREA.setPrefHeight(600);

        Button searchButton = new Button("搜索");
        searchButton.setOnAction(event -> {
            Consumer<String> consumer = lyric -> {
                if (lyric != null) {
                    TEXT_AREA.setText(lyric);
                }
            };
            SearchLyric.show(data.getTitle(), data.getArtist(), data.getAlbum(), consumer);
        });

        Button cancelButton = new Button("取消");
        cancelButton.setOnAction(event -> STAGE.close());

        Button okButton = new Button("确认修改");
        okButton.setOnAction(event -> {
            STAGE.close();
            data.setLyric(TEXT_AREA.getText());
            AudioFileWriter.updateTag(data, EditableTag.LYRIC);
            UiCoordinator.showNotification("歌词 已更新");
            UiCoordinator.refreshAsideData();
        });

        HBox buttons = new HBox(15, searchButton, cancelButton, okButton);
        buttons.setAlignment(Pos.CENTER_RIGHT);
        buttons.setPadding(new Insets(0, 5, 15, 0));

        VBox vBox = new VBox(15, TEXT_AREA, buttons);
        Scene scene = new Scene(vBox);
        STAGE.setScene(scene);
        STAGE.setTitle("歌词");
        STAGE.initModality(Modality.APPLICATION_MODAL);
        STAGE.sizeToScene();
        STAGE.setResizable(false);
    }

    public static void show(AudioFileData data) {
        EditLyric.data = data;
        TEXT_AREA.clear();
        TEXT_AREA.setText(data.getLyric());
        STAGE.show();
    }

}
