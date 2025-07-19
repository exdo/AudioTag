package xyz.idaoteng.audiotag.dialog;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import xyz.idaoteng.audiotag.api.MusicApi;
import xyz.idaoteng.audiotag.api.migu.MiguMusicApi;
import xyz.idaoteng.audiotag.api.netease.NetEaseMusicApi;
import xyz.idaoteng.audiotag.api.timeless.TimelessApi;
import xyz.idaoteng.audiotag.util.ImageInApp;
import xyz.idaoteng.audiotag.util.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.function.Consumer;

public class SearchLyric {
    // 常量定义
    private static final double WAITING_PAGE_PROGRESS_SIZE = 75.0;
    private static final int PANE_SPACING = 8;
    private static final int PADDING_SMALL = 10;
    private static final int PADDING_MEDIUM = 15;
    private static final int PADDING_LARGE = 20;

    private static final VBox WAITING_PAGE = new VBox(PADDING_MEDIUM);
    private static final ToggleGroup TOGGLE_GROUP = new ToggleGroup();

    private static final MusicApi QQ_MUSIC_API = new TimelessApi();
    private static final MusicApi NET_EASE_API = new NetEaseMusicApi();
    private static final MusicApi MIGU_MUSIC_API = new MiguMusicApi();

    private static final Stage STAGE = new Stage();
    private static final VBox BODY = new VBox(PANE_SPACING);
    private static final Scene SCENE = new Scene(BODY);
    private static final ExecutorService LYRIC_SEARCH_EXECUTOR;

    static {
        LYRIC_SEARCH_EXECUTOR = Executors.newFixedThreadPool(4, new ThreadFactory() {
            private int counter = 0;

            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r, "LyricSearchThread-" + counter++);
                thread.setDaemon(true);
                return thread;
            }
        });

        BODY.setMaxHeight(600);
        BODY.setPadding(new Insets(0, PADDING_MEDIUM, 0, PADDING_MEDIUM));
        STAGE.setScene(SCENE);
        STAGE.setTitle("搜索歌词");
        STAGE.setResizable(false);
        STAGE.initModality(Modality.APPLICATION_MODAL);

        STAGE.setOnHidden(e -> TOGGLE_GROUP.selectToggle(null));
    }

    static {
        WAITING_PAGE.setAlignment(Pos.CENTER);
        WAITING_PAGE.setPadding(new Insets(PADDING_MEDIUM, PADDING_SMALL, PADDING_LARGE, PADDING_SMALL));

        Label waitingLabel = new Label("正在搜索中......");
        waitingLabel.setMinWidth(350);

        ProgressIndicator progress = new ProgressIndicator();
        progress.setPrefSize(WAITING_PAGE_PROGRESS_SIZE, WAITING_PAGE_PROGRESS_SIZE);

        Button stopButton = new Button("取消");
        stopButton.setOnAction(e -> STAGE.close());
        HBox buttonBox = new HBox(stopButton);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        WAITING_PAGE.getChildren().addAll(waitingLabel, progress, buttonBox);
    }


    public static void show(String title, String artist, String album, Consumer<String> onResult) {
        // 使用异步任务加载封面
        Task<Void> loadTask = createSearchTask(title, artist, album, onResult);
        // 启动后台任务
        LYRIC_SEARCH_EXECUTOR.submit(loadTask);

        // 显示等待页面
        BODY.getChildren().clear();
        BODY.getChildren().add(WAITING_PAGE);

        // 窗口关闭时取消任务
        STAGE.setOnCloseRequest(event -> loadTask.cancel());
        STAGE.show();
    }

    private static final ArrayList<String> LYRICS = new ArrayList<>();

    private static Task<Void> createSearchTask(String title, String artist, String album, Consumer<String> onResult) {
        return new Task<>() {
            @Override
            protected Void call() throws InterruptedException {
                searchLyrics(title, artist, album);
                return null;
            }

            @Override
            protected void succeeded() {
                if (!LYRICS.isEmpty()) {
                    showLyricSelectionPage(onResult);
                } else {
                    Platform.runLater(() -> {
                        STAGE.close();
                        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                        alert.setGraphic(ImageInApp.getSearchEmptyIcon());
                        alert.setHeaderText("没有搜到任何匹配的歌词");
                        alert.show();
                    });
                }
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    // 异常处理
                    Throwable ex = getException();
                    ex.printStackTrace();
                    // 提示用户加载失败
                    Utils.errorAlert("歌词加载失败").show();
                    STAGE.close();
                });
            }
        };
    }

    private static void showLyricSelectionPage(Consumer<String> onResult) {
        Platform.runLater(() -> {
            STAGE.close();
            SelectLyric selection = new SelectLyric(LYRICS);
            Optional<String> lyric = selection.showAndGetSelection();
            onResult.accept(lyric.orElse(null));
        });
    }

    private static void searchLyrics(String title, String artist, String album) throws InterruptedException {
        List<String> qq = new ArrayList<>();
        List<String> netease = new ArrayList<>();
        List<String> migu = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(3);

        LYRIC_SEARCH_EXECUTOR.submit(() -> {
            try {
                qq.addAll(QQ_MUSIC_API.getLyric(title, artist, album));
            } finally {
                latch.countDown();
            }
        });

        LYRIC_SEARCH_EXECUTOR.submit(() -> {
            try {
                netease.addAll(NET_EASE_API.getLyric(title, artist, album));
            } finally {
                latch.countDown();
            }
        });

        LYRIC_SEARCH_EXECUTOR.submit(() -> {
            try {
                migu.addAll(MIGU_MUSIC_API.getLyric(title, artist, album));
            } finally {
                latch.countDown();
            }
        });

        latch.await();
        LYRICS.clear();
        LYRICS.addAll(qq);
        LYRICS.addAll(netease);
        LYRICS.addAll(migu);
    }
}
