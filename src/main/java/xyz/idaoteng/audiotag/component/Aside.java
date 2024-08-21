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
import xyz.idaoteng.audiotag.*;
import xyz.idaoteng.audiotag.AudioMetaData.Editable;
import xyz.idaoteng.audiotag.core.MetaDataWriter;

import java.io.*;
import java.util.HashMap;
import java.util.Set;

public class Aside {
    private static final VBox ASIDE = new VBox(5);
    private static final ComboBox<String> TITLE_COMBO_BOX = new ComboBox<>();
    private static final ComboBox<String> ARTIST_COMBO_BOX = new ComboBox<>();
    private static final ComboBox<String> ALBUM_COMBO_BOX = new ComboBox<>();
    private static final ComboBox<String> GENRE_COMBO_BOX = new ComboBox<>();
    private static final ComboBox<String> TRACK_COMBO_BOX = new ComboBox<>();
    private static final HBox COVER_PANEL = new HBox();
    private static final HBox CONFIRM_BOX = new HBox(30);

    private static AudioMetaData META_DATA_DISPLAYED = null;
    private static boolean metaDataChanged = false;
    private static final HashMap<Editable, String> EDITABLE_DATA_TABLE = new HashMap<>();

    private static final ByteArrayOutputStream DEFAULT_COVER = new ByteArrayOutputStream();

    private static final FileChooser.ExtensionFilter COVER_EXTENSION_FILTER;

    private static byte[] newCoverBytes = null;

    private enum ComboBoxType {
        TITLE, ARTIST, ALBUM, GENRE, TRACK
    }

    static {
        try (InputStream inputStream = Session.class.getResourceAsStream("cover.png")) {
            if (inputStream == null) throw new RuntimeException("无法加载默认封面图片");
            inputStream.transferTo(DEFAULT_COVER);
        } catch (Exception e) {
            throw new RuntimeException("无法加载默认封面图片");
        }

        String[] imageExtensions = new String[]{"*.jpg", "*.jpeg", "*.png", "*.bmp", "*.webp"};
        COVER_EXTENSION_FILTER = new FileChooser.ExtensionFilter("图片文件", imageExtensions);

        ASIDE.setMinWidth(250);
        ASIDE.setPadding(new Insets(10, 10, 10, 10));
        ASIDE.setStyle("-fx-border-style: solid; -fx-border-color: #cccccc; -fx-border-width: 1 0 0 1");

        Label titleLabel = new Label("标题");
        TITLE_COMBO_BOX.setMinWidth(240);
        TITLE_COMBO_BOX.setEditable(true);
        configComboBoxAction(ComboBoxType.TITLE, TITLE_COMBO_BOX);

        Label artistLabel = new Label("艺术家");
        ARTIST_COMBO_BOX.setMinWidth(240);
        ARTIST_COMBO_BOX.setEditable(true);
        configComboBoxAction(ComboBoxType.ARTIST, ARTIST_COMBO_BOX);

        Label albumLabel = new Label("专辑");
        ALBUM_COMBO_BOX.setMinWidth(240);
        ALBUM_COMBO_BOX.setEditable(true);
        configComboBoxAction(ComboBoxType.ALBUM, ALBUM_COMBO_BOX);

        Label genreLabel = new Label("流派");
        GENRE_COMBO_BOX.setEditable(true);
        GENRE_COMBO_BOX.getItems().addAll(CommonMusicGenres.getGenres());
        configComboBoxAction(ComboBoxType.GENRE, GENRE_COMBO_BOX);
        VBox genrePanel = new VBox(5);
        genrePanel.setMinWidth(155);
        genrePanel.getChildren().addAll(genreLabel, GENRE_COMBO_BOX);

        Label trackLabel = new Label("音轨序号");
        TRACK_COMBO_BOX.setEditable(true);
        TRACK_COMBO_BOX.getItems().addAll("1", "2", "3", "4", "5", "6", "7", "8", "9", "10");
        configComboBoxAction(ComboBoxType.TRACK, TRACK_COMBO_BOX);
        VBox trackPanel = new VBox(5);
        trackPanel.getChildren().addAll(trackLabel, TRACK_COMBO_BOX);

        HBox genreAndTrack = new HBox(10);
        genreAndTrack.setMaxWidth(240);
        genreAndTrack.getChildren().addAll(genrePanel, trackPanel);

        Label coverLabel = new Label("封面");
        COVER_PANEL.setMaxHeight(200);
        COVER_PANEL.setMaxWidth(200);
        COVER_PANEL.setMinHeight(200);
        COVER_PANEL.setMinWidth(200);
        COVER_PANEL.setStyle("-fx-border-style: solid; -fx-border-color: #cccccc; -fx-border-width: 1 1 1 1");
        setDefaultCover();
        configCoverPanelAction();

        VBox coverOptions = new VBox(15);
        Button changeCoverButton = new Button("更换");
        Button extractCoverButton = new Button("提取");
        Button deleteCoverButton = new Button("删除");
        coverOptions.setAlignment(Pos.CENTER);
        coverOptions.getChildren().addAll(changeCoverButton, extractCoverButton, deleteCoverButton);
        configCoverOptionsAction(changeCoverButton, extractCoverButton, deleteCoverButton);

        HBox coverPanelAndOptions = new HBox(3);
        coverPanelAndOptions.getChildren().addAll(COVER_PANEL, coverOptions);

        initConfirmBox();

        ASIDE.getChildren().addAll(titleLabel, TITLE_COMBO_BOX, artistLabel, ARTIST_COMBO_BOX, albumLabel,
                ALBUM_COMBO_BOX, genreAndTrack, coverLabel, coverPanelAndOptions, CONFIRM_BOX);

        initEditableData();
    }

    private static void configCoverOptionsAction(Button changeButton, Button extractButton, Button deleteButton) {
        changeButton.setOnAction(event -> updateCover(false));

        extractButton.setOnAction(event -> {
            if (META_DATA_DISPLAYED != null) {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("保存封面");
                fileChooser.getExtensionFilters().addAll(COVER_EXTENSION_FILTER);
                File file = fileChooser.showSaveDialog(StartUp.getPrimaryStage());
                if (file != null) {
                    Utils.saveCover(META_DATA_DISPLAYED.getCover(), file);
                }
            }
        });

        deleteButton.setOnAction(event -> updateCover(true));
    }

    private static void configCoverPanelAction() {
        COVER_PANEL.setOnMouseClicked(event -> {
            if (event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 1) {
                updateCover(false);
            }
        });
    }

    private static void updateCover(boolean delete) {
        if (delete) {
            setDefaultCover();
            newCoverBytes = new byte[0];
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("选择封面");
        fileChooser.getExtensionFilters().addAll(COVER_EXTENSION_FILTER);
        File file = fileChooser.showOpenDialog(StartUp.getPrimaryStage());
        if (file != null) {
            newCoverBytes = Utils.retouchCover(file);
            setCover(newCoverBytes);
            metaDataChanged = true;
        }
    }

    private static void initEditableData() {
        EDITABLE_DATA_TABLE.put(Editable.TITLE, null);
        EDITABLE_DATA_TABLE.put(Editable.ARTIST, null);
        EDITABLE_DATA_TABLE.put(Editable.ALBUM, null);
        EDITABLE_DATA_TABLE.put(Editable.GENRE, null);
        EDITABLE_DATA_TABLE.put(Editable.TRACK, null);
        newCoverBytes = null;
    }

    private static void initConfirmBox() {
        Button confirmButton = new Button("确认");
        confirmButton.setOnAction(event -> {
            if (!metaDataChanged || META_DATA_DISPLAYED == null) return;

            Set<Editable> keys = EDITABLE_DATA_TABLE.keySet();
            keys.forEach(key -> {
                String newValue = EDITABLE_DATA_TABLE.get(key);
                if (newValue != null) {
                    META_DATA_DISPLAYED.set(key, newValue);
                }
            });

            if (newCoverBytes != null) {
                if (newCoverBytes.length == 0) {
                    META_DATA_DISPLAYED.setCover(null);
                } else {
                    META_DATA_DISPLAYED.setCover(newCoverBytes);
                }
            }
            MetaDataWriter.write(META_DATA_DISPLAYED);
            Center.getTableView().refresh();
            metaDataChanged = false;
        });

        Button cancelButton = new Button("取消");
        cancelButton.setOnAction(event -> {
            if (META_DATA_DISPLAYED == null) return;

            TITLE_COMBO_BOX.setValue(META_DATA_DISPLAYED.getTitle());
            ARTIST_COMBO_BOX.setValue(META_DATA_DISPLAYED.getArtist());
            ALBUM_COMBO_BOX.setValue(META_DATA_DISPLAYED.getAlbum());
            GENRE_COMBO_BOX.setValue(META_DATA_DISPLAYED.getGenre());
            TRACK_COMBO_BOX.setValue(META_DATA_DISPLAYED.getTrack());
            setCover(META_DATA_DISPLAYED.getCover());
            initEditableData();
            metaDataChanged = false;
        });

        CONFIRM_BOX.setAlignment(Pos.CENTER);
        CONFIRM_BOX.getChildren().addAll(confirmButton, cancelButton);
    }

    private static void configComboBoxAction(ComboBoxType type, ComboBox<String> comboBox) {
        comboBox.setOnAction(event -> {
            String newValue = comboBox.getValue();
            if (newValue != null) {
                switch (type) {
                    case TITLE -> EDITABLE_DATA_TABLE.put(Editable.TITLE, newValue);
                    case ARTIST -> EDITABLE_DATA_TABLE.put(Editable.ARTIST, newValue);
                    case ALBUM -> EDITABLE_DATA_TABLE.put(Editable.ALBUM, newValue);
                    case GENRE -> EDITABLE_DATA_TABLE.put(Editable.GENRE, newValue);
                    case TRACK -> EDITABLE_DATA_TABLE.put(Editable.TRACK, newValue);
                }
                metaDataChanged = true;
            }
        });
    }

    private static void setDefaultCover() {
        COVER_PANEL.getChildren().clear();
        ImageView cover = new ImageView();
        cover.setFitWidth(90);
        cover.setFitHeight(90);
        cover.setImage(new Image(new ByteArrayInputStream(DEFAULT_COVER.toByteArray())));
        COVER_PANEL.setAlignment(Pos.CENTER);
        COVER_PANEL.getChildren().add(cover);
    }

    private static void setCover(byte[] coverBytes) {
        COVER_PANEL.getChildren().clear();
        ImageView cover = new ImageView();
        cover.setFitWidth(200);
        cover.setFitHeight(200);
        cover.setImage(new Image(new ByteArrayInputStream(coverBytes)));
        COVER_PANEL.getChildren().add(cover);
    }

    public static void showMetaData(AudioMetaData metaData) {
        initEditableData();
        META_DATA_DISPLAYED = metaData;
        metaDataChanged = false;

        TITLE_COMBO_BOX.setValue(metaData.getTitle());

        ARTIST_COMBO_BOX.setValue(metaData.getArtist());
        ARTIST_COMBO_BOX.getItems().addAll(Head.getAlternativeArtists());

        ALBUM_COMBO_BOX.setValue(metaData.getAlbum());
        ALBUM_COMBO_BOX.getItems().addAll(Head.getAlternativeAlbums());

        GENRE_COMBO_BOX.setValue(metaData.getGenre());

        TRACK_COMBO_BOX.setValue(metaData.getTrack());

        if (metaData.getCover() != null) {
            setCover(metaData.getCover());
        } else {
            setDefaultCover();
        }
    }

    public static VBox getAside() {
        return ASIDE;
    }
}
