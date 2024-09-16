package xyz.idaoteng.audiotag.component;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
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
import xyz.idaoteng.audiotag.constant.MusicGenre;
import xyz.idaoteng.audiotag.core.MetaDataWriter;
import xyz.idaoteng.audiotag.dialog.SelectCover;

import java.io.ByteArrayInputStream;
import java.io.File;

public class Aside {
    private static final VBox ASIDE = new VBox(5);
    private static final ComboBox<String> TITLE_COMBO_BOX = new ComboBox<>();
    private static final ComboBox<String> ARTIST_COMBO_BOX = new ComboBox<>();
    private static final ComboBox<String> ALBUM_COMBO_BOX = new ComboBox<>();
    private static final ComboBox<String> DATE_COMBO_BOX = new ComboBox<>();
    private static final ComboBox<String> GENRE_COMBO_BOX = new ComboBox<>();
    private static final ComboBox<String> TRACK_COMBO_BOX = new ComboBox<>();
    private static final ComboBox<String> COMMENT_COMBO_BOX = new ComboBox<>();
    private static final HBox COVER_PANEL = new HBox();
    private static final Button CHANGE_COVER_BUTTON = new Button("更换");
    private static final Button EXTRACT_COVER_BUTTON = new Button("提取");
    private static final Button DELETE_COVER_BUTTON = new Button("删除");
    private static final Button SEARCH_COVER_BUTTON = new Button("搜索");
    private static final Button CONFIRM_BUTTON = new Button("确认更改");
    private static final Button CANCEL_BUTTON = new Button("取消");
    private static final HBox CONFIRM_BOX = new HBox();

    private static AudioMetaData originalMetaData = null;
    private static AudioMetaData metaDataDisplayed = null;

    private static final FileChooser.ExtensionFilter COVER_EXTENSION_FILTER;

    private enum ComboBoxType {
        TITLE, ARTIST, ALBUM, DATE, GENRE, TRACK, COMMENT
    }

    // 初始化侧边栏
    static {
        // 封面文件类型过滤器
        String[] imageExtensions = new String[]{"*.jpg", "*.jpeg", "*.png", "*.bmp", "*.webp"};
        COVER_EXTENSION_FILTER = new FileChooser.ExtensionFilter("图片文件", imageExtensions);
        // 设置侧边栏样式
        ASIDE.setMinWidth(250);
        ASIDE.setPadding(new Insets(10, 10, 10, 10));
        ASIDE.setStyle("-fx-border-style: solid; -fx-border-color: #cccccc; -fx-border-width: 1 0 0 1");
        // 配置侧边栏组件（渲染效果与书写顺序一致）
        Label titleLabel = new Label("标题");
        configComboBox(ComboBoxType.TITLE, TITLE_COMBO_BOX, true);

        Label artistLabel = new Label("艺术家");
        configComboBox(ComboBoxType.ARTIST, ARTIST_COMBO_BOX, true);

        Label albumLabel = new Label("专辑");
        configComboBox(ComboBoxType.ALBUM, ALBUM_COMBO_BOX, true);

        Label dateLabel = new Label("出版日期");
        configComboBox(ComboBoxType.DATE, DATE_COMBO_BOX, true);

        Label genreLabel = new Label("流派");
        GENRE_COMBO_BOX.getItems().addAll(MusicGenre.getGenres());
        configComboBox(ComboBoxType.GENRE, GENRE_COMBO_BOX, false);
        VBox genrePanel = new VBox(5);
        genrePanel.setMinWidth(165);
        genrePanel.setMaxWidth(165);
        genrePanel.getChildren().addAll(genreLabel, GENRE_COMBO_BOX);

        Label trackLabel = new Label("音轨序号");
        TRACK_COMBO_BOX.getItems().addAll("1", "2", "3", "4", "5", "6", "7", "8", "9", "10");
        configComboBox(ComboBoxType.TRACK, TRACK_COMBO_BOX, false);
        VBox trackPanel = new VBox(5);
        trackPanel.getChildren().addAll(trackLabel, TRACK_COMBO_BOX);

        HBox genreAndTrack = new HBox(10);
        genreAndTrack.setMaxWidth(240);
        genreAndTrack.getChildren().addAll(genrePanel, trackPanel);

        Label commentLabel = new Label("备注");
        configComboBox(ComboBoxType.COMMENT, COMMENT_COMBO_BOX, true);

        Label coverLabel = new Label("封面");
        COVER_PANEL.setMaxHeight(200);
        COVER_PANEL.setMaxWidth(200);
        COVER_PANEL.setMinHeight(200);
        COVER_PANEL.setMinWidth(200);
        COVER_PANEL.setStyle("-fx-border-style: solid; -fx-border-color: #cccccc; -fx-border-width: 1 1 1 1");
        configCoverPanelActionHandle();

        VBox coverOptions = new VBox(15);
        coverOptions.setAlignment(Pos.CENTER);
        coverOptions.getChildren().addAll(CHANGE_COVER_BUTTON, EXTRACT_COVER_BUTTON,
                DELETE_COVER_BUTTON, SEARCH_COVER_BUTTON);
        configCoverOptionActionHandle();

        HBox coverPanelAndOptions = new HBox(3);
        coverPanelAndOptions.getChildren().addAll(COVER_PANEL, coverOptions);

        initConfirmBox();

        ASIDE.setMinWidth(265);
        ASIDE.setMaxWidth(265);
        ASIDE.getChildren().addAll(titleLabel, TITLE_COMBO_BOX, artistLabel, ARTIST_COMBO_BOX, albumLabel,
                ALBUM_COMBO_BOX, dateLabel, DATE_COMBO_BOX, genreAndTrack, commentLabel, COMMENT_COMBO_BOX,
                coverLabel, coverPanelAndOptions, CONFIRM_BOX);

        showBlank();
    }

    private static void configComboBox(ComboBoxType type, ComboBox<String> comboBox, boolean defaultSize) {
        if (defaultSize) {
            comboBox.setMinWidth(240);
            comboBox.setMaxWidth(240);
        }

        comboBox.setEditable(true);

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

    private static void configCoverPanelActionHandle() {
        COVER_PANEL.setOnMouseClicked(event -> {
            if (metaDataDisplayed == null) return;

            if (event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 1) {
                updateCover(false);
            }
        });
    }

    private static void updateCover(boolean delete) {
        if (originalMetaData == null) return;

        if (delete) {
            setDefaultCover();
        } else {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("选择封面");
            String path = Session.getFolderPathOfTheLastSelectedImage();
            fileChooser.setInitialDirectory(new File(path));
            fileChooser.getExtensionFilters().addAll(COVER_EXTENSION_FILTER);
            File file = fileChooser.showOpenDialog(StartUp.getPrimaryStage());
            if (file != null) {
                setCover(Utils.retouchCover(file));
                Session.setFolderPathOfTheLastSelectedImage(file.getParentFile().getAbsolutePath());
            }
        }
    }

    private static void configCoverOptionActionHandle() {
        CHANGE_COVER_BUTTON.setOnAction(event -> updateCover(false));

        EXTRACT_COVER_BUTTON.setOnAction(event -> {
            if (originalMetaData != null) {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("保存封面");
                String path = Session.getLastSelectedImageSavingPath();
                fileChooser.setInitialDirectory(new File(path));
                fileChooser.getExtensionFilters().addAll(COVER_EXTENSION_FILTER);
                File file = fileChooser.showSaveDialog(StartUp.getPrimaryStage());
                if (file != null) {
                    Utils.saveCover(originalMetaData.getCover(), file);
                    Session.setLastSelectedImageSavingPath(file.getParentFile().getAbsolutePath());
                }
            }
        });

        DELETE_COVER_BUTTON.setOnAction(event -> updateCover(true));

        SEARCH_COVER_BUTTON.setOnAction(event -> {
            byte[] coverBytes = SelectCover.show(originalMetaData.getTitle(), originalMetaData.getArtist(), originalMetaData.getAlbum());
            if (coverBytes != null) {
                setCover(coverBytes);
            }
        });
    }

    private static void initConfirmBox() {
        CONFIRM_BUTTON.setOnAction(event -> {
            if (originalMetaData == null || metaDataDisplayed == null) return;

            exchangeEditableValue(metaDataDisplayed, originalMetaData);

            MetaDataWriter.write(originalMetaData);

            showMetaData(originalMetaData);
            Center.updateTableView(null);
            Center.selectItem(originalMetaData);
        });

        CANCEL_BUTTON.setOnAction(event -> {
            if (originalMetaData == null || metaDataDisplayed == null) return;

            showMetaData(originalMetaData);
        });

        CONFIRM_BOX.setAlignment(Pos.CENTER);
        CONFIRM_BOX.setSpacing(50);
        CONFIRM_BOX.setPadding(new Insets(20, 0, 0, 0));
        CONFIRM_BOX.getChildren().addAll(CONFIRM_BUTTON, CANCEL_BUTTON);
    }

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

    public static void showMetaData(AudioMetaData original) {
        originalMetaData = original;
        metaDataDisplayed = new AudioMetaData();
        exchangeEditableValue(original, metaDataDisplayed);

        TITLE_COMBO_BOX.setDisable(false);
        ARTIST_COMBO_BOX.setDisable(false);
        ALBUM_COMBO_BOX.setDisable(false);
        DATE_COMBO_BOX.setDisable(false);
        GENRE_COMBO_BOX.setDisable(false);
        TRACK_COMBO_BOX.setDisable(false);
        COMMENT_COMBO_BOX.setDisable(false);

        TITLE_COMBO_BOX.getItems().clear();
        TITLE_COMBO_BOX.setValue(original.getTitle());
        TITLE_COMBO_BOX.getItems().add("");

        ARTIST_COMBO_BOX.getItems().clear();
        ARTIST_COMBO_BOX.setValue(original.getArtist());
        ARTIST_COMBO_BOX.getItems().add("");
        ARTIST_COMBO_BOX.getItems().addAll(Center.getAlternativeArtists());

        ALBUM_COMBO_BOX.getItems().clear();
        ALBUM_COMBO_BOX.setValue(original.getAlbum());
        ALBUM_COMBO_BOX.getItems().add("");
        ALBUM_COMBO_BOX.getItems().addAll(Center.getAlternativeAlbums());

        DATE_COMBO_BOX.getItems().clear();
        DATE_COMBO_BOX.setValue(original.getDate());
        DATE_COMBO_BOX.getItems().add("");

        GENRE_COMBO_BOX.getItems().clear();
        GENRE_COMBO_BOX.setValue(original.getGenre());
        GENRE_COMBO_BOX.getItems().add("");
        GENRE_COMBO_BOX.getItems().addAll(MusicGenre.getGenres());

        TRACK_COMBO_BOX.setValue(original.getTrack());
        TRACK_COMBO_BOX.getItems().add("");

        COMMENT_COMBO_BOX.getItems().clear();
        COMMENT_COMBO_BOX.setValue(original.getComment());
        COMMENT_COMBO_BOX.getItems().add("");

        if (original.getCover() != null) {
            setCover(original.getCover());
        } else {
            setDefaultCover();
        }

    }

    private static void setCover(byte[] coverBytes) {
        COVER_PANEL.getChildren().clear();
        ImageView cover = new ImageView();
        cover.setFitWidth(200);
        cover.setFitHeight(200);
        cover.setImage(new Image(new ByteArrayInputStream(coverBytes)));
        COVER_PANEL.getChildren().add(cover);

        metaDataDisplayed.setCover(coverBytes);

        CHANGE_COVER_BUTTON.setDisable(false);
        EXTRACT_COVER_BUTTON.setDisable(false);
        DELETE_COVER_BUTTON.setDisable(false);
        SEARCH_COVER_BUTTON.setDisable(false);
    }

    private static void setDefaultCover() {
        COVER_PANEL.getChildren().clear();
        COVER_PANEL.setAlignment(Pos.CENTER);
        COVER_PANEL.getChildren().add(ImageInApp.getDefaultCover());

        if (metaDataDisplayed != null) {
            metaDataDisplayed.setCover(null);
        }

        CHANGE_COVER_BUTTON.setDisable(originalMetaData == null);
        EXTRACT_COVER_BUTTON.setDisable(true);
        DELETE_COVER_BUTTON.setDisable(true);
        SEARCH_COVER_BUTTON.setDisable(originalMetaData == null);
    }

    public static void showBlank() {
        showMetaData(new AudioMetaData());
        originalMetaData = null;

        TITLE_COMBO_BOX.setDisable(true);
        ARTIST_COMBO_BOX.setDisable(true);
        ALBUM_COMBO_BOX.setDisable(true);
        DATE_COMBO_BOX.setDisable(true);
        GENRE_COMBO_BOX.setDisable(true);
        TRACK_COMBO_BOX.setDisable(true);
        COMMENT_COMBO_BOX.setDisable(true);

        setDefaultCover();
    }

    public static void refresh() {
        if (originalMetaData != null) {
            showMetaData(originalMetaData);
        } else {
            showBlank();
        }
    }

    public static VBox getAside() {
        return ASIDE;
    }
}
