package xyz.idaoteng.audiotag.dialog;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import xyz.idaoteng.audiotag.UiCoordinator;
import xyz.idaoteng.audiotag.bean.AudioFileData;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class Tidy {
    private static final Stage STAGE = new Stage();
    private static final TextField TEXT_FIELD = new TextField();
    private static final RadioButton COPY = new RadioButton("复制");
    private static final RadioButton MOVE = new RadioButton("移动");

    private static final ArrayList<AudioFileData> DATA_LIST = new ArrayList<>();
    private static boolean isTidyByArtist;

    static {
        Label locationLabel = new Label("将文件整理至：");
        locationLabel.setFont(Font.font(13));

        TEXT_FIELD.setMinWidth(270);
        TEXT_FIELD.setMaxWidth(270);
        TEXT_FIELD.setEditable(false);

        Button selectFolder = new Button("选择文件夹");
        selectFolder.requestFocus();
        selectFolder.setOnAction(event -> {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setTitle("选择文件夹");
            File dir = directoryChooser.showDialog(STAGE);
            if (dir != null) {
                TEXT_FIELD.setText(dir.getAbsolutePath());
            }
        });
        HBox textAndSelect = new HBox(5);
        textAndSelect.getChildren().addAll(TEXT_FIELD, selectFolder);

        Label isCopyOrMoveLabel = new Label("对源文件的处理方式：");
        ToggleGroup toggleGroup = new ToggleGroup();
        COPY.setToggleGroup(toggleGroup);
        COPY.setSelected(true);
        MOVE.setToggleGroup(toggleGroup);
        HBox copyOrMove = new HBox(10);
        copyOrMove.getChildren().addAll(isCopyOrMoveLabel, COPY, MOVE);

        Button okButton = new Button("确定");
        okButton.setOnAction(event -> {
            if (isTidyByArtist) {
                tidyFileByArtist();
            } else {
                tidyFileByAlbum();
            }
            STAGE.close();
        });

        Button cancelButton = new Button("取消");
        cancelButton.setOnAction(event -> STAGE.close());

        HBox buttons = new HBox(50);
        buttons.setPadding(new Insets(0, 15, 0, 0));
        buttons.setAlignment(Pos.CENTER_RIGHT);
        buttons.getChildren().addAll(okButton, cancelButton);

        VBox body = new VBox(15);
        body.setPadding(new Insets(5, 0, 0, 15));
        body.getChildren().addAll(locationLabel, textAndSelect, copyOrMove, buttons);

        Scene scene = new Scene(body, 400, 185);
        STAGE.setScene(scene);
        STAGE.initModality(Modality.APPLICATION_MODAL);
        STAGE.setResizable(false);
    }

    private static void copyOrMoveFiles(HashMap<String, HashSet<AudioFileData>> files) {
        for (String artist : files.keySet()) {
            File artistOrAlbumDir = new File(TEXT_FIELD.getText(), artist);
            if (!artistOrAlbumDir.exists()) {
                boolean success = artistOrAlbumDir.mkdir();
                if (!success) {
                    String msg = "无法创建目录:" + TEXT_FIELD.getText() + File.separator + artist;
                    UiCoordinator.showNotification(msg);
                    continue;
                }
            }

            for (AudioFileData data : files.get(artist)) {
                Path source = Path.of(data.getAbsolutePath());
                Path target = Path.of(artistOrAlbumDir.getAbsolutePath(), data.getFilename());
                if (COPY.isSelected()) {
                    try {
                        Files.copy(source, target);
                    } catch (Exception e) {
                        String msg = data.getFilename() + " 复制失败: " + e.getMessage();
                        UiCoordinator.showNotification(msg);
                    }
                } else {
                    try {
                        Files.move(source, target);
                    } catch (Exception e) {
                        String msg = data.getFilename() + " 移动失败" + e.getMessage();
                        UiCoordinator.showNotification(msg);
                    }
                }
            }
        }
    }

    private static void tidyFileByArtist() {
        HashMap<String, HashSet<AudioFileData>> sameArtistFiles = new HashMap<>();
        for (AudioFileData selectedItem : DATA_LIST) {
            String artist = selectedItem.getArtist();
            sameArtistFiles.computeIfAbsent(artist, k -> new HashSet<>());
            sameArtistFiles.get(artist).add(selectedItem);
        }

        copyOrMoveFiles(sameArtistFiles);
    }

    private static void tidyFileByAlbum() {
        HashMap<String, HashSet<AudioFileData>> sameAlbumFiles = new HashMap<>();
        for (AudioFileData selectedItem : DATA_LIST) {
            String album = selectedItem.getAlbum();
            sameAlbumFiles.computeIfAbsent(album, k -> new HashSet<>());
            sameAlbumFiles.get(album).add(selectedItem);
        }

        copyOrMoveFiles(sameAlbumFiles);
    }

    public static void show(List<AudioFileData> dataList, boolean isTidyByArtist) {
        DATA_LIST.clear();
        DATA_LIST.addAll(dataList);
        Tidy.isTidyByArtist = isTidyByArtist;
        if (isTidyByArtist) {
            STAGE.setTitle("将同一歌手的文件放置在同一文件夹");
        } else {
            STAGE.setTitle("将同一专辑的文件放置在同一文件夹");
        }
        STAGE.showAndWait();
    }
}
