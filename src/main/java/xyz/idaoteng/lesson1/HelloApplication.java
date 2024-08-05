package xyz.idaoteng.lesson1;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) {
        BorderPane root = new BorderPane();

        HBox head = new HBox(6);
        head.setMaxHeight(50);
        head.setMinHeight(50);
        Button selectFileButton = new Button("选择文件");

        Button selectDirectoryButton = new Button("选择文件夹");
        Button artistListButton = new Button("备选艺术家表");
        Button albumListButton = new Button("备选专辑表");
        head.getChildren().addAll(selectFileButton, selectDirectoryButton, artistListButton, albumListButton);
        root.setTop(head);

        TableView<MusicMetaInfo> tableView = new TableView<>();
        tableView.setMinWidth(570);
        TableColumn<MusicMetaInfo, String> titleColumn = new TableColumn<>("标题");
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        TableColumn<MusicMetaInfo, String> artistColumn = new TableColumn<>("艺术家");
        artistColumn.setCellValueFactory(new PropertyValueFactory<>("artist"));
        TableColumn<MusicMetaInfo, String> albumColumn = new TableColumn<>("专辑");
        albumColumn.setCellValueFactory(new PropertyValueFactory<>("album"));
        TableColumn<MusicMetaInfo, String> genreColumn = new TableColumn<>("流派");
        genreColumn.setCellValueFactory(new PropertyValueFactory<>("genre"));
        TableColumn<MusicMetaInfo, String> trackColumn = new TableColumn<>("音轨");
        trackColumn.setCellValueFactory(new PropertyValueFactory<>("track"));
        tableView.getColumns().add(titleColumn);
        tableView.getColumns().add(artistColumn);
        tableView.getColumns().add(albumColumn);
        tableView.getColumns().add(genreColumn);
        tableView.getColumns().add(trackColumn);
        root.setCenter(tableView);

        MusicMetaInfo musicMetaInfo1 = new MusicMetaInfo();
        musicMetaInfo1.setTitle("测试1");
        musicMetaInfo1.setArtist("测试2");
        musicMetaInfo1.setAlbum("测试3");
        musicMetaInfo1.setGenre("测试4");
        musicMetaInfo1.setTrack("测试5");
        MusicMetaInfo musicMetaInfo2 = new MusicMetaInfo();
        musicMetaInfo2.setTitle("测试6");
        musicMetaInfo2.setArtist("测试7");
        musicMetaInfo2.setAlbum("测试8");
        musicMetaInfo2.setGenre("测试9");
        musicMetaInfo2.setTrack("测试10");
        tableView.getItems().add(musicMetaInfo1);
        tableView.getItems().add(musicMetaInfo2);

        VBox infoDetailPanel = new VBox(6);
        infoDetailPanel.setMinWidth(250);
        Text text2 = new Text("测试右边");
        infoDetailPanel.getChildren().add(text2);
        root.setRight(infoDetailPanel);

        Scene scene = new Scene(root, 900, 700);
        stage.setScene(scene);
        stage.setMinHeight(700);
        stage.setMinWidth(900);
        stage.show();

        setSelectFileButtonController(selectFileButton, stage);
        setSelectDirectoryButtonController(selectDirectoryButton, stage);
    }

    private void setSelectFileButtonController(Button selectFileButton, Stage stage) {
        selectFileButton.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("选择文件");
            File file = fileChooser.showOpenDialog(stage);
            if (file == null) {
                System.out.println("未选择文件");
                return;
            }
            System.out.println(file.getAbsolutePath());
        });
    }

    private void setSelectDirectoryButtonController(Button selectDirectoryButton, Stage stage) {
        selectDirectoryButton.setOnAction(event -> {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setTitle("选择文件夹");
            File file = directoryChooser.showDialog(stage);
            System.out.println(file.getAbsolutePath());
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}