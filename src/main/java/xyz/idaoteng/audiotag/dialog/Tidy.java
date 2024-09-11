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
import xyz.idaoteng.audiotag.bean.AudioMetaData;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class Tidy {
    private static final Stage STAGE = new Stage();
    private static final VBox BODY = new VBox(15);
    private static final Label LABEL = new Label("将文件整理至：");
    private static final TextField TEXT_FIELD = new TextField();
    private static final RadioButton COPY = new RadioButton("复制");
    private static final RadioButton MOVE = new RadioButton("移动");
    private static final Button OK_BUTTON = new Button("确定");
    private static final Button CANCEL_BUTTON = new Button("取消");

    private static final ArrayList<AudioMetaData> DATA_LIST = new ArrayList<>();
    private static boolean isTidyByArtist;

    static {
        LABEL.setFont(Font.font(13));

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

        Label isCopyOrMove = new Label("对源文件的处理方式：");
        ToggleGroup toggleGroup = new ToggleGroup();
        COPY.setToggleGroup(toggleGroup);
        COPY.setSelected(true);
        MOVE.setToggleGroup(toggleGroup);
        HBox copyOrMove = new HBox(10);
        copyOrMove.getChildren().addAll(isCopyOrMove, COPY, MOVE);

        OK_BUTTON.setOnAction(event -> {
            if (isTidyByArtist) {
                tidyFileByArtist();
            } else {
                tidyFileByAlbum();
            }
            STAGE.close();
        });
        CANCEL_BUTTON.setOnAction(event -> STAGE.close());
        HBox buttons = new HBox(50);
        buttons.setAlignment(Pos.CENTER);
        buttons.getChildren().addAll(OK_BUTTON, CANCEL_BUTTON);

        BODY.setPadding(new Insets(5, 0, 0, 15));
        BODY.getChildren().addAll(LABEL, textAndSelect, copyOrMove, buttons);

        Scene scene = new Scene(BODY, 390, 140);
        STAGE.setScene(scene);
        STAGE.initModality(Modality.APPLICATION_MODAL);
        STAGE.setResizable(false);
    }

    private static void tidyFileByArtist() {
        HashMap<String, HashSet<AudioMetaData>> sameArtistFiles = new HashMap<>();
        for (AudioMetaData selectedItem : DATA_LIST) {
            String artist = selectedItem.getArtist();
            sameArtistFiles.computeIfAbsent(artist, k -> new HashSet<>());
            sameArtistFiles.get(artist).add(selectedItem);
        }

        copyOrMoveFiles(sameArtistFiles);
    }

    public static void copyOrMoveFiles(HashMap<String, HashSet<AudioMetaData>> files) {
        for (String artist : files.keySet()) {
            File artistDir = new File(TEXT_FIELD.getText(), artist);
            if (!artistDir.exists()) {
                boolean success = artistDir.mkdir();
                if (!success) {
                    System.out.println("创建目录 " + artist + " 失败");
                    continue;
                }
            }

            for (AudioMetaData data : files.get(artist)) {
                Path source = Path.of(data.getAbsolutePath());
                Path target = Path.of(artistDir.getAbsolutePath(), data.getFilename());
                if (COPY.isSelected()) {
                    try {
                        Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e) {
                        e.printStackTrace();
                        System.out.println("文件 " + data.getFilename() + " 复制失败");
                    }
                } else {
                    try {
                        Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e) {
                        e.printStackTrace();
                        System.out.println("文件 " + data.getFilename() + " 移动失败");
                    }
                }
            }
        }
    }

    private static void tidyFileByAlbum() {
        HashMap<String, HashSet<AudioMetaData>> sameAlbumFiles = new HashMap<>();
        for (AudioMetaData selectedItem : DATA_LIST) {
            String album = selectedItem.getAlbum();
            sameAlbumFiles.computeIfAbsent(album, k -> new HashSet<>());
            sameAlbumFiles.get(album).add(selectedItem);
        }

        copyOrMoveFiles(sameAlbumFiles);
    }

    public static void show(List<AudioMetaData> dataList, boolean isTidyByArtist) {
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
