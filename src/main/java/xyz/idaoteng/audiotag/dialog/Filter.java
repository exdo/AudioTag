package xyz.idaoteng.audiotag.dialog;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import xyz.idaoteng.audiotag.Session;
import xyz.idaoteng.audiotag.bean.AudioMetaData;
import xyz.idaoteng.audiotag.component.Center;

import java.util.ArrayList;
import java.util.List;

public class Filter {
    private static final Stage STAGE = new Stage();
    private static final VBox BODY = new VBox(10);
    private static final CheckBox TITLE_CHECK_BOX = new CheckBox("标题 ：");
    private static final TextField TITLE_TEXT_FIELD = new TextField();
    private static final ComboBox<String> ARTIST_COMBO_BOX = new ComboBox<>();
    private static final CheckBox ARTIST_CHECK_BOX = new CheckBox("艺术家=");
    private static final ComboBox<String> ALBUM_COMBO_BOX = new ComboBox<>();
    private static final CheckBox ALBUM_CHECK_BOX = new CheckBox("专辑 =");
    private static final ComboBox<String> GENRE_COMBO_BOX = new ComboBox<>();
    private static final CheckBox GENRE_CHECK_BOX = new CheckBox("流派 =");
    private static final CheckBox COVER_CHECK_BOX = new CheckBox("封面为空");

    private static final Font FONT = new Font(13);

    private static final List<AudioMetaData> ALL_ITEMS = new ArrayList<>();
    private static final List<AudioMetaData> FILTERED_ITEMS = new ArrayList<>();

    private static Button filterButton;
    private static final String FILTER_ON = "过滤";
    private static final String FILTER_OFF = "关闭过滤";

    static {
        Label LABEL = new Label("请勾选需要参与过滤的条件");
        LABEL.setFont(FONT);

        VBox allCheckBox = new VBox(18);
        allCheckBox.setMinWidth(70);
        allCheckBox.setMaxWidth(70);
        allCheckBox.getChildren().addAll(TITLE_CHECK_BOX, ARTIST_CHECK_BOX, ALBUM_CHECK_BOX,
                GENRE_CHECK_BOX, COVER_CHECK_BOX);

        TITLE_CHECK_BOX.setOnAction(event -> TITLE_TEXT_FIELD.setDisable(!TITLE_CHECK_BOX.isSelected()));

        ARTIST_CHECK_BOX.setOnAction(event -> ARTIST_COMBO_BOX.setDisable(!ARTIST_CHECK_BOX.isSelected()));

        ALBUM_CHECK_BOX.setOnAction(event -> ALBUM_COMBO_BOX.setDisable(!ALBUM_CHECK_BOX.isSelected()));

        GENRE_CHECK_BOX.setOnAction(event -> GENRE_COMBO_BOX.setDisable(!GENRE_CHECK_BOX.isSelected()));

        TITLE_TEXT_FIELD.setMinWidth(275);
        TITLE_TEXT_FIELD.setMinWidth(275);
        TITLE_TEXT_FIELD.setDisable(true);

        ARTIST_COMBO_BOX.setMinWidth(250);
        ARTIST_COMBO_BOX.setMaxWidth(250);
        ARTIST_COMBO_BOX.setEditable(true);
        ARTIST_COMBO_BOX.setDisable(true);

        ALBUM_COMBO_BOX.setMinWidth(275);
        ALBUM_COMBO_BOX.setMaxWidth(275);
        ALBUM_COMBO_BOX.setEditable(true);
        ALBUM_COMBO_BOX.setDisable(true);

        GENRE_COMBO_BOX.setMinWidth(200);
        GENRE_COMBO_BOX.setMaxWidth(200);
        GENRE_COMBO_BOX.setEditable(true);
        GENRE_COMBO_BOX.setDisable(true);

        VBox optionHBox = new VBox(10);
        optionHBox.getChildren().addAll(TITLE_TEXT_FIELD, ARTIST_COMBO_BOX, ALBUM_COMBO_BOX, GENRE_COMBO_BOX);

        HBox mainContent = new HBox(10);
        mainContent.getChildren().addAll(allCheckBox, optionHBox);

        Button confirmButton = new Button("确定");
        confirmButton.setFont(FONT);
        configConfirmButton(confirmButton);
        Button cancelButton = new Button("取消");
        cancelButton.setFont(FONT);
        cancelButton.setOnAction(event -> {
            STAGE.close();
            filterButton.setText(FILTER_ON);
        });
        HBox buttonHBox = new HBox(confirmButton, cancelButton);
        buttonHBox.setSpacing(10);
        buttonHBox.setAlignment(Pos.CENTER_RIGHT);
        buttonHBox.setPadding(new Insets(15, 0, 0, 0));

        BODY.setPadding(new Insets(10, 15, 0, 15));
        BODY.getChildren().addAll(LABEL, mainContent, buttonHBox);
        Scene scene = new Scene(BODY, 380, 270);
        STAGE.setScene(scene);
        STAGE.setTitle("过滤");
        STAGE.setResizable(false);
        STAGE.initModality(Modality.APPLICATION_MODAL);
    }

    private static void configConfirmButton(Button confirmButton) {
        confirmButton.setOnAction(event -> {
            FILTERED_ITEMS.clear();
            List<AudioMetaData> filtered = ALL_ITEMS.stream().filter(metaData -> {
                if (ARTIST_CHECK_BOX.isSelected()) {
                    return metaData.getArtist().contains(ARTIST_COMBO_BOX.getValue());
                } else {
                    return true;
                }
            }).filter(metaData -> {
                if (ALBUM_CHECK_BOX.isSelected()) {
                    return metaData.getAlbum().contains(ALBUM_COMBO_BOX.getValue());
                } else {
                    return true;
                }
            }).filter(metaData -> {
                if (GENRE_CHECK_BOX.isSelected()) {
                    return metaData.getGenre().contains(GENRE_COMBO_BOX.getValue());
                } else {
                    return true;
                }
            }).filter(metaData -> {
                if (TITLE_CHECK_BOX.isSelected()) {
                    if ("".equals(TITLE_TEXT_FIELD.getText())) {
                        return metaData.getTitle().equals("");
                    } else {
                        return metaData.getTitle().contains(TITLE_TEXT_FIELD.getText());
                    }
                } else {
                    return true;
                }
            }).filter(metaData -> {
                if (COVER_CHECK_BOX.isSelected()) {
                    return metaData.getCover() == null;
                } else {
                    return true;
                }
            }).toList();
            FILTERED_ITEMS.addAll(filtered);
            Center.updateTableView(FILTERED_ITEMS);
            filterButton.setText(FILTER_OFF);
            STAGE.close();
        });
    }

    public static void show(List<AudioMetaData> dataList) {
        ALL_ITEMS.clear();
        ALL_ITEMS.addAll(dataList);

        ARTIST_COMBO_BOX.getItems().clear();
        ARTIST_COMBO_BOX.getItems().add("");
        ARTIST_COMBO_BOX.getItems().addAll(Center.getAlternativeArtists());

        ALBUM_COMBO_BOX.getItems().clear();
        ALBUM_COMBO_BOX.getItems().add("");
        ALBUM_COMBO_BOX.getItems().addAll(Center.getAlternativeAlbums());

        GENRE_COMBO_BOX.getItems().clear();
        GENRE_COMBO_BOX.getItems().add("");
        GENRE_COMBO_BOX.getItems().addAll(Session.getAlternativeGenres());

        STAGE.showAndWait();
    }

    public static void takeOverFilterButton(Button filter) {
        filterButton = filter;
        filterButton.setOnAction(event -> {
            switch (filter.getText()) {
                case FILTER_ON -> Center.filter();
                case FILTER_OFF -> {
                    Center.turnOffFilter();
                    filter.setText(FILTER_ON);
                }
            }
        });
    }
}
