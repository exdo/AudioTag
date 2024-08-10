package xyz.idaoteng.lesson1.component;

import javafx.scene.Node;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import xyz.idaoteng.lesson1.ApplicationContext;
import xyz.idaoteng.lesson1.MusicMetaInfo;

public class Center {
    private static final TableView<MusicMetaInfo> musicMetaInfoTableView = new TableView<>();

    static {
        init();
        ApplicationContext.register("musicMetaInfoTableView", musicMetaInfoTableView);
    }

    private static void init() {
        musicMetaInfoTableView.setStyle("-fx-border-style: solid; -fx-border-color: #cccccc; -fx-border-width: 0 1 0 0");

        TableColumn<MusicMetaInfo, String> filenameColumn = new TableColumn<>("文件名");
        filenameColumn.setPrefWidth(235);
        filenameColumn.setCellValueFactory(new PropertyValueFactory<>("filename"));
        TableColumn<MusicMetaInfo, String> titleColumn = new TableColumn<>("标题");
        titleColumn.setPrefWidth(175);
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        TableColumn<MusicMetaInfo, String> artistColumn = new TableColumn<>("艺术家");
        artistColumn.setCellValueFactory(new PropertyValueFactory<>("artist"));
        artistColumn.setPrefWidth(150);
        TableColumn<MusicMetaInfo, String> albumColumn = new TableColumn<>("专辑");
        albumColumn.setPrefWidth(175);
        albumColumn.setCellValueFactory(new PropertyValueFactory<>("album"));
        TableColumn<MusicMetaInfo, String> genreColumn = new TableColumn<>("流派");
        genreColumn.setPrefWidth(85);
        genreColumn.setCellValueFactory(new PropertyValueFactory<>("genre"));
        TableColumn<MusicMetaInfo, String> trackColumn = new TableColumn<>("音轨");
        trackColumn.setPrefWidth(55);
        trackColumn.setCellValueFactory(new PropertyValueFactory<>("track"));
        TableColumn<MusicMetaInfo, String> bitrateColumn = new TableColumn<>("比特率");
        bitrateColumn.setPrefWidth(75);
        bitrateColumn.setCellValueFactory(new PropertyValueFactory<>("bitrate"));
        TableColumn<MusicMetaInfo, String> lengthColumn = new TableColumn<>("时长");
        lengthColumn.setPrefWidth(65);
        lengthColumn.setCellValueFactory(new PropertyValueFactory<>("length"));

        musicMetaInfoTableView.getColumns().add(filenameColumn);
        musicMetaInfoTableView.getColumns().add(titleColumn);
        musicMetaInfoTableView.getColumns().add(artistColumn);
        musicMetaInfoTableView.getColumns().add(bitrateColumn);
        musicMetaInfoTableView.getColumns().add(lengthColumn);
        musicMetaInfoTableView.getColumns().add(albumColumn);
        musicMetaInfoTableView.getColumns().add(genreColumn);
        musicMetaInfoTableView.getColumns().add(trackColumn);
    }

    public static Node getCenter() {
        return musicMetaInfoTableView;
    }
}
