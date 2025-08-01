package xyz.idaoteng.audiotag.component;

import atlantafx.base.theme.Styles;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import xyz.idaoteng.audiotag.App;
import xyz.idaoteng.audiotag.Session;
import xyz.idaoteng.audiotag.UiCoordinator;
import xyz.idaoteng.audiotag.bean.AudioFileData;
import xyz.idaoteng.audiotag.constant.EditableTag;
import xyz.idaoteng.audiotag.constant.ExtensionFilters;
import xyz.idaoteng.audiotag.constant.MusicGenre;
import xyz.idaoteng.audiotag.dialog.EditLyric;
import xyz.idaoteng.audiotag.dialog.SearchCover;
import xyz.idaoteng.audiotag.jaudiotagger.AudioFileWriter;
import xyz.idaoteng.audiotag.util.ImageInApp;
import xyz.idaoteng.audiotag.util.Utils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * 音频文件元数据编辑侧边栏组件
 * 提供查看和编辑音频文件元数据(标题、艺术家、专辑等信息)的界面
 */
public class Aside {
    // 侧边栏主容器
    private final VBox aside = new VBox();

    // 各种元数据组合框
    private final ComboBox<String> titleComboBox = new ComboBox<>();
    private final ComboBox<String> artistComboBox = new ComboBox<>();
    private final ComboBox<String> albumComboBox = new ComboBox<>();
    private final ComboBox<String> dateComboBox = new ComboBox<>();
    private final ComboBox<String> genreComboBox = new ComboBox<>();
    private final ComboBox<String> trackComboBox = new ComboBox<>();
    private final ComboBox<String> commentComboBox = new ComboBox<>();

    // 封面面板和相关按钮
    private final HBox coverPanel = new HBox();
    private final Button changeCoverButton = new Button("更换");
    private final Button extractCoverButton = new Button("提取");
    private final Button deleteCoverButton = new Button("删除");
    private final Button searchCoverButton = new Button("搜索");

    // 确认和取消按钮
    private final Button confirmButton = new Button("确认修改");
    private final Button cancelButton = new Button("取消");
    private final HBox confirmBox = new HBox();
    // 用于显示大图的ImageView
    private final ImageView lightBox = new ImageView();
    // 元数据存储
    private AudioFileData originalData = null; // 原始元数据
    private AudioFileData displayedData = null; // 当前显示的元数据(可能包含编辑后的内容)

    public Aside() {
        // 设置侧边栏样式和间距
        aside.setPadding(new Insets(10, 10, 10, 10));
        aside.setStyle("-fx-border-style: solid; -fx-border-color: #cccccc; -fx-border-width: 1 0 0 1");

        // 配置各个元数据组合框和对应的标签
        // 标题
        Label titleLabel = new Label("标题");
        configComboBox(EditableTag.TITLE, titleComboBox, true);

        // 艺术家
        Label artistLabel = new Label("艺术家");
        configComboBox(EditableTag.ARTIST, artistComboBox, true);

        // 专辑
        Label albumLabel = new Label("专辑");
        configComboBox(EditableTag.ALBUM, albumComboBox, true);

        // 出版日期
        Label dateLabel = new Label("出版日期");
        configComboBox(EditableTag.DATE, dateComboBox, true);

        // 流派
        Label genreLabel = new Label("流派");
        genreComboBox.getItems().addAll(MusicGenre.getGenres()); // 添加预设流派
        configComboBox(EditableTag.GENRE, genreComboBox, false);
        VBox genrePanel = new VBox(5); // 流派的垂直布局
        genrePanel.setMinWidth(165);
        genrePanel.setMaxWidth(165);
        genrePanel.getChildren().addAll(genreLabel, genreComboBox);

        // 音轨序号
        Label trackLabel = new Label("音轨序号");
        trackComboBox.getItems().setAll("", "1", "2", "3", "4", "5", "6", "7", "8", "9");
        configComboBox(EditableTag.TRACK, trackComboBox, false);
        VBox trackPanel = new VBox(5); // 音轨序号的垂直布局
        trackPanel.getChildren().addAll(trackLabel, trackComboBox);

        // 流派和音轨序号的水平布局
        HBox genreAndTrack = new HBox(10);
        genreAndTrack.setMaxWidth(250);
        genreAndTrack.getChildren().addAll(genrePanel, trackPanel);

        // 备注
        Label commentLabel = new Label("备注");
        configComboBox(EditableTag.COMMENT, commentComboBox, true);

        // 歌词
        Button viewLyricButton = new Button("查看歌词");
        viewLyricButton.setGraphic(ImageInApp.getLyricIcon());
        viewLyricButton.getStyleClass().add(Styles.FLAT);
        viewLyricButton.setOnAction(event -> EditLyric.show(originalData));

        // 封面面板
        Label coverLabel = new Label("封面");
        coverPanel.setMaxHeight(200);
        coverPanel.setMaxWidth(200);
        coverPanel.setMinHeight(200);
        coverPanel.setMinWidth(200);
        coverPanel.setStyle("-fx-border-style: solid; -fx-border-color: #cccccc; -fx-border-width: 1 1 1 1");
        configCoverPanel(); // 配置封面面板的交互事件

        // 封面操作按钮
        VBox coverOptions = new VBox(15);
        coverOptions.setAlignment(Pos.CENTER);
        coverOptions.getChildren().addAll(
                changeCoverButton,
                extractCoverButton,
                deleteCoverButton,
                searchCoverButton
        );
        configCoverOption(); // 配置封面操作按钮的点击事件

        // 封面面板和操作按钮的水平布局
        HBox coverPanelAndOptions = new HBox(3);
        coverPanelAndOptions.getChildren().addAll(coverPanel, coverOptions);

        // 初始化确认按钮区域
        initConfirmBox();

        // 设置侧边栏尺寸
        aside.setMinWidth(280);
        aside.setMaxWidth(280);

        // 将所有组件添加到侧边栏主容器中
        aside.getChildren().addAll(
                titleLabel,
                titleComboBox,
                artistLabel,
                artistComboBox,
                albumLabel,
                albumComboBox,
                dateLabel,
                dateComboBox,
                genreAndTrack,
                commentLabel,
                commentComboBox,
                viewLyricButton,
                coverLabel,
                coverPanelAndOptions,
                confirmBox
        );

        // 初始显示空白状态
        showBlank();
    }

    /**
     * 配置组合框
     *
     * @param type        组合框类型
     * @param comboBox    要配置的组合框
     * @param defaultSize 是否使用默认尺寸
     */
    private void configComboBox(EditableTag type, ComboBox<String> comboBox, boolean defaultSize) {
        if (defaultSize) {
            comboBox.setMinWidth(250);
            comboBox.setMaxWidth(250);
        }

        comboBox.setEditable(true); // 允许编辑

        // 值变化监听器 - 当组合框值变化时更新 displayedData
        comboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                switch (type) {
                    case TITLE -> {
                        if (!newValue.equals(originalData.getTitle())) {
                            displayedData.setTitle(newValue);
                        }
                    }
                    case ARTIST -> {
                        if (!newValue.equals(originalData.getArtist())) {
                            displayedData.setArtist(newValue);
                        }
                    }
                    case ALBUM -> {
                        if (!newValue.equals(originalData.getAlbum())) {
                            displayedData.setAlbum(newValue);
                        }
                    }
                    case GENRE -> {
                        if (!newValue.equals(originalData.getGenre())) {
                            displayedData.setGenre(newValue);
                        }
                    }
                    case TRACK -> {
                        if (!newValue.equals(originalData.getTrack())) {
                            displayedData.setTrack(newValue);
                        }
                    }
                    case DATE -> {
                        if (!newValue.equals(originalData.getDate())) {
                            displayedData.setDate(newValue);
                        }
                    }
                    case COMMENT -> {
                        if (!newValue.equals(originalData.getComment())) {
                            displayedData.setComment(newValue);
                        }
                    }
                }
            }
        });
    }

    /**
     * 配置封面面板的交互事件
     */
    private void configCoverPanel() {
        // 鼠标进入时改变指针样式
        coverPanel.setOnMouseEntered(event -> {
            if (displayedData.getCover() != null) {
                coverPanel.setCursor(Cursor.HAND);
            }
        });

        // 鼠标离开时恢复默认样式
        coverPanel.setOnMouseExited(event -> {
            coverPanel.setCursor(Cursor.DEFAULT); // 默认指针
        });

        // 点击封面时显示大图
        coverPanel.setOnMouseClicked(event -> {
            if (displayedData == null) return;

            if (event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 1) {
                if (displayedData.getCover() != null) {
                    lightBox.setImage(new Image(new ByteArrayInputStream(displayedData.getCover())));
                    lightBox.setPreserveRatio(true);
                    lightBox.setFitHeight(App.getPrimaryStage().getHeight() - 150);
                    lightBox.setFitWidth(App.getPrimaryStage().getWidth() - 150);
                    lightBox.setSmooth(true);
                    lightBox.setCache(true);
                    UiCoordinator.showModal(lightBox); // 在模态框中显示大图
                }
            }
        });
    }

    /**
     * 更新封面
     *
     * @param delete 是否删除封面
     */
    private void updateCover(boolean delete) {
        if (originalData == null) return;

        if (delete) {
            displayedData.setCover(null);
            showDefaultCover();
        } else {
            // 打开文件选择器选择新的封面图片
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("选择封面");
            fileChooser.setInitialDirectory(Session.getLastSelectedImageFolder());
            fileChooser.getExtensionFilters().addAll(ExtensionFilters.IMAGE_FILTER);
            File file = fileChooser.showOpenDialog(App.getPrimaryStage());
            if (file != null) {
                setCover(Utils.retouchedOrItself(file)); // 设置新封面
                Session.setLastSelectedImageFolder(file.getParentFile());
            }
        }
    }

    /**
     * 配置封面操作按钮的点击事件
     */
    private void configCoverOption() {
        // 更换封面按钮
        changeCoverButton.setOnAction(event -> updateCover(false));

        // 提取封面按钮
        extractCoverButton.setOnAction(event -> {
            if (originalData != null) {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("保存封面");

                fileChooser.setInitialDirectory(Session.getLastImageSavingPath());
                fileChooser.getExtensionFilters().addAll(ExtensionFilters.IMAGE_FILTER);
                File file = fileChooser.showSaveDialog(App.getPrimaryStage());
                if (file != null) {
                    Utils.saveCover(originalData.getCover(), file); // 保存封面到文件
                    Session.setLastImageSavingPath(file.getParentFile());
                }
            }
        });

        // 删除封面按钮
        deleteCoverButton.setOnAction(event -> updateCover(true));

        // 搜索封面按钮 - 打开封面搜索对话框
        searchCoverButton.setOnAction(event -> {
            String title = originalData.getTitle();
            String artist = originalData.getArtist();
            String album = originalData.getAlbum();
            Consumer<byte[]> setCover = coverBytes -> {
                if (coverBytes != null) {
                    Platform.runLater(() -> setCover(coverBytes)); // 在UI线程中设置封面
                }
            };
            SearchCover.show(title, artist, album, setCover);
        });
    }

    /**
     * 初始化确认和取消按钮区域
     */
    private void initConfirmBox() {
        // 确认按钮点击事件 - 保存修改
        confirmButton.setOnAction(event -> {
            if (originalData == null || displayedData == null) return;

            List<EditableTag> changedTags = getChangedTags(); // 获取修改过的标签
            if (changedTags.isEmpty()) return; // 没有修改则不执行任何操作

            exchangeEditableValue(displayedData, originalData); // 将修改同步到原始元数据

            AudioFileWriter.updateTag(originalData, changedTags); // 写入元数据到文件

            showData(originalData); // 刷新显示
            UiCoordinator.refreshTableView(); // 更新中心区域的表格视图
            String msg = changedTags.stream().map(EditableTag::getText).collect(Collectors.joining("、"));
            UiCoordinator.showNotification(msg + " 已修改"); // 显示通知
        });

        // 取消按钮点击事件 - 丢弃修改
        cancelButton.setOnAction(event -> {
            if (originalData == null || displayedData == null) return;
            showData(originalData); // 恢复原始数据
        });

        // 确认按钮区域的布局设置
        confirmBox.setAlignment(Pos.CENTER);
        confirmBox.setSpacing(50);
        confirmBox.setPadding(new Insets(20, 0, 0, 0));
        confirmBox.getChildren().addAll(confirmButton, cancelButton);
    }

    /**
     * 获取修改过的标签名称列表
     *
     * @return 修改过的标签名称列表
     */
    private List<EditableTag> getChangedTags() {
        List<EditableTag> changedTagNames = new ArrayList<>();
        if (originalData != null && displayedData != null) {
            if (!originalData.getTitle().equals(displayedData.getTitle())) {
                changedTagNames.add(EditableTag.TITLE);
            }
            if (!originalData.getArtist().equals(displayedData.getArtist())) {
                changedTagNames.add(EditableTag.ARTIST);
            }
            if (!originalData.getAlbum().equals(displayedData.getAlbum())) {
                changedTagNames.add(EditableTag.ALBUM);
            }
            if (!originalData.getDate().equals(displayedData.getDate())) {
                changedTagNames.add(EditableTag.DATE);
            }
            if (!originalData.getGenre().equals(displayedData.getGenre())) {
                changedTagNames.add(EditableTag.GENRE);
            }
            if (!originalData.getTrack().equals(displayedData.getTrack())) {
                changedTagNames.add(EditableTag.TRACK);
            }
            if (!originalData.getComment().equals(displayedData.getComment())) {
                changedTagNames.add(EditableTag.COMMENT);
            }
            if (!Arrays.equals(originalData.getCover(), displayedData.getCover())) {
                changedTagNames.add(EditableTag.COVER);
            }
        }
        return changedTagNames;
    }

    private void exchangeEditableValue(AudioFileData from, AudioFileData target) {
        target.setTitle(from.getTitle());
        target.setArtist(from.getArtist());
        target.setAlbum(from.getAlbum());
        target.setDate(from.getDate());
        target.setGenre(from.getGenre());
        target.setTrack(from.getTrack());
        target.setComment(from.getComment());
        target.setLyric(from.getLyric());
        target.setCover(from.getCover());
    }

    /**
     * 显示音频文件数据到侧边栏
     *
     * @param original 要显示的音频文件数据
     */
    public void showData(AudioFileData original) {
        originalData = original;
        displayedData = new AudioFileData();
        exchangeEditableValue(original, displayedData); // 复制原始数据到显示用的副本
        resetThenDisplay(displayedData);
    }

    /**
     * 重置侧边栏
     */
    private void resetThenDisplay(AudioFileData data) {
        // 启用所有组合框
        titleComboBox.setDisable(false);
        artistComboBox.setDisable(false);
        albumComboBox.setDisable(false);
        dateComboBox.setDisable(false);
        genreComboBox.setDisable(false);
        trackComboBox.setDisable(false);
        commentComboBox.setDisable(false);

        // 设置各个组合框的值和选项
        // 标题
        titleComboBox.getItems().clear();
        titleComboBox.setValue(data.getTitle());
        titleComboBox.getItems().add("");

        // 艺术家
        artistComboBox.getItems().clear();
        artistComboBox.setValue(data.getArtist());
        artistComboBox.getItems().add("");
        artistComboBox.getItems().addAll(Session.getAlternativeArtists()); // 添加备选艺术家

        // 专辑
        albumComboBox.getItems().clear();
        albumComboBox.setValue(data.getAlbum());
        albumComboBox.getItems().add("");
        albumComboBox.getItems().addAll(Session.getAlternativeAlbums()); // 添加备选专辑

        // 出版日期
        dateComboBox.getItems().clear();
        dateComboBox.setValue(data.getDate());
        dateComboBox.getItems().add("");

        // 流派
        genreComboBox.getItems().clear();
        genreComboBox.setValue(data.getGenre());
        genreComboBox.getItems().add("");
        genreComboBox.getItems().addAll(MusicGenre.getGenres()); // 添加所有预设流派

        // 音轨序号
        trackComboBox.setValue(data.getTrack());
        trackComboBox.getItems().add("");

        // 备注
        commentComboBox.getItems().clear();
        commentComboBox.setValue(data.getComment());
        commentComboBox.getItems().add("");

        // 设置封面
        if (data.getCover() != null) {
            setCover(data.getCover());
        } else {
            showDefaultCover();
        }
    }

    /**
     * 设置封面图片
     *
     * @param coverBytes 封面图片字节数组
     */
    private void setCover(byte[] coverBytes) {
        if (coverBytes == null) {
            showDefaultCover();
            return;
        }

        displayedData.setCover(coverBytes); // 更新显示的元数据中的封面
        coverPanel.getChildren().clear();
        coverPanel.getChildren().add(Session.getImageView(coverBytes));

        // 启用在有封面时可用的按钮
        changeCoverButton.setDisable(false);
        extractCoverButton.setDisable(false);
        deleteCoverButton.setDisable(false);
        searchCoverButton.setDisable(false);
    }

    /**
     * 展示默认封面
     */
    private static final ImageView DEFAULT_COVER =  ImageInApp.getDefaultCover();
    private void showDefaultCover() {
        coverPanel.getChildren().clear();
        coverPanel.setAlignment(Pos.CENTER);
        coverPanel.getChildren().add(DEFAULT_COVER);

        // 设置按钮状态：更换和搜索只在有原始数据时可用，提取和删除在无封面时不可用
        changeCoverButton.setDisable(originalData == null);
        extractCoverButton.setDisable(true);
        deleteCoverButton.setDisable(true);
        searchCoverButton.setDisable(originalData == null);
    }

    /**
     * 显示空白状态
     */
    public void showBlank() {
        showData(new AudioFileData());
        originalData = null;
        titleComboBox.setDisable(true);
        artistComboBox.setDisable(true);
        albumComboBox.setDisable(true);
        dateComboBox.setDisable(true);
        genreComboBox.setDisable(true);
        trackComboBox.setDisable(true);
        commentComboBox.setDisable(true);
        showDefaultCover();
    }

    /**
     * 刷新侧边栏显示
     */
    public void reload() {
        if (originalData != null) {
            showData(originalData);
        } else {
            showBlank();
        }
    }

    /**
     * 获取侧边栏主容器
     *
     * @return 侧边栏 VBox 组件
     */
    public Node getNode() {
        return aside;
    }
}