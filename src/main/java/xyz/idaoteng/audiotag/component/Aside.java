package xyz.idaoteng.audiotag.component;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import xyz.idaoteng.audiotag.ImageInApp;
import xyz.idaoteng.audiotag.Session;
import xyz.idaoteng.audiotag.StartUp;
import xyz.idaoteng.audiotag.Utils;
import xyz.idaoteng.audiotag.bean.AudioMetaData;
import xyz.idaoteng.audiotag.constant.ComboBoxType;
import xyz.idaoteng.audiotag.constant.MusicGenre;
import xyz.idaoteng.audiotag.core.MetaDataWriter;
import xyz.idaoteng.audiotag.dialog.SelectCover;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

/**
 * 音频文件元数据编辑侧边栏组件
 * 提供查看和编辑音频文件元数据(标题、艺术家、专辑等信息)的界面
 */
public class Aside {
    // 侧边栏主容器
    private static final VBox ASIDE = new VBox();

    // 各种元数据组合框
    private static final ComboBox<String> TITLE_COMBO_BOX = new ComboBox<>();
    private static final ComboBox<String> ARTIST_COMBO_BOX = new ComboBox<>();
    private static final ComboBox<String> ALBUM_COMBO_BOX = new ComboBox<>();
    private static final ComboBox<String> DATE_COMBO_BOX = new ComboBox<>();
    private static final ComboBox<String> GENRE_COMBO_BOX = new ComboBox<>();
    private static final ComboBox<String> TRACK_COMBO_BOX = new ComboBox<>();
    private static final ComboBox<String> COMMENT_COMBO_BOX = new ComboBox<>();

    // 封面面板和相关按钮
    private static final HBox COVER_PANEL = new HBox();
    private static final Button CHANGE_COVER_BUTTON = new Button("更换");
    private static final Button EXTRACT_COVER_BUTTON = new Button("提取");
    private static final Button DELETE_COVER_BUTTON = new Button("删除");
    private static final Button SEARCH_COVER_BUTTON = new Button("搜索");

    // 确认和取消按钮
    private static final Button CONFIRM_BUTTON = new Button("确认更改");
    private static final Button CANCEL_BUTTON = new Button("取消");
    private static final HBox CONFIRM_BOX = new HBox();
    // 封面文件类型过滤器
    private static final FileChooser.ExtensionFilter COVER_EXTENSION_FILTER;
    // 用于显示大图的ImageView
    private static final ImageView LIGHT_BOX = new ImageView();
    // 元数据存储
    private static AudioMetaData originalMetaData = null; // 原始元数据
    private static AudioMetaData metaDataDisplayed = null; // 当前显示的元数据(可能包含编辑后的内容)
    // 侧边栏最小高度
    private static double asideMinHeight = 0;

    /*
      静态初始化块 - 初始化侧边栏UI组件
     */
    static {
        // 配置封面文件类型过滤器
        String[] imageExtensions = new String[]{"*.jpg", "*.jpeg", "*.png", "*.bmp", "*.webp"};
        COVER_EXTENSION_FILTER = new FileChooser.ExtensionFilter("图片文件", imageExtensions);

        // 设置侧边栏样式和间距
        ASIDE.setPadding(new Insets(10, 10, 10, 10));
        ASIDE.setStyle("-fx-border-style: solid; -fx-border-color: #cccccc; -fx-border-width: 1 0 0 1");

        // 配置各个元数据组合框和对应的标签
        // 标题
        Label titleLabel = new Label("标题");
        asideMinHeight = asideMinHeight + titleLabel.getHeight();
        configComboBox(ComboBoxType.TITLE, TITLE_COMBO_BOX, true);

        // 艺术家
        Label artistLabel = new Label("艺术家");
        asideMinHeight = asideMinHeight + artistLabel.getHeight();
        configComboBox(ComboBoxType.ARTIST, ARTIST_COMBO_BOX, true);

        // 专辑
        Label albumLabel = new Label("专辑");
        asideMinHeight = asideMinHeight + albumLabel.getHeight();
        configComboBox(ComboBoxType.ALBUM, ALBUM_COMBO_BOX, true);

        // 出版日期
        Label dateLabel = new Label("出版日期");
        asideMinHeight = asideMinHeight + dateLabel.getHeight();
        configComboBox(ComboBoxType.DATE, DATE_COMBO_BOX, true);

        // 流派
        Label genreLabel = new Label("流派");
        GENRE_COMBO_BOX.getItems().addAll(MusicGenre.getGenres()); // 添加预设流派
        configComboBox(ComboBoxType.GENRE, GENRE_COMBO_BOX, false);
        VBox genrePanel = new VBox(5); // 流派的垂直布局
        genrePanel.setMinWidth(165);
        genrePanel.setMaxWidth(165);
        genrePanel.getChildren().addAll(genreLabel, GENRE_COMBO_BOX);

        // 音轨序号
        Label trackLabel = new Label("音轨序号");
        TRACK_COMBO_BOX.getItems().setAll("", "1", "2", "3", "4", "5", "6", "7", "8", "9");
        configComboBox(ComboBoxType.TRACK, TRACK_COMBO_BOX, false);
        VBox trackPanel = new VBox(5); // 音轨序号的垂直布局
        trackPanel.getChildren().addAll(trackLabel, TRACK_COMBO_BOX);

        // 流派和音轨序号的水平布局
        HBox genreAndTrack = new HBox(10);
        genreAndTrack.setMaxWidth(250);
        genreAndTrack.getChildren().addAll(genrePanel, trackPanel);

        // 备注
        Label commentLabel = new Label("备注");
        asideMinHeight = asideMinHeight + commentLabel.getHeight();
        configComboBox(ComboBoxType.COMMENT, COMMENT_COMBO_BOX, true);

        // 封面面板
        Label coverLabel = new Label("封面");
        COVER_PANEL.setMaxHeight(200);
        COVER_PANEL.setMaxWidth(200);
        COVER_PANEL.setMinHeight(200);
        COVER_PANEL.setMinWidth(200);
        COVER_PANEL.setStyle("-fx-border-style: solid; -fx-border-color: #cccccc; -fx-border-width: 1 1 1 1");
        configCoverPanelActionHandle(); // 配置封面面板的交互事件

        // 封面操作按钮
        VBox coverOptions = new VBox(15);
        coverOptions.setAlignment(Pos.CENTER);
        coverOptions.getChildren().addAll(CHANGE_COVER_BUTTON, EXTRACT_COVER_BUTTON,
                DELETE_COVER_BUTTON, SEARCH_COVER_BUTTON);
        configCoverOptionActionHandle(); // 配置封面操作按钮的点击事件

        // 封面面板和操作按钮的水平布局
        HBox coverPanelAndOptions = new HBox(3);
        coverPanelAndOptions.getChildren().addAll(COVER_PANEL, coverOptions);
        asideMinHeight = asideMinHeight + coverPanelAndOptions.getHeight();

        // 初始化确认按钮区域
        initConfirmBox();

        // 设置侧边栏尺寸
        ASIDE.setMinWidth(280);
        ASIDE.setMaxWidth(280);
        ASIDE.setMinHeight(asideMinHeight);

        // 将所有组件添加到侧边栏主容器中
        ASIDE.getChildren().addAll(titleLabel, TITLE_COMBO_BOX, artistLabel, ARTIST_COMBO_BOX, albumLabel,
                ALBUM_COMBO_BOX, dateLabel, DATE_COMBO_BOX, genreAndTrack, commentLabel, COMMENT_COMBO_BOX,
                coverLabel, coverPanelAndOptions, CONFIRM_BOX);

        // 初始显示空白状态
        showBlank();
    }

    /**
     * 配置组合框
     * @param type        组合框类型
     * @param comboBox    要配置的组合框
     * @param defaultSize 是否使用默认尺寸
     */
    private static void configComboBox(ComboBoxType type, ComboBox<String> comboBox, boolean defaultSize) {
        if (defaultSize) {
            comboBox.setMinWidth(250);
            comboBox.setMaxWidth(250);
        }

        asideMinHeight = asideMinHeight + comboBox.getHeight();
        comboBox.setEditable(true); // 允许编辑

        // 值变化监听器 - 当组合框值变化时更新显示的元数据
        comboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                switch (type) {
                    case TITLE -> {
                        if (!newValue.equals(originalMetaData.getTitle())) {
                            metaDataDisplayed.setTitle(newValue);
                        }
                    }
                    case ARTIST -> {
                        if (!newValue.equals(originalMetaData.getArtist())) {
                            metaDataDisplayed.setArtist(newValue);
                        }
                    }
                    case ALBUM -> {
                        if (!newValue.equals(originalMetaData.getAlbum())) {
                            metaDataDisplayed.setAlbum(newValue);
                        }
                    }
                    case GENRE -> {
                        if (!newValue.equals(originalMetaData.getGenre())) {
                            metaDataDisplayed.setGenre(newValue);
                        }
                    }
                    case TRACK -> {
                        if (!newValue.equals(originalMetaData.getTrack())) {
                            metaDataDisplayed.setTrack(newValue);
                        }
                    }
                    case DATE -> {
                        if (!newValue.equals(originalMetaData.getDate())) {
                            metaDataDisplayed.setDate(newValue);
                        }
                    }
                    case COMMENT -> {
                        if (!newValue.equals(originalMetaData.getComment())) {
                            metaDataDisplayed.setComment(newValue);
                        }
                    }
                }
            }
        });
    }

    /**
     * 配置封面面板的交互事件
     */
    private static void configCoverPanelActionHandle() {
        // 鼠标进入时改变指针样式
        COVER_PANEL.setOnMouseEntered(event -> {
            if (metaDataDisplayed.getCover() == null) return;
            COVER_PANEL.setCursor(Cursor.HAND); // 手型指针
        });

        // 鼠标离开时恢复默认样式
        COVER_PANEL.setOnMouseExited(event -> {
            if (metaDataDisplayed.getCover() == null) return;
            COVER_PANEL.setCursor(Cursor.DEFAULT); // 默认指针
        });

        // 点击封面时显示大图
        COVER_PANEL.setOnMouseClicked(event -> {
            if (metaDataDisplayed == null) return;

            if (event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 1) {
                if (metaDataDisplayed.getCover() != null) {
                    LIGHT_BOX.setImage(new Image(new ByteArrayInputStream(metaDataDisplayed.getCover())));
                    LIGHT_BOX.setPreserveRatio(true);
                    LIGHT_BOX.setFitHeight(StartUp.getPrimaryStage().getHeight() - 150);
                    LIGHT_BOX.setFitWidth(StartUp.getPrimaryStage().getWidth() - 150);
                    LIGHT_BOX.setSmooth(true);
                    LIGHT_BOX.setCache(true);
                    Modal.show(LIGHT_BOX); // 在模态框中显示大图
                }
            }
        });
    }

    /**
     * 更新封面
     * @param delete 是否删除封面
     */
    private static void updateCover(boolean delete) {
        if (originalMetaData == null) return;

        if (delete) {
            setDefaultCover(); // 设置默认封面
        } else {
            // 打开文件选择器选择新的封面图片
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("选择封面");
            String path = Session.getFolderPathOfTheLastSelectedImage();
            fileChooser.setInitialDirectory(new File(path));
            fileChooser.getExtensionFilters().addAll(COVER_EXTENSION_FILTER);
            File file = fileChooser.showOpenDialog(StartUp.getPrimaryStage());
            if (file != null) {
                setCover(Utils.retouchCover(file)); // 设置新封面
                Session.setFolderPathOfTheLastSelectedImage(file.getParentFile().getAbsolutePath());
            }
        }
    }

    /**
     * 配置封面操作按钮的点击事件
     */
    private static void configCoverOptionActionHandle() {
        // 更换封面按钮
        CHANGE_COVER_BUTTON.setOnAction(event -> updateCover(false));

        // 提取封面按钮
        EXTRACT_COVER_BUTTON.setOnAction(event -> {
            if (originalMetaData != null) {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("保存封面");
                String path = Session.getLastSelectedImageSavingPath();
                fileChooser.setInitialDirectory(new File(path));
                fileChooser.getExtensionFilters().addAll(COVER_EXTENSION_FILTER);
                File file = fileChooser.showSaveDialog(StartUp.getPrimaryStage());
                if (file != null) {
                    Utils.saveCover(originalMetaData.getCover(), file); // 保存封面到文件
                    Session.setLastSelectedImageSavingPath(file.getParentFile().getAbsolutePath());
                }
            }
        });

        // 删除封面按钮
        DELETE_COVER_BUTTON.setOnAction(event -> updateCover(true));

        // 搜索封面按钮 - 打开封面搜索对话框
        SEARCH_COVER_BUTTON.setOnAction(event -> {
            String title = originalMetaData.getTitle();
            String artist = originalMetaData.getArtist();
            String album = originalMetaData.getAlbum();
            Consumer<byte[]> setCover = coverBytes -> {
                if (coverBytes != null) {
                    Platform.runLater(() -> setCover(coverBytes)); // 在UI线程中设置封面
                }
            };
            SelectCover.show(title, artist, album, setCover);
        });
    }

    /**
     * 初始化确认和取消按钮区域
     */
    private static void initConfirmBox() {
        // 确认按钮点击事件 - 保存修改
        CONFIRM_BUTTON.setOnAction(event -> {
            if (originalMetaData == null || metaDataDisplayed == null) return;

            List<String> changedTagNames = getChangedTags(); // 获取修改过的标签
            if (changedTagNames.isEmpty()) return; // 没有修改则不执行任何操作

            exchangeEditableValue(metaDataDisplayed, originalMetaData); // 将修改同步到原始元数据

            MetaDataWriter.write(originalMetaData); // 写入元数据到文件

            showMetaData(originalMetaData); // 刷新显示
            Center.updateTableView(null); // 更新中心区域的表格视图
            Center.selectItem(originalMetaData); // 选中当前项
            String message = String.join("、", changedTagNames) + " 已修改";
            Notification.showNotification(message); // 显示通知
        });

        // 取消按钮点击事件 - 丢弃修改
        CANCEL_BUTTON.setOnAction(event -> {
            if (originalMetaData == null || metaDataDisplayed == null) return;
            showMetaData(originalMetaData); // 恢复原始数据
        });

        // 确认按钮区域的布局设置
        CONFIRM_BOX.setAlignment(Pos.CENTER);
        CONFIRM_BOX.setSpacing(50);
        CONFIRM_BOX.setPadding(new Insets(20, 0, 0, 0));
        CONFIRM_BOX.getChildren().addAll(CONFIRM_BUTTON, CANCEL_BUTTON);
        asideMinHeight = asideMinHeight + CONFIRM_BOX.getHeight();
    }

    /**
     * 获取修改过的标签名称列表
     * @return 修改过的标签名称列表
     */
    private static List<String> getChangedTags() {
        List<String> changedTagNames = new ArrayList<>();
        if (originalMetaData != null && metaDataDisplayed != null) {
            if (!originalMetaData.getTitle().equals(metaDataDisplayed.getTitle())) {
                changedTagNames.add("标题");
            }
            if (!originalMetaData.getArtist().equals(metaDataDisplayed.getArtist())) {
                changedTagNames.add("艺术家");
            }
            if (!originalMetaData.getAlbum().equals(metaDataDisplayed.getAlbum())) {
                changedTagNames.add("专辑");
            }
            if (!originalMetaData.getDate().equals(metaDataDisplayed.getDate())) {
                changedTagNames.add("日期");
            }
            if (!originalMetaData.getGenre().equals(metaDataDisplayed.getGenre())) {
                changedTagNames.add("流派");
            }
            if (!originalMetaData.getTrack().equals(metaDataDisplayed.getTrack())) {
                changedTagNames.add("序号");
            }
            if (!originalMetaData.getComment().equals(metaDataDisplayed.getComment())) {
                changedTagNames.add("备注");
            }
            if (!Arrays.equals(originalMetaData.getCover(), metaDataDisplayed.getCover())) {
                changedTagNames.add("封面");
            }
        }
        return changedTagNames;
    }

    /**
     * 交换两个元数据对象的可编辑值
     * @param from 源元数据对象
     * @param to   目标元数据对象
     */
    private static void exchangeEditableValue(AudioMetaData from, AudioMetaData to) {
        to.setTitle(from.getTitle());
        to.setArtist(from.getArtist());
        to.setAlbum(from.getAlbum());
        to.setDate(from.getDate());
        to.setGenre(from.getGenre());
        to.setTrack(from.getTrack());
        to.setComment(from.getComment());
        to.setCover(from.getCover());
    }

    /**
     * 显示元数据到侧边栏
     * @param original 要显示的音频元数据
     */
    public static void showMetaData(AudioMetaData original) {
        originalMetaData = original;
        metaDataDisplayed = new AudioMetaData();
        exchangeEditableValue(original, metaDataDisplayed); // 复制原始数据到显示用的副本

        // 启用所有组合框
        TITLE_COMBO_BOX.setDisable(false);
        ARTIST_COMBO_BOX.setDisable(false);
        ALBUM_COMBO_BOX.setDisable(false);
        DATE_COMBO_BOX.setDisable(false);
        GENRE_COMBO_BOX.setDisable(false);
        TRACK_COMBO_BOX.setDisable(false);
        COMMENT_COMBO_BOX.setDisable(false);

        // 设置各个组合框的值和选项
        // 标题
        TITLE_COMBO_BOX.getItems().clear();
        TITLE_COMBO_BOX.setValue(original.getTitle());
        TITLE_COMBO_BOX.getItems().add("");

        // 艺术家
        ARTIST_COMBO_BOX.getItems().clear();
        ARTIST_COMBO_BOX.setValue(original.getArtist());
        ARTIST_COMBO_BOX.getItems().add("");
        ARTIST_COMBO_BOX.getItems().addAll(Center.getAlternativeArtists()); // 添加备选艺术家

        // 专辑
        ALBUM_COMBO_BOX.getItems().clear();
        ALBUM_COMBO_BOX.setValue(original.getAlbum());
        ALBUM_COMBO_BOX.getItems().add("");
        ALBUM_COMBO_BOX.getItems().addAll(Center.getAlternativeAlbums()); // 添加备选专辑

        // 出版日期
        DATE_COMBO_BOX.getItems().clear();
        DATE_COMBO_BOX.setValue(original.getDate());
        DATE_COMBO_BOX.getItems().add("");

        // 流派
        GENRE_COMBO_BOX.getItems().clear();
        GENRE_COMBO_BOX.setValue(original.getGenre());
        GENRE_COMBO_BOX.getItems().add("");
        GENRE_COMBO_BOX.getItems().addAll(MusicGenre.getGenres()); // 添加所有预设流派

        // 音轨序号
        TRACK_COMBO_BOX.setValue(original.getTrack());
        TRACK_COMBO_BOX.getItems().add("");

        // 备注
        COMMENT_COMBO_BOX.getItems().clear();
        COMMENT_COMBO_BOX.setValue(original.getComment());
        COMMENT_COMBO_BOX.getItems().add("");

        // 设置封面
        if (original.getCover() != null) {
            setCover(original.getCover());
        } else {
            setDefaultCover();
        }
    }

    /**
     * 设置封面图片
     * @param coverBytes 封面图片字节数组
     */
    private static void setCover(byte[] coverBytes) {
        COVER_PANEL.getChildren().clear();
        ImageView cover = new ImageView();
        cover.setFitWidth(200);
        cover.setFitHeight(200);
        cover.setImage(new Image(new ByteArrayInputStream(coverBytes)));
        COVER_PANEL.getChildren().add(cover);

        metaDataDisplayed.setCover(coverBytes); // 更新显示的元数据中的封面

        // 启用在有封面时可用的按钮
        CHANGE_COVER_BUTTON.setDisable(false);
        EXTRACT_COVER_BUTTON.setDisable(false);
        DELETE_COVER_BUTTON.setDisable(false);
        SEARCH_COVER_BUTTON.setDisable(false);
    }

    /**
     * 设置默认封面
     */
    private static void setDefaultCover() {
        COVER_PANEL.getChildren().clear();
        COVER_PANEL.setAlignment(Pos.CENTER);
        COVER_PANEL.getChildren().add(ImageInApp.getDefaultCover()); // 添加默认封面图片

        if (metaDataDisplayed != null) {
            metaDataDisplayed.setCover(null); // 清空显示的元数据中的封面
        }

        // 设置按钮状态：更换和搜索只在有原始数据时可用，提取和删除在无封面时不可用
        CHANGE_COVER_BUTTON.setDisable(originalMetaData == null);
        EXTRACT_COVER_BUTTON.setDisable(true);
        DELETE_COVER_BUTTON.setDisable(true);
        SEARCH_COVER_BUTTON.setDisable(originalMetaData == null);
    }

    /**
     * 显示空白状态
     */
    public static void showBlank() {
        showMetaData(new AudioMetaData()); // 显示空元数据
        originalMetaData = null; // 清空原始数据引用

        // 禁用所有组合框
        TITLE_COMBO_BOX.setDisable(true);
        ARTIST_COMBO_BOX.setDisable(true);
        ALBUM_COMBO_BOX.setDisable(true);
        DATE_COMBO_BOX.setDisable(true);
        GENRE_COMBO_BOX.setDisable(true);
        TRACK_COMBO_BOX.setDisable(true);
        COMMENT_COMBO_BOX.setDisable(true);

        setDefaultCover(); // 设置默认封面
    }

    /**
     * 刷新侧边栏显示
     */
    public static void refresh() {
        if (originalMetaData != null) {
            showMetaData(originalMetaData); // 重新显示原始数据
        } else {
            showBlank(); // 显示空白状态
        }
    }

    /**
     * 获取侧边栏主容器
     * @return 侧边栏 VBox 组件
     */
    public static VBox getAside() {
        return ASIDE;
    }
}