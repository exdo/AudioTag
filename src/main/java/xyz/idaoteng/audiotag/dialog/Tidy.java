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
import xyz.idaoteng.audiotag.constant.DaemonExecutor;
import xyz.idaoteng.audiotag.util.Utils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

public class Tidy {
    private static final Stage STAGE = new Stage();
    private static final TextField TEXT_FIELD = new TextField();
    private static final RadioButton COPY = new RadioButton("复制");
    private static final RadioButton MOVE = new RadioButton("移动");
    private static final ProgressBar PROGRESS_BAR = new ProgressBar(0.0);

    private static final ArrayList<AudioFileData> DATA_LIST = new ArrayList<>();
    private static boolean isTidyByArtist;
    private static File lastSelectedDir = null;
    private static boolean missionCanceled = false;
    private static final ArrayList<String> ERR_MESSAGE = new ArrayList<>();


    static {
        Label locationLabel = new Label("将文件整理至：");
        locationLabel.setFont(Font.font(13));

        TEXT_FIELD.setMinWidth(270);
        TEXT_FIELD.setMaxWidth(270);
        TEXT_FIELD.setEditable(false);

        Button selectFolder = new Button("选择文件夹");
        selectFolder.requestFocus();
        selectFolder.setOnAction(event -> chooseDir());
        HBox textAndSelect = new HBox(5);
        textAndSelect.getChildren().addAll(TEXT_FIELD, selectFolder);

        Label isCopyOrMoveLabel = new Label("对源文件的处理方式：");
        ToggleGroup toggleGroup = new ToggleGroup();
        COPY.setToggleGroup(toggleGroup);
        COPY.setSelected(true);
        MOVE.setToggleGroup(toggleGroup);
        HBox copyOrMove = new HBox(10);
        copyOrMove.getChildren().addAll(isCopyOrMoveLabel, COPY, MOVE);

        PROGRESS_BAR.setPrefWidth(380);
        PROGRESS_BAR.progressProperty().addListener((ob, o, n) -> {
            if (n.doubleValue() == 1.0) {
                STAGE.close();
            }
        });

        Button okButton = new Button("确定");
        okButton.setOnAction(event -> okButtonEventHandle(selectFolder));

        Button cancelButton = new Button("取消");
        cancelButton.setOnAction(event -> {
            missionCanceled = true;
            STAGE.close();
        });

        HBox buttons = new HBox(50);
        buttons.setPadding(new Insets(0, 15, 0, 0));
        buttons.setAlignment(Pos.CENTER_RIGHT);
        buttons.getChildren().addAll(okButton, cancelButton);

        VBox body = new VBox(15);
        body.setPadding(new Insets(5, 0, 0, 15));
        body.getChildren().addAll(locationLabel, textAndSelect, copyOrMove, PROGRESS_BAR, buttons);

        Scene scene = new Scene(body, 400, 210);
        STAGE.setScene(scene);
        STAGE.initModality(Modality.APPLICATION_MODAL);
        STAGE.setResizable(false);
    }

    private static void chooseDir() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("选择文件夹");

        if (lastSelectedDir != null) {
            directoryChooser.setInitialDirectory(lastSelectedDir);
        }

        File dir = directoryChooser.showDialog(STAGE);
        if (dir != null) {
            TEXT_FIELD.setText(dir.getAbsolutePath());
            lastSelectedDir = dir;
        }
    }

    private static void okButtonEventHandle(Button selectFolder) {
        if (TEXT_FIELD.getText() == null || TEXT_FIELD.getText().isBlank()) {
            selectFolder.fire();
            return;
        }

        if (isTidyByArtist) {
            tidyFileByArtist();
        } else {
            tidyFileByAlbum();
        }
    }

    private static final ExecutorService IO_SERVICE = DaemonExecutor.TIME_CONSUMING_TASK_EXECUTOR;
    private static void copyOrMoveFiles(HashMap<String, HashSet<AudioFileData>> sameArtistOrAlbumFiles) {
        CountDownLatch latch = new CountDownLatch(sameArtistOrAlbumFiles.size());
        for (String artistOrAlbum : sameArtistOrAlbumFiles.keySet()) {
            if (missionCanceled) {
                while (latch.getCount() != 0) {
                    latch.countDown();
                }
                break;
            }

            Optional<String> artisOrAlbumDir = createArtisOrAlbumDir(artistOrAlbum);
            HashSet<AudioFileData> fileData = sameArtistOrAlbumFiles.get(artistOrAlbum);
            artisOrAlbumDir.ifPresent(dir -> IO_SERVICE.submit(() -> copyOrMoveTask(fileData, dir)));
            latch.countDown();
            PROGRESS_BAR.setProgress(1.0 - (double) latch.getCount() / sameArtistOrAlbumFiles.size());
        }

        try {
            latch.await();

            if (!ERR_MESSAGE.isEmpty()) {
                Alert alert = Utils.errorAlert("出现以下错误：");
                StringBuilder content = new StringBuilder();
                for (String path : ERR_MESSAGE) {
                    content.append(path).append("\n");
                }
                TextArea textArea = new TextArea(content.toString());
                textArea.setEditable(false);
                textArea.setWrapText(true);
                textArea.setMaxHeight(500);
                alert.getDialogPane().setContent(textArea);
                alert.show();
            } else {
                UiCoordinator.showNotification("整理完毕");
            }
        } catch (InterruptedException e) {
            UiCoordinator.showNotification("任务异常中断");
        }
    }

    private static Optional<String> createArtisOrAlbumDir(String artistOrAlbumName) {
        File artistOrAlbumDir = new File(TEXT_FIELD.getText(), artistOrAlbumName);
        if (!artistOrAlbumDir.exists()) {
            boolean success = artistOrAlbumDir.mkdir();
            if (!success) {
                String msg = "无法创建目录:" + TEXT_FIELD.getText() + File.separator + artistOrAlbumName;
                UiCoordinator.showNotification(msg);
                return Optional.empty();
            }
        }
        return Optional.of(artistOrAlbumDir.getAbsolutePath());
    }

    private static void copyOrMoveTask(HashSet<AudioFileData> fileData, String targetDir) {
        for (AudioFileData data : fileData) {
            Path source = Path.of(data.getAbsolutePath());
            String targetName = data.getFilename() +  "." + data.getFormat();
            Path target = Path.of(targetDir, targetName);
            if (COPY.isSelected()) {
                try {
                    Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
                } catch (Exception e) {
                    ERR_MESSAGE.add(data.getFilename() + " 复制失败: \n" + e.getMessage());
                }
            } else {
                try {
                    Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
                } catch (Exception e) {
                    ERR_MESSAGE.add(data.getFilename() + " 移动失败：\n" + e.getMessage()) ;
                }
            }
        }
    }

    private static void tidyFileByArtist() {
        HashMap<String, HashSet<AudioFileData>> sameArtistFiles = new HashMap<>();
        for (AudioFileData data : DATA_LIST) {
            String artist = data.getArtist();
            if (artist == null || artist.isBlank()) continue;
            sameArtistFiles.computeIfAbsent(artist, k -> new HashSet<>());
            sameArtistFiles.get(artist).add(data);
        }

        copyOrMoveFiles(sameArtistFiles);
    }

    private static void tidyFileByAlbum() {
        HashMap<String, HashSet<AudioFileData>> sameAlbumFiles = new HashMap<>();
        for (AudioFileData data : DATA_LIST) {
            String album = data.getAlbum();
            if (album == null || album.isBlank()) continue;
            sameAlbumFiles.computeIfAbsent(album, k -> new HashSet<>());
            sameAlbumFiles.get(album).add(data);
        }

        copyOrMoveFiles(sameAlbumFiles);
    }

    public static void show(List<AudioFileData> dataList, boolean isTidyByArtist) {
        DATA_LIST.clear();
        DATA_LIST.addAll(dataList);
        Tidy.isTidyByArtist = isTidyByArtist;
        PROGRESS_BAR.setProgress(0);
        missionCanceled = false;
        ERR_MESSAGE.clear();
        if (isTidyByArtist) {
            STAGE.setTitle("将同一歌手的文件放置在同一文件夹");
        } else {
            STAGE.setTitle("将同一专辑的文件放置在同一文件夹");
        }
        STAGE.showAndWait();
    }
}
