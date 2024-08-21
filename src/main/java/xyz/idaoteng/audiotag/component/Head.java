package xyz.idaoteng.audiotag.component;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import xyz.idaoteng.audiotag.AudioMetaData;
import xyz.idaoteng.audiotag.Session;
import xyz.idaoteng.audiotag.StartUp;
import xyz.idaoteng.audiotag.core.MetaDataReader;
import xyz.idaoteng.audiotag.core.SupportedFileTypes;

import java.io.File;
import java.util.*;

public class Head {
    private static final HBox HEAD = new HBox(6);
    private static final Button SELECT_FILE_BUTTON = new Button("选择文件");
    private static final Button SELECT_DIRECTORY_BUTTON = new Button("选择文件夹");
    private static final Button REFRESH_BUTTON = new Button("刷新");
    /*
        TODO
        private static final Button artistListButton = new Button("备选艺术家表");
        private static final Button albumListButton = new Button("备选专辑表");
    */
    private static final HashSet<String> ALTERNATIVE_ARTISTS = new HashSet<>();
    private static final HashSet<String> ALTERNATIVE_ALBUMS = new HashSet<>();

    private static final ExtensionFilter EXTENSION_FILTER = new ExtensionFilter("audio file", SupportedFileTypes.getTypes());

    static {
        HEAD.setMaxHeight(30);
        HEAD.setMinHeight(30);
        HEAD.setPadding(new Insets(5, 0, 0, 0));
        HEAD.setStyle("-fx-border-style: solid; -fx-border-color: #cccccc; -fx-border-width: 0 0 1 0");

        try {
            Class.forName("xyz.idaoteng.audiotag.component.Center");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        configSelectFileButtonAction();
        configSelectDirectoryButtonAction();
        configRefreshButtonAction();

        HEAD.getChildren().addAll(SELECT_FILE_BUTTON, SELECT_DIRECTORY_BUTTON, REFRESH_BUTTON);
    }

    private static void configRefreshButtonAction() {
        REFRESH_BUTTON.setOnAction(event -> {
            String needRefreshDirectoryPath = Session.getNeedRefreshDirectoryPath();
            if (needRefreshDirectoryPath != null) {
                updateTableView(MetaDataReader.readDirectory(new File(needRefreshDirectoryPath)));
            } else {
                List<String> current = Session.getCurrentTableViewContent();
                List<AudioMetaData> dataList = new ArrayList<>(current.size());
                Iterator<String> iterator = current.iterator();
                while (iterator.hasNext()) {
                    String path = iterator.next();
                    File file = new File(path);
                    if (file.exists()) {
                        dataList.add(MetaDataReader.readFile(file));
                    } else {
                        iterator.remove();
                    }
                }
                updateTableView(dataList);
            }
        });
    }

    private static void configSelectDirectoryButtonAction() {
        SELECT_DIRECTORY_BUTTON.setOnAction(event -> {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setInitialDirectory(new File(Session.getLastSelectedDirectoryPath()));
            directoryChooser.setTitle("选择文件夹");
            File dir = directoryChooser.showDialog(StartUp.getPrimaryStage());
            if (dir != null) {
                updateTableView(MetaDataReader.readDirectory(dir));
                String lastSelectedDirectoryPath = dir.getAbsolutePath();
                Session.setLastSelectedDirectoryPath(lastSelectedDirectoryPath);
                Session.setNeedRefreshDirectoryPath(lastSelectedDirectoryPath);
                Session.setCurrentTableViewContent(Collections.singletonList(lastSelectedDirectoryPath));
            }
        });
    }

    private static void configSelectFileButtonAction() {
        SELECT_FILE_BUTTON.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setInitialDirectory(new File(Session.getLastSelectedFileParentPath()));
            fileChooser.getExtensionFilters().addAll(EXTENSION_FILTER);
            fileChooser.setTitle("选择应音频文件（多选）");
            List<File> files = fileChooser.showOpenMultipleDialog(StartUp.getPrimaryStage());
            if (files != null && !files.isEmpty()) {
                List<AudioMetaData> dataList = new ArrayList<>(files.size());
                List<String> selectedFilesPath = new ArrayList<>(files.size());
                files.forEach(file -> {
                    dataList.add(MetaDataReader.readFile(file));
                    selectedFilesPath.add(file.getAbsolutePath());
                });
                updateTableView(dataList);
                String lastSelectedFileParentPath = files.get(0).toPath().getParent().toString();
                Session.setLastSelectedFileParentPath(lastSelectedFileParentPath);
                Session.setNeedRefreshDirectoryPath(null);
                Session.setCurrentTableViewContent(selectedFilesPath);
            }
        });
    }

    private static void updateTableView(List<AudioMetaData> dataList) {
        recordeArtistAndAlbum(dataList);
        TableView<AudioMetaData> tableView = Center.getTableView();
        tableView.getItems().clear();
        tableView.getItems().addAll(dataList);
        tableView.refresh();
    }

    public static void recordeArtistAndAlbum(List<AudioMetaData> dataList) {
        ALTERNATIVE_ARTISTS.clear();
        ALTERNATIVE_ALBUMS.clear();
        for (AudioMetaData metaData : dataList) {
            String artist = metaData.getArtist();
            if (artist != null && !"".equals(artist)) {
                ALTERNATIVE_ARTISTS.add(artist);
            }

            String album = metaData.getAlbum();
            if (album != null && !"".equals(album)) {
                ALTERNATIVE_ALBUMS.add(album);
            }
        }
    }

    public static List<String> getAlternativeArtists() {
        return new ArrayList<>(ALTERNATIVE_ARTISTS);
    }

    public static List<String> getAlternativeAlbums() {
        return new ArrayList<>(ALTERNATIVE_ALBUMS);
    }

    public static Node getHead() {
        return HEAD;
    }
}
