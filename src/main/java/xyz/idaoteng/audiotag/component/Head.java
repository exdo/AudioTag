package xyz.idaoteng.audiotag.component;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import xyz.idaoteng.audiotag.bean.AudioMetaData;
import xyz.idaoteng.audiotag.Session;
import xyz.idaoteng.audiotag.StartUp;
import xyz.idaoteng.audiotag.core.MetaDataReader;
import xyz.idaoteng.audiotag.core.SupportedFileTypes;
import xyz.idaoteng.audiotag.dialog.Filter;
import xyz.idaoteng.audiotag.exception.CantReadException;

import java.io.File;
import java.util.*;

public class Head {
    private static final HBox HEAD = new HBox(5);
    private static final Button SELECT_FILE_BUTTON = new Button("选择文件");
    private static final MenuItem SELECT_FOLDER_WITH_CHILDREN = new MenuItem("选择文件夹<含子文件夹>");
    public static final MenuItem SELECT_FOLDER_WITHOUT_CHILDREN = new MenuItem("选择文件夹<不含子文件夹>");
    private static final Button REFRESH_BUTTON = new Button("刷新");

    private static final ExtensionFilter EXTENSION_FILTER = new ExtensionFilter("audio file", SupportedFileTypes.getTypes());

    static {
        HEAD.setMaxHeight(30);
        HEAD.setMinHeight(30);
        HEAD.setPadding(new Insets(5, 0, 0, 0));
        HEAD.setStyle("-fx-border-style: solid; -fx-border-color: #cccccc; -fx-border-width: 0 0 1 0");

        try {
            Class.forName("xyz.idaoteng.audiotag.component.Center");
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        configSelectFileButtonActionHandle();

        MenuButton menuButton  = new MenuButton("选择文件夹");
        SeparatorMenuItem separator = new SeparatorMenuItem();
        menuButton.getItems().addAll(SELECT_FOLDER_WITH_CHILDREN, separator, SELECT_FOLDER_WITHOUT_CHILDREN);
        configSelectDirectoryButtonActionHandle();

        configRefreshButtonActionHandle();

        Button selectAll = new Button("全选");
        selectAll.setOnAction(event -> Center.selectAll());

        Button filter = new Button("过滤");
        Filter.takeOverFilterButton(filter);

        Button renameBaseOnTag = new Button("根据标签重命名");
        renameBaseOnTag.setOnAction(event -> Center.renameBaseOnTags());

        Button addTag = new Button("基于文件名添加标签");
        addTag.setOnAction(event -> Center.addTagBaseOnFilename());

        Button delete = new Button("删除文件");
        delete.setOnAction(event -> Center.deleteSelectedItems());

        Button rename = new Button("重命名");
        Center.takeOverRenameButton(rename);

        MenuButton other = new MenuButton("其他");
        MenuItem remove = new MenuItem("从表格中移除");
        remove.setOnAction(event -> Center.removeSelectedItems());

        MenuItem addOrder = new MenuItem("从上至下依次添加序号");
        addOrder.setOnAction(event -> Center.addOrder());

        Menu deleteSpecialTag = Center.generateDeleteSpecificTagMenu();
        Center.configDeleteSpecificTagMenu(deleteSpecialTag);

        MenuItem packageToAlbum = new MenuItem("设置成同一专辑");
        packageToAlbum.setOnAction(event -> Center.packageToAlbum());

        other.getItems().addAll(addOrder, deleteSpecialTag, packageToAlbum,
                Center.generateSameAlbumOptionMenu());

        RadioButton enableDragRow = new RadioButton("允许拖拽行");
        enableDragRow.setPadding(new Insets(4, 0, 0, 0));
        Center.takeOverEnableDragRow(enableDragRow);

        HEAD.getChildren().addAll(SELECT_FILE_BUTTON, menuButton, REFRESH_BUTTON, selectAll, filter,
                renameBaseOnTag, addTag, delete, rename, other, enableDragRow);
    }

    private static void configRefreshButtonActionHandle() {
        REFRESH_BUTTON.setOnAction(event -> {
            List<String> current = Session.getCurrentTableViewContentPaths();
            List<AudioMetaData> refreshed = new ArrayList<>(current.size());
            Iterator<String> iterator = current.iterator();
            while (iterator.hasNext()) {
                String path = iterator.next();
                File file = new File(path);
                if (file.exists()) {
                    try {
                        refreshed.add(MetaDataReader.readFile(file));
                    } catch (CantReadException ignored) {}
                } else {
                    iterator.remove();
                }
            }
            Aside.showMetaData(new AudioMetaData());
            Center.updateTableView(refreshed);
         });
    }

    private static void configSelectDirectoryButtonActionHandle() {
        SELECT_FOLDER_WITH_CHILDREN.setOnAction(event -> {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setInitialDirectory(new File(Session.getPathToTheLastSelectedFolder()));
            directoryChooser.setTitle("选择文件夹（含子文件夹）");
            File dir = directoryChooser.showDialog(StartUp.getPrimaryStage());
            if (dir != null) {
                List<AudioMetaData> audioMetaData = MetaDataReader.readDirectory(dir, true);
                Center.updateTableView(audioMetaData);
                Session.setPathToTheLastSelectedFolder(dir.getAbsolutePath());
            }
        });

        SELECT_FOLDER_WITHOUT_CHILDREN.setOnAction(event -> {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setInitialDirectory(new File(Session.getPathToTheLastSelectedFolder()));
            directoryChooser.setTitle("选择文件夹（不含子文件夹）");
            File dir = directoryChooser.showDialog(StartUp.getPrimaryStage());
            if (dir != null) {
                List<AudioMetaData> audioMetaData = MetaDataReader.readDirectory(dir, false);
                Center.updateTableView(audioMetaData);
                Session.setPathToTheLastSelectedFolder(dir.getAbsolutePath());
            }
        });
    }

    private static void configSelectFileButtonActionHandle() {
        SELECT_FILE_BUTTON.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setInitialDirectory(new File(Session.getFolderPathOfTheLastSelectedFile()));
            fileChooser.getExtensionFilters().addAll(EXTENSION_FILTER);
            fileChooser.setTitle("选择音频文件（多选）");
            List<File> files = fileChooser.showOpenMultipleDialog(StartUp.getPrimaryStage());
            if (files != null && !files.isEmpty()) {
                List<AudioMetaData> dataList = new ArrayList<>(files.size());
                files.forEach(file -> {
                    try {
                        dataList.add(MetaDataReader.readFile(file));
                    } catch (CantReadException ignored) {}
                });
                Center.updateTableView(dataList);
                String folderPath = files.get(0).getParentFile().getAbsolutePath();
                Session.setFolderPathOfTheLastSelectedFile(folderPath);
            }
        });
    }

    public static Node getHead() {
        return HEAD;
    }
}
