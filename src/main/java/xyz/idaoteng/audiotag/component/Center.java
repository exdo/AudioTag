package xyz.idaoteng.audiotag.component;

import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import xyz.idaoteng.audiotag.AudioMetaData;
import xyz.idaoteng.audiotag.Session;
import xyz.idaoteng.audiotag.core.MetaDataReader;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class Center {
    private static final AnchorPane CENTER = new AnchorPane();
    private static final TableView<AudioMetaData> TABLE_VIEW = new TableView<>();
    private static final ContextMenu CONTEXT_MENU = new ContextMenu();
    private static final MenuItem RENAME_MENU_ITEM = new MenuItem("重命名");

    private static final HashSet<String> ALTERNATIVE_ARTISTS = new HashSet<>();
    private static final HashSet<String> ALTERNATIVE_ALBUMS = new HashSet<>();

    private static final ArrayList<AudioMetaData> SELECTED_AUDIO_META_DATA = new ArrayList<>();

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

        TableColumn<AudioMetaData, String> dateColumn = new TableColumn<>("出版日期");
        dateColumn.setPrefWidth(100);
        dateColumn.setStyle("-fx-alignment: CENTER-RIGHT");
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));

        TableColumn<AudioMetaData, String> genreColumn = new TableColumn<>("流派");
        genreColumn.setPrefWidth(85);
        genreColumn.setCellValueFactory(new PropertyValueFactory<>("genre"));

        TableColumn<AudioMetaData, String> trackColumn = new TableColumn<>("音轨序号");
        trackColumn.setPrefWidth(55);
        trackColumn.setCellValueFactory(new PropertyValueFactory<>("track"));

        TableColumn<AudioMetaData, String> commentColumn = new TableColumn<>("备注");
        commentColumn.setPrefWidth(200);
        commentColumn.setCellValueFactory(new PropertyValueFactory<>("comment"));

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
        TABLE_VIEW.getColumns().add(albumColumn);
        TABLE_VIEW.getColumns().add(dateColumn);
        TABLE_VIEW.getColumns().add(genreColumn);
        TABLE_VIEW.getColumns().add(trackColumn);
        TABLE_VIEW.getColumns().add(commentColumn);
        TABLE_VIEW.getColumns().add(bitrateColumn);
        TABLE_VIEW.getColumns().add(lengthColumn);

        TABLE_VIEW.setTableMenuButtonVisible(true);
        TABLE_VIEW.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        configContextMenu();

        initContent();

        configAction();

        AnchorPane.setTopAnchor(TABLE_VIEW, 0.0);
        AnchorPane.setBottomAnchor(TABLE_VIEW, 0.0);
        AnchorPane.setLeftAnchor(TABLE_VIEW, 0.0);
        AnchorPane.setRightAnchor(TABLE_VIEW, 0.0);
        CENTER.getChildren().add(TABLE_VIEW);
    }

    private static void configContextMenu() {
        MenuItem renameBaseOnTags = new MenuItem("根据标签重命名");
        MenuItem addTagsBaseOnFilename = new MenuItem("基于文件名添加标签");
        MenuItem deleteFromTable = new MenuItem("从表格中移除");
        MenuItem deleteFile = new MenuItem("删除选中的文件");
        CONTEXT_MENU.getItems().add(RENAME_MENU_ITEM);
        CONTEXT_MENU.getItems().add(renameBaseOnTags);
        CONTEXT_MENU.getItems().add(addTagsBaseOnFilename);
        CONTEXT_MENU.getItems().add(deleteFromTable);
        CONTEXT_MENU.getItems().add(deleteFile);
    }

    private static void configAction() {
        TABLE_VIEW.setOnMouseClicked(event -> {
            if (event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 1) {
                AudioMetaData selectedItem = TABLE_VIEW.getSelectionModel().getSelectedItem();
                if (selectedItem != null) {
                    Aside.showMetaData(selectedItem);
                }
            }
        });

        TABLE_VIEW.setOnMousePressed(event -> {
            if (event.isSecondaryButtonDown()) {
                SELECTED_AUDIO_META_DATA.clear();
                List<AudioMetaData> selectedItems = TABLE_VIEW.getSelectionModel().getSelectedItems();
                if (!selectedItems.isEmpty()) {
                    SELECTED_AUDIO_META_DATA.addAll(selectedItems);
                    RENAME_MENU_ITEM.setDisable(SELECTED_AUDIO_META_DATA.size() > 1);
                    CONTEXT_MENU.show(TABLE_VIEW, event.getScreenX(), event.getScreenY());
                }
            }
        });

        double layoutX = TABLE_VIEW.getLayoutX();
        TABLE_VIEW.setOnDragEntered(event -> {

        });

        TABLE_VIEW.setOnDragExited(event -> {

        });
    }

    private static void initContent() {
        List<String> paths = Session.getCurrentTableViewContentPaths();
        List<AudioMetaData> audioMetaData = new ArrayList<>(paths.size());
        paths.forEach(path -> audioMetaData.add(MetaDataReader.readFile(new File(path))));
        updateTableView(audioMetaData);
    }

    public static void updateTableView(List<AudioMetaData> dataList) {
        if (dataList == null) {
            recordeUpdate(TABLE_VIEW.getItems());
            TABLE_VIEW.refresh();
        } else {
            Aside.showBlank();
            recordeUpdate(dataList);
            TABLE_VIEW.getItems().clear();
            TABLE_VIEW.getItems().addAll(dataList);
            TABLE_VIEW.refresh();
        }
    }

    private static void recordeUpdate(List<AudioMetaData> dataList) {
        ALTERNATIVE_ARTISTS.clear();
        ALTERNATIVE_ALBUMS.clear();

        List<String> paths = new ArrayList<>(dataList.size());

        for (AudioMetaData metaData : dataList) {
            String artist = metaData.getArtist();
            if (artist != null) {
                artist = artist.trim();
                if (!"".equals(artist)) {
                    ALTERNATIVE_ARTISTS.add(artist);
                }
            }

            String album = metaData.getAlbum();
            if (album != null) {
                album = album.trim();
                if (!"".equals(album)) {
                    ALTERNATIVE_ALBUMS.add(album);
                }
            }

            String genre = metaData.getGenre();
            if (genre != null) {
                genre = genre.trim();
                if (!"".equals(genre) && !Session.getAlternativeGenres().contains(genre)) {
                    Session.addGenre(genre);
                }
            }

            paths.add(metaData.getAbsolutePath());
        }

        Session.setCurrentTableViewContentPaths(paths);
    }

    public static List<String> getAlternativeArtists() {
        return new ArrayList<>(ALTERNATIVE_ARTISTS);
    }

    public static List<String> getAlternativeAlbums() {
        return new ArrayList<>(ALTERNATIVE_ALBUMS);
    }

    public static Node getCenter() {
        return CENTER;
    }

    public static void selectItem(AudioMetaData audioMetaData) {
        int index = TABLE_VIEW.getItems().indexOf(audioMetaData);
        TABLE_VIEW.getSelectionModel().select(index);
    }
}
