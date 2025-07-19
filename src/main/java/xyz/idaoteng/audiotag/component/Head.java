package xyz.idaoteng.audiotag.component;

import atlantafx.base.controls.ToggleSwitch;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ToolBar;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import xyz.idaoteng.audiotag.App;
import xyz.idaoteng.audiotag.Session;
import xyz.idaoteng.audiotag.UiCoordinator;
import xyz.idaoteng.audiotag.bean.AudioFileData;
import xyz.idaoteng.audiotag.constant.ExtensionFilters;
import xyz.idaoteng.audiotag.jaudiotagger.AudioFileReader;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Head {
    private final ToolBar toolBar = new ToolBar();

    public enum FilterText {
        ON("过滤"),
        OFF("关闭过滤");

        private final String text;

        FilterText(String filterText) {
            text = filterText;
        }

        String getText() {
            return text;
        }
    }

    private final Button filterButton = new Button(FilterText.ON.getText());

    public Head() {
        buildToolbar();
    }

    private void buildToolbar() {
        MenuButton fileMenuButton = fileMenuButton();

        MenuButton editMenuButton = editMenuButton();

        filterButton.setOnAction(event -> {
            if (filterButton.getText().equals(FilterText.ON.getText())) {
                UiCoordinator.openFilterDialog();
                event.consume();
                return;
            }

            if (filterButton.getText().equals(FilterText.OFF.getText())) {
                UiCoordinator.recoverTableViewItems();
            }
        });

        Button refreshButton = new Button("刷新");
        refreshButton.setOnAction(event -> UiCoordinator.refreshTableView());

        ToggleSwitch toggleSwitch = toggleSwitch();

        toolBar.getItems().addAll(
                fileMenuButton,
                editMenuButton,
                filterButton,
                refreshButton,
                toggleSwitch
        );
    }

    private MenuButton fileMenuButton() {
        MenuButton fileMenuButton = new MenuButton("文件");

        MenuItem openFile = new MenuItem("打开文件");
        openFile.setOnAction(event -> UiCoordinator.setTableViewItems(openFile()));

        MenuItem openDirectories = new MenuItem("打开文件夹(含子文件夹)");
        openDirectories.setOnAction(event -> UiCoordinator.setTableViewItems(openFolder(true)));

        MenuItem openDirectory = new MenuItem("打开文件夹(不含子文件夹)");
        openDirectory.setOnAction(event -> UiCoordinator.setTableViewItems(openFolder(false)));

        MenuItem addFile = new MenuItem("添加文件");
        addFile.setOnAction(event -> UiCoordinator.addTableViewItems(openFile()));

        MenuItem addDirectories = new MenuItem("添加文件夹(含子文件夹)");
        addDirectories.setOnAction(event -> UiCoordinator.addTableViewItems(openFolder(true)));

        MenuItem addDirectory = new MenuItem("添加文件夹(不含子文件夹)");
        addDirectory.setOnAction(event -> UiCoordinator.addTableViewItems(openFolder(false)));

        fileMenuButton.getItems().addAll(
                openFile,
                openDirectories,
                openDirectory,
                addFile,
                addDirectories,
                addDirectory);
        return fileMenuButton;
    }

    private MenuButton editMenuButton() {
        MenuButton edit = new MenuButton("编辑");

        MenuItem selectAll = new MenuItem("全选");
        selectAll.setOnAction(event -> UiCoordinator.selectAllItems());

        MenuItem clearAll = new MenuItem("清空");
        clearAll.setOnAction(event -> UiCoordinator.clearAllItems());

        edit.getItems().addAll(selectAll, clearAll);
        return edit;
    }

    private ToggleSwitch toggleSwitch() {
        ToggleSwitch toggleSwitch = new ToggleSwitch("允许拖拽行");
        toggleSwitch.setSelected(false);
        toggleSwitch.selectedProperty().addListener((ob, o, n) -> UiCoordinator.enableDragRow(n));
        return toggleSwitch;
    }

    private List<AudioFileData> openFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("添加文件（多选）");
        fileChooser.getExtensionFilters().addAll(ExtensionFilters.FILE_FILTER);
        // 设置初始目录为上次选择的文件夹
        fileChooser.setInitialDirectory(Session.getLastSelectedFolder());

        // 显示对话框并获取选择的文件列表
        List<File> files = fileChooser.showOpenMultipleDialog(App.getPrimaryStage());

        if (files != null && !files.isEmpty()) {
            // 读取每个文件的数据
            List<AudioFileData> dataList = new ArrayList<>(files.size());
            for (File file : files) {
                AudioFileData data = AudioFileReader.readFile(file);
                if (data != null) {
                    dataList.add(data);
                }
            }
            Session.setLastSelectedFolder(files.get(files.size() - 1).getParentFile());
            return dataList;
        }
        return Collections.emptyList();
    }

    private List<AudioFileData> openFolder(boolean recursive) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setInitialDirectory(Session.getLastSelectedFolder());
        if (recursive) {
            directoryChooser.setTitle("添加文件夹（含子文件夹）");
        } else {
            directoryChooser.setTitle("添加文件夹（不含子文件夹）");
        }
        File dir = directoryChooser.showDialog(App.getPrimaryStage());

        if (dir != null) {
            Session.setLastSelectedFolder(dir);
            return AudioFileReader.readDirectory(dir, recursive);
        }
        return Collections.emptyList();
    }

    public Node getNode() {
        return toolBar;
    }

    public void switchFilterButtonText(FilterText text) {
        filterButton.setText(text.getText());
    }
}
