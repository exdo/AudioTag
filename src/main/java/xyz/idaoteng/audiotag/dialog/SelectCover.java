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
import xyz.idaoteng.audiotag.api.CoverApi;
import xyz.idaoteng.audiotag.api.migu.MiguMusicApi;
import xyz.idaoteng.audiotag.api.netease.NetEaseMusicApi;
import xyz.idaoteng.audiotag.api.timeless.TimelessApi;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class SelectCover {
    // 常量定义
    private static final double IMAGE_SIZE = 150.0;
    private static final double PREVIEW_IMAGE_MAX_SIZE = 600.0; // 预览图片的最大尺寸
    private static final double WAITING_PAGE_PROGRESS_SIZE = 75.0;
    private static final int PANE_SPACING = 8;
    private static final int CONTENT_SPACING = 10;
    private static final int BUTTON_SPACING = 10;
    private static final int GRID_H_GAP = 5;
    private static final int GRID_V_GAP = 5;
    private static final int GRID_COLUMN_COUNT = 5;
    private static final int PADDING_SMALL = 10;
    private static final int PADDING_MEDIUM = 15;
    private static final int PADDING_LARGE = 20;

    private static final String STYLE_BOLD_FONT = "-fx-font-weight: bold";

    private static final Stage STAGE = new Stage();
    private static final VBox PANE = new VBox(PANE_SPACING);
    private static final Scene SCENE = new Scene(PANE);

    private static final ToggleGroup TOGGLE_GROUP = new ToggleGroup();
    private static final CoverApi COVER_API_TIMELESS = new TimelessApi();
    private static final CoverApi COVER_API_NET_EASE = new NetEaseMusicApi();
    private static final CoverApi COVER_API_MIGU = new MiguMusicApi();

    private static final List<byte[]> COVERS_FROM_TIMELESS = new ArrayList<>();
    private static final List<byte[]> COVERS_FROM_NET_EASE = new ArrayList<>();
    private static final List<byte[]> COVERS_FROM_MIGU = new ArrayList<>();

    private static final String[] ID_PREFIX = {"qq", "netEase", "migu"};

    private static Stage previewStage; // 预览窗口

    static {
        PANE.setMaxHeight(600);
        PANE.setPadding(new Insets(0, PADDING_MEDIUM, 0, PADDING_MEDIUM));
        STAGE.setScene(SCENE);
        STAGE.setTitle("选择图片");
        STAGE.setResizable(false);
        STAGE.initModality(Modality.APPLICATION_MODAL);

        STAGE.setOnHidden(e -> TOGGLE_GROUP.selectToggle(null));
    }

    private static byte[] result = null;

    public static void show(String title, String artist, String album, Consumer<byte[]> onResult) {
        // 重置数据
        result = null;

        // 使用异步任务加载封面
        Task<Void> loadTask = createLoadTask(title, artist, album, onResult);

        // 显示等待页面
        showWaitingPage(loadTask);

        // 启动后台任务
        Thread searchingThread = new Thread(loadTask);
        searchingThread.setDaemon(true);
        searchingThread.start();

        // 窗口关闭时取消任务
        STAGE.setOnCloseRequest(event -> loadTask.cancel());
    }

    /**
     * 创建异步任务，用于加载封面图片。
     *
     * @param title    歌曲标题
     * @param artist   歌曲艺术家
     * @param album    歌曲专辑
     * @param onResult 加载完成后的回调函数
     * @return 异步任务
     */
    private static Task<Void> createLoadTask(String title, String artist, String album, Consumer<byte[]> onResult) {
        return new Task<>() {
            @Override
            protected Void call() {
                refreshCovers(title, artist, album);
                return null;
            }

            @Override
            protected void succeeded() {
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

                HBox buttonBox = new HBox(BUTTON_SPACING, okButton, cancelButton);
                buttonBox.setAlignment(Pos.CENTER_RIGHT);
                buttonBox.setPadding(new Insets(0, 0, 18, 0));

                ScrollPane scrollPane = new ScrollPane();
                VBox content = new VBox(CONTENT_SPACING, qq, netEase, migu);
                scrollPane.setContent(content);
                scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

                PANE.getChildren().clear();
                PANE.getChildren().setAll(scrollPane, buttonBox);
                STAGE.sizeToScene();
                STAGE.show();
            }
        };
    }

    /**
     * 显示等待页面。
     *
     * @param loadTask 异步加载任务
     */
    private static void showWaitingPage(Task<?> loadTask) {
        VBox waitingPage = new VBox(PADDING_MEDIUM);
        waitingPage.setAlignment(Pos.CENTER);
        waitingPage.setPadding(new Insets(PADDING_MEDIUM, PADDING_SMALL, PADDING_LARGE, PADDING_SMALL));

        Label waitingLabel = new Label("正在搜索中......");
        waitingLabel.setMinWidth(350);

        ProgressIndicator progress = new ProgressIndicator();
        progress.setPrefSize(WAITING_PAGE_PROGRESS_SIZE, WAITING_PAGE_PROGRESS_SIZE);

        Button stopButton = new Button("取消");
        stopButton.setOnAction(e -> {
            STAGE.close();
            loadTask.cancel();
        });
        HBox buttonBox = new HBox(stopButton);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        waitingPage.getChildren().addAll(waitingLabel, progress, buttonBox);
        PANE.getChildren().clear();
        PANE.getChildren().add(waitingPage);
        STAGE.show();
    }

    /**
     * 刷新从不同API获取的封面列表。
     *
     * @param title  歌曲标题
     * @param artist 歌曲艺术家
     * @param album  歌曲专辑
     */
    private static void refreshCovers(String title, String artist, String album) {
        COVERS_FROM_MIGU.clear();
        COVERS_FROM_MIGU.addAll(COVER_API_MIGU.getCover(title, artist, album));

        COVERS_FROM_NET_EASE.clear();
        COVERS_FROM_NET_EASE.addAll(COVER_API_NET_EASE.getCover(title, artist, album));

        COVERS_FROM_TIMELESS.clear();
        COVERS_FROM_TIMELESS.addAll(COVER_API_TIMELESS.getCover(title, artist, album));
    }

    /**
     * 根据ID获取封面图片数据。
     *
     * @param id 封面图片的ID
     * @return 封面图片数据
     */
    private static byte[] getCoverById(String id) {
        String[] split = id.split("-");
        return switch (split[0]) {
            case "qq" -> COVERS_FROM_TIMELESS.get(Integer.parseInt(split[1]));
            case "netEase" -> COVERS_FROM_NET_EASE.get(Integer.parseInt(split[1]));
            case "migu" -> COVERS_FROM_MIGU.get(Integer.parseInt(split[1]));
            default -> null;
        };
    }

    /**
     * 生成包含封面列表的卡片。
     *
     * @param covers 封面图片数据列表
     * @param title  卡片标题
     * @param tag    卡片标签
     * @return 卡片节点
     */
    private static Node generateCard(List<byte[]> covers, String title, String tag) {
        VBox card = new VBox();
        Label label = new Label(title);
        label.setStyle(STYLE_BOLD_FONT);
        Node content;
        if (!covers.isEmpty()) {
            GridPane gridPane = new GridPane();
            gridPane.setPadding(new Insets(PADDING_SMALL));
            gridPane.setHgap(GRID_H_GAP);
            gridPane.setVgap(GRID_V_GAP);

            for (int i = 0; i < covers.size(); i++) {
                Node node = generateAlternativeImageNode(covers.get(i), tag + "-" + i);
                gridPane.add(node, i % GRID_COLUMN_COUNT, i / GRID_COLUMN_COUNT);
            }

            content = gridPane;
        } else {
            Label blankResultTitle = new Label("搜索结果为空");
            blankResultTitle.setMinWidth(450);
            blankResultTitle.setAlignment(Pos.CENTER);
            blankResultTitle.setTextFill(Color.RED);
            content = blankResultTitle;
        }
        Separator separator = new Separator();
        card.getChildren().addAll(label, content, separator);
        return card;
    }

    /**
     * 生成单个封面图片的选择节点。
     *
     * @param cover 封面图片数据
     * @param id    节点ID
     * @return 封面图片选择节点
     */
    private static Node generateAlternativeImageNode(byte[] cover, String id) {
        AnchorPane pane = new AnchorPane();
        pane.setMinHeight(IMAGE_SIZE);
        pane.setMaxHeight(IMAGE_SIZE);
        pane.setMinWidth(IMAGE_SIZE);
        pane.setMaxWidth(IMAGE_SIZE);

        ImageView imageView = new ImageView();
        imageView.setFitHeight(IMAGE_SIZE);
        imageView.setFitWidth(IMAGE_SIZE);
        imageView.setImage(new Image(new ByteArrayInputStream(cover)));
        AnchorPane.setTopAnchor(imageView, 0.0);
        AnchorPane.setBottomAnchor(imageView, 0.0);
        AnchorPane.setLeftAnchor(imageView, 0.0);
        AnchorPane.setRightAnchor(imageView, 0.0);

        // 添加点击事件，显示预览
        imageView.setOnMouseClicked(event -> showImagePreview(imageView.getImage()));

        RadioButton radioButton = new RadioButton();
        radioButton.setId(id);
        radioButton.setToggleGroup(TOGGLE_GROUP);
        AnchorPane.setTopAnchor(radioButton, 0.0);
        AnchorPane.setRightAnchor(radioButton, 0.0);

        pane.setOnMouseClicked(event -> TOGGLE_GROUP.selectToggle(radioButton));
        pane.getChildren().addAll(imageView, radioButton);
        return pane;
    }

    /**
     * 显示图片预览窗口.
     * @param image 要显示的图片
     */
    private static void showImagePreview(Image image) {
        if (previewStage == null) {
            previewStage = new Stage();
            previewStage.initModality(Modality.NONE); // 非模态窗口，不阻塞主窗口
            previewStage.setTitle("图片预览");
        }

        ImageView previewImageView = new ImageView(image);
        previewImageView.setPreserveRatio(true); // 保持宽高比
        previewImageView.setSmooth(true);       // 提高缩放质量
        previewImageView.setCache(true);        // 缓存图片

        // 根据图片大小调整预览窗口大小
        double width  = Math.min(image.getWidth(), PREVIEW_IMAGE_MAX_SIZE);
        double height = Math.min(image.getHeight(), PREVIEW_IMAGE_MAX_SIZE);
        previewImageView.setFitWidth(width);
        previewImageView.setFitHeight(height);

        ScrollPane scrollPane = new ScrollPane(previewImageView);
        scrollPane.setFitToWidth(true);   // 宽度适应内容
        scrollPane.setFitToHeight(true);  // 高度适应内容

        Scene scene = new Scene(scrollPane, width, height);
        previewStage.setScene(scene);
        previewStage.show();
    }
}
