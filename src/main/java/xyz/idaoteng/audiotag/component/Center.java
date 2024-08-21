package xyz.idaoteng.audiotag.component;

import javafx.scene.Node;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import xyz.idaoteng.audiotag.AudioMetaData;
import xyz.idaoteng.audiotag.Session;
import xyz.idaoteng.audiotag.core.MetaDataReader;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Center {
    private static final TableView<AudioMetaData> TABLE_VIEW = new TableView<>();

    static {
        TABLE_VIEW.setStyle("-fx-border-style: solid; -fx-border-color: #cccccc; -fx-border-width: 0 1 0 0");

        TableColumn<AudioMetaData, String> filenameColumn = new TableColumn<>("文件名");
        filenameColumn.setPrefWidth(235);
        filenameColumn.setCellValueFactory(new PropertyValueFactory<>("filename"));

        TableColumn<AudioMetaData, String> titleColumn = new TableColumn<>("标题");
        titleColumn.setPrefWidth(175);
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));

        TableColumn<AudioMetaData, String> artistColumn = new TableColumn<>("艺术家");
        artistColumn.setPrefWidth(150);
        artistColumn.setCellValueFactory(new PropertyValueFactory<>("artist"));

        TableColumn<AudioMetaData, String> albumColumn = new TableColumn<>("专辑");
        albumColumn.setPrefWidth(175);
        albumColumn.setCellValueFactory(new PropertyValueFactory<>("album"));

        TableColumn<AudioMetaData, String> genreColumn = new TableColumn<>("流派");
        genreColumn.setPrefWidth(85);
        genreColumn.setCellValueFactory(new PropertyValueFactory<>("genre"));

        TableColumn<AudioMetaData, String> trackColumn = new TableColumn<>("音轨");
        trackColumn.setPrefWidth(55);
        trackColumn.setCellValueFactory(new PropertyValueFactory<>("track"));

        TableColumn<AudioMetaData, String> bitrateColumn = new TableColumn<>("比特率");
        bitrateColumn.setPrefWidth(75);
        bitrateColumn.setStyle("-fx-alignment: CENTER-RIGHT");
        bitrateColumn.setCellValueFactory(new PropertyValueFactory<>("bitrate"));

        TableColumn<AudioMetaData, String> lengthColumn = new TableColumn<>("时长");
        lengthColumn.setPrefWidth(65);
        lengthColumn.setStyle("-fx-alignment: CENTER-RIGHT");
        lengthColumn.setCellValueFactory(new PropertyValueFactory<>("length"));

        TABLE_VIEW.getColumns().add(filenameColumn);
        TABLE_VIEW.getColumns().add(titleColumn);
        TABLE_VIEW.getColumns().add(artistColumn);
        TABLE_VIEW.getColumns().add(bitrateColumn);
        TABLE_VIEW.getColumns().add(lengthColumn);
        TABLE_VIEW.getColumns().add(albumColumn);
        TABLE_VIEW.getColumns().add(genreColumn);
        TABLE_VIEW.getColumns().add(trackColumn);

        initContext();

        configAction();
    }

    private static void configAction() {
        TABLE_VIEW.setOnMouseClicked(event -> {
            if (event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 1) {
                AudioMetaData audioMetaData = TABLE_VIEW.getSelectionModel().getSelectedItem();
                if (audioMetaData != null) {
                    Aside.showMetaData(audioMetaData);
                }
            }
        });
    }

    private static void initContext() {
        List<String> paths = Session.getCurrentTableViewContent();
        List<AudioMetaData> audioMetaData = new ArrayList<>(paths.size());
        if (paths.size() >= 1) {
            paths.forEach(path -> {
                File file = new File(path);
                if (file.isDirectory()) {
                    audioMetaData.addAll(MetaDataReader.readDirectory(file));
                } else {
                    audioMetaData.add(MetaDataReader.readFile(file));
                }
            });
            TABLE_VIEW.getItems().addAll(audioMetaData);
            Head.recordeArtistAndAlbum(audioMetaData);
        }
    }

    public static Node getCenter() {
        return TABLE_VIEW;
    }

    public static TableView<AudioMetaData> getTableView() {
        return TABLE_VIEW;
    }
}
