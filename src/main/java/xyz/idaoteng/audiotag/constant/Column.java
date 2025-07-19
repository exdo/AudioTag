package xyz.idaoteng.audiotag.constant;

import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import xyz.idaoteng.audiotag.bean.AudioFileData;

import java.util.HashMap;

public enum Column {
    FILENAME(ColumnNameText.FILENAME),
    ARTIST(ColumnNameText.ARTIST),
    TITLE(ColumnNameText.TITLE),
    ALBUM(ColumnNameText.ALBUM),
    DATE(ColumnNameText.DATE),
    GENRE(ColumnNameText.GENRE),
    TRACK(ColumnNameText.TRACK),
    COMMENT(ColumnNameText.COMMENT),
    BITRATE(ColumnNameText.BITRATE),
    LENGTH(ColumnNameText.LENGTH),
    FORMAT(ColumnNameText.FORMAT),
    SIZE(ColumnNameText.SIZE);

    private final String name;

    Column(String name) {
        this.name = name;
    }

    private static HashMap<String, TableColumn<AudioFileData, String>> nameToColumnMap() {
        // 文件名列
        TableColumn<AudioFileData, String> filenameColumn = new TableColumn<>(ColumnNameText.FILENAME);
        filenameColumn.setPrefWidth(235);
        filenameColumn.setCellValueFactory(new PropertyValueFactory<>("filename"));
        filenameColumn.setReorderable(false);

        // 艺术家列
        TableColumn<AudioFileData, String> artistColumn = new TableColumn<>(ColumnNameText.ARTIST);
        artistColumn.setPrefWidth(150);
        artistColumn.setCellValueFactory(new PropertyValueFactory<>("artist"));

        // 标题列
        TableColumn<AudioFileData, String> titleColumn = new TableColumn<>(ColumnNameText.TITLE);
        titleColumn.setPrefWidth(175);
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));

        // 专辑列
        TableColumn<AudioFileData, String> albumColumn = new TableColumn<>(ColumnNameText.ALBUM);
        albumColumn.setPrefWidth(175);
        albumColumn.setCellValueFactory(new PropertyValueFactory<>("album"));

        // 序号列
        TableColumn<AudioFileData, String> trackColumn = new TableColumn<>(ColumnNameText.TRACK);
        trackColumn.setPrefWidth(75);
        trackColumn.setCellValueFactory(new PropertyValueFactory<>("track"));

        // 流派列
        TableColumn<AudioFileData, String> genreColumn = new TableColumn<>(ColumnNameText.GENRE);
        genreColumn.setPrefWidth(85);
        genreColumn.setCellValueFactory(new PropertyValueFactory<>("genre"));

        // 发行日期列
        TableColumn<AudioFileData, String> dateColumn = new TableColumn<>(ColumnNameText.DATE);
        dateColumn.setPrefWidth(100);
        dateColumn.setStyle("-fx-alignment: CENTER-RIGHT");
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));

        // 备注列
        TableColumn<AudioFileData, String> commentColumn = new TableColumn<>(ColumnNameText.COMMENT);
        commentColumn.setPrefWidth(200);
        commentColumn.setCellValueFactory(new PropertyValueFactory<>("comment"));

        // 比特率列
        TableColumn<AudioFileData, String> bitrateColumn = new TableColumn<>(ColumnNameText.BITRATE);
        bitrateColumn.setPrefWidth(105);
        bitrateColumn.setStyle("-fx-alignment: CENTER-RIGHT");
        bitrateColumn.setCellValueFactory(new PropertyValueFactory<>("bitrate"));

        // 时长列
        TableColumn<AudioFileData, String> lengthColumn = new TableColumn<>(ColumnNameText.LENGTH);
        lengthColumn.setPrefWidth(65);
        lengthColumn.setStyle("-fx-alignment: CENTER-RIGHT");
        lengthColumn.setCellValueFactory(new PropertyValueFactory<>("length"));

        // 格式列
        TableColumn<AudioFileData, String> formatColumn = new TableColumn<>(ColumnNameText.FORMAT);
        formatColumn.setPrefWidth(50);
        formatColumn.setStyle("-fx-alignment: CENTER-RIGHT");
        formatColumn.setCellValueFactory(new PropertyValueFactory<>("format"));

        // 文件大小列
        TableColumn<AudioFileData, String> sizeColumn = new TableColumn<>(ColumnNameText.SIZE);
        sizeColumn.setPrefWidth(75);
        sizeColumn.setStyle("-fx-alignment: CENTER-RIGHT");
        sizeColumn.setCellValueFactory(new PropertyValueFactory<>("size"));

        HashMap<String, TableColumn<AudioFileData, String>> idToColumnMap = new HashMap<>(12);

        idToColumnMap.put(ColumnNameText.FILENAME, filenameColumn);
        idToColumnMap.put(ColumnNameText.ARTIST, artistColumn);
        idToColumnMap.put(ColumnNameText.TITLE, titleColumn);
        idToColumnMap.put(ColumnNameText.ALBUM, albumColumn);
        idToColumnMap.put(ColumnNameText.TRACK, trackColumn);
        idToColumnMap.put(ColumnNameText.GENRE, genreColumn);
        idToColumnMap.put(ColumnNameText.DATE, dateColumn);
        idToColumnMap.put(ColumnNameText.COMMENT, commentColumn);
        idToColumnMap.put(ColumnNameText.BITRATE, bitrateColumn);
        idToColumnMap.put(ColumnNameText.LENGTH, lengthColumn);
        idToColumnMap.put(ColumnNameText.FORMAT, formatColumn);
        idToColumnMap.put(ColumnNameText.SIZE, sizeColumn);

        return idToColumnMap;
    }

    public static TableColumn<AudioFileData, String> getColumnByName(String name) {
        return nameToColumnMap().get(name);
    }

    public String getName() {
        return name;
    }
}
