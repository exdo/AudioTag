package xyz.idaoteng.audiotag.dialog;

import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import xyz.idaoteng.audiotag.api.Api;
import xyz.idaoteng.audiotag.api.migu.MiguMusicApi;
import xyz.idaoteng.audiotag.api.netease.NetEaseMusicApi;
import xyz.idaoteng.audiotag.api.timeless.TimelessApi;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class SelectCover {
    private static final Stage STAGE = new Stage();
    private static final VBox PANE = new VBox(8);
    private static final Scene SCENE = new Scene(PANE);

    private static final ToggleGroup TOGGLE_GROUP = new ToggleGroup();
    private static final Api API_TIMELESS = new TimelessApi();
    private static final Api API_NET_EASE = new NetEaseMusicApi();
    private static final Api API_MIGU = new MiguMusicApi();

    private static final List<byte[]> COVERS_FROM_TIMELESS = new ArrayList<>();
    private static final List<byte[]> COVERS_FROM_NET_EASE = new ArrayList<>();
    private static final List<byte[]> COVERS_FROM_MIGU = new ArrayList<>();

    private static final String[] ID_PREFIX = new String[] {"qq", "netEase", "migu"};
    
    static {
        PANE.setMaxHeight(600);
        PANE.setPadding(new Insets(0, 15, 0, 15));
        STAGE.setScene(SCENE);
        STAGE.setTitle("选择图片");
        STAGE.setResizable(false);
        STAGE.initModality(Modality.APPLICATION_MODAL);

        STAGE.setOnHidden(e -> TOGGLE_GROUP.selectToggle(null));
    }

    private static byte[] result = null;
    public static void show(String title, String artist, String album, Consumer<byte[]> onResult) {
        result = null;
        // 使用异步任务加载封面
        Task<Void> loadTask = new Task<>() {
            @Override
            protected Void call() {
                refreshCovers(title, artist, album);
                return null;
            }
        };

        loadTask.setOnSucceeded(e -> {
            // 数据加载完成后更新 UI
            Node qq = generateCard(COVERS_FROM_TIMELESS, "来自QQ音乐", ID_PREFIX[0]);
            Node netEase = generateCard(COVERS_FROM_NET_EASE, "来自网易云音乐", ID_PREFIX[1]);
            Node migu = generateCard(COVERS_FROM_MIGU, "来自咪咕音乐", ID_PREFIX[2]);

            // 更新 UI 前关闭等待页面，防止界面突然变大形成闪烁及窗口位置不居中
            STAGE.close();

            Button okButton = new Button("确定");
            okButton.setOnAction(event -> {
                RadioButton selectedRadioButton = (RadioButton) TOGGLE_GROUP.getSelectedToggle();
                if (selectedRadioButton != null) {
                    result = getCoverById(selectedRadioButton.getId());
                }
                STAGE.close();
                // 用户点击确定后调用回调
                onResult.accept(result);
            });

            Button cancelButton = new Button("取消");
            cancelButton.setOnAction(event -> STAGE.close());
            cancelButton.requestFocus();

            HBox buttonBox = new HBox(10, okButton, cancelButton);
            buttonBox.setAlignment(Pos.CENTER_RIGHT);
            buttonBox.setPadding(new Insets(0, 0, 18, 0));

            ScrollPane scrollPane = new ScrollPane();
            VBox content = new VBox(10, qq, netEase, migu);
            scrollPane.setContent(content);
            scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

            PANE.getChildren().clear();
            PANE.getChildren().setAll(scrollPane, buttonBox);
            STAGE.sizeToScene();
            STAGE.show();
        });

        Thread searchingThread = new Thread(loadTask);// 启动后台任务
        searchingThread.setDaemon(true);
        searchingThread.start();

        VBox waitingPage = new VBox(15);
        waitingPage.setAlignment(Pos.CENTER);
        waitingPage.setPadding(new Insets(15, 10, 20, 10));

        Label  waitingLabel = new Label("正在搜索中......");
        waitingLabel.setMinWidth(350);

        ProgressIndicator progress = new ProgressIndicator();
        progress.setPrefSize(75, 75);

        Button stopButton = new Button("取消");
        stopButton.setOnAction(e -> {
            STAGE.close();
            loadTask.cancel();
            searchingThread.stop();
        });
        HBox buttonBox = new HBox(stopButton);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        waitingPage.getChildren().addAll(waitingLabel, progress, buttonBox);
        PANE.getChildren().clear();
        PANE.getChildren().add(waitingPage);
        STAGE.show();
    }

    private static void refreshCovers(String title, String artist, String album) {
        COVERS_FROM_MIGU.clear();
        COVERS_FROM_MIGU.addAll(API_MIGU.getCover(title, artist, album));

        COVERS_FROM_NET_EASE.clear();
        COVERS_FROM_NET_EASE.addAll(API_NET_EASE.getCover(title, artist, album));

        COVERS_FROM_TIMELESS.clear();
        COVERS_FROM_TIMELESS.addAll(API_TIMELESS.getCover(title, artist, album));
    }

    private static byte[] getCoverById(String id) {
        String[] split = id.split("-");
        return switch (split[0]) {
            case "qq" -> COVERS_FROM_TIMELESS.get(Integer.parseInt(split[1]));
            case "netEase" -> COVERS_FROM_NET_EASE.get(Integer.parseInt(split[1]));
            case "migu" -> COVERS_FROM_MIGU.get(Integer.parseInt(split[1]));
            default -> null;
        };
    }

    private static Node generateCard(List<byte[]> coves, String title, String tag) {
        VBox card = new VBox();
        Label label = new Label(title);
        label.setStyle("-fx-font-weight: bold");
        Node content;
        if (!coves.isEmpty()) {
            GridPane gridPane = new GridPane();
            gridPane.setPadding(new Insets(10));
            gridPane.setHgap(5);
            gridPane.setVgap(5);

            for (int i = 0; i < coves.size(); i++) {
                Node node = generateAlternativeImageNode(coves.get(i), tag + "-" + i);
                gridPane.add(node, i % 5, i / 5);
            }

            content = gridPane;
        } else {
            Label blankResultTitle = new Label("搜索结果为空");
            blankResultTitle.setMinWidth(450);
            blankResultTitle.setAlignment(Pos.CENTER);
            blankResultTitle.setTextFill(Color.RED);
            content = blankResultTitle;
        }
        Separator  separator = new Separator();
        card.getChildren().addAll(label, content, separator);
        return card;
    }

    private static Node generateAlternativeImageNode(byte[] cover, String id) {
        AnchorPane pane = new AnchorPane();
        pane.setMinHeight(150);
        pane.setMaxHeight(150);
        pane.setMinWidth(150);
        pane.setMaxWidth(150);

        ImageView imageView = new ImageView();
        imageView.setFitHeight(150);
        imageView.setFitWidth(150);
        imageView.setImage(new Image(new ByteArrayInputStream(cover)));
        AnchorPane.setTopAnchor(imageView, 0.0);
        AnchorPane.setBottomAnchor(imageView, 0.0);
        AnchorPane.setLeftAnchor(imageView, 0.0);
        AnchorPane.setRightAnchor(imageView, 0.0);

        RadioButton radioButton = new RadioButton();
        radioButton.setId(id);
        radioButton.setToggleGroup(TOGGLE_GROUP);
        AnchorPane.setTopAnchor(radioButton, 0.0);
        AnchorPane.setRightAnchor(radioButton, 0.0);

        pane.setOnMouseClicked(event -> TOGGLE_GROUP.selectToggle(radioButton));
        pane.getChildren().addAll(imageView, radioButton);
        return pane;
    }
}
