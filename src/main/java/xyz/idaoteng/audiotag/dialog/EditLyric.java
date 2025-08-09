package xyz.idaoteng.audiotag.dialog;

import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import xyz.idaoteng.audiotag.Session;
import xyz.idaoteng.audiotag.UiCoordinator;
import xyz.idaoteng.audiotag.bean.AppConfiguration;
import xyz.idaoteng.audiotag.bean.AudioFileData;
import xyz.idaoteng.audiotag.constant.EditableTag;
import xyz.idaoteng.audiotag.jaudiotagger.AudioFileWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

public class EditLyric {
    private static final TextArea TEXT_AREA = new TextArea();
    private static final Stage STAGE = new Stage();

    private static AudioFileData data;

    static {
        TEXT_AREA.setPrefWidth(450);
        TEXT_AREA.setPrefHeight(600);

        Button searchButton = new Button("搜索");
        searchButton.setOnAction(EditLyric::showSearchLyricDialog);

        Button cancelButton = new Button("取消");
        cancelButton.setOnAction(event -> STAGE.close());

        Button okButton = new Button("保存");
        okButton.setOnAction(EditLyric::saveLyric);

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

    private static void showSearchLyricDialog(ActionEvent event) {
        Consumer<String> consumer = lyric -> {
            if (lyric != null) {
                TEXT_AREA.setText(lyric);
            }
        };
        SearchLyric.show(data.getTitle(), data.getArtist(), data.getAlbum(), consumer);
    }

    private static void saveLyric(ActionEvent event) {
        STAGE.close();

        data.setLyric(TEXT_AREA.getText());

        AppConfiguration config = Session.getConfig();
        if (config.isWriteInTag()) {
            AudioFileWriter.updateTag(data, EditableTag.LYRIC);
            UiCoordinator.showNotification("歌词 已保存进标签");
        }
        if (config.isWriteInFile()) {
            File parent = new File(data.getAbsolutePath()).getParentFile();
            if (config.isCreateLyricsFolder()) {
                saveInLyricsFolder(new File(parent, "lyrics"));
            } else {
                writeLyric(parent);
            }
        }

        UiCoordinator.refreshAsideData();
    }

    private static void saveInLyricsFolder(File lyricsDir) {
        if (!lyricsDir.exists() || !lyricsDir.isDirectory()) {
            if (lyricsDir.mkdir()) {
                writeLyric(lyricsDir);
            } else {
                UiCoordinator.showNotification("无法创建 lyrics 文件夹");
            }
        }
    }

    private static void writeLyric(File lyricsDir) {
        File lrc = new File(lyricsDir, data.getFilename() + ".lrc");
        try (FileWriter writer = new FileWriter(lrc, StandardCharsets.UTF_8)) {
            writer.write(TEXT_AREA.getText());
            writer.flush();
        } catch (IOException e) {
            UiCoordinator.showNotification("无法创建歌词文件");
        }
        UiCoordinator.showNotification("歌词文件已更新");
    }

    public static void show(AudioFileData data) {
        EditLyric.data = data;
        TEXT_AREA.clear();
        TEXT_AREA.setText(data.getLyric());
        STAGE.show();
    }

}
