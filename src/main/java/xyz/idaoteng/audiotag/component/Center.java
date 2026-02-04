package xyz.idaoteng.audiotag.component;

import atlantafx.base.theme.Styles;
import javafx.collections.ListChangeListener;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import xyz.idaoteng.audiotag.Session;
import xyz.idaoteng.audiotag.UiCoordinator;
import xyz.idaoteng.audiotag.bean.AudioFileData;
import xyz.idaoteng.audiotag.constant.Column;
import xyz.idaoteng.audiotag.jaudiotagger.AudioFileReader;
import xyz.idaoteng.audiotag.util.tableutil.DragRowSwitch;
import xyz.idaoteng.audiotag.util.tableutil.DragSelectCallback;
import xyz.idaoteng.audiotag.util.tableutil.DragSelectSwitch;
import xyz.idaoteng.audiotag.util.tableutil.TableViewUtil;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Center {
    private final TableView<AudioFileData> tableView = new TableView<>();
    private static final double ROW_FIXED_HEIGHT = 28.0;
    private DragRowSwitch dragRowSwitch;
    private DragSelectSwitch dragSelectSwitch;

    public Center() {
        tableView.getStyleClass().addAll(Styles.BORDERED, Styles.DENSE, Styles.STRIPED);
        tableView.setTableMenuButtonVisible(true);

        addColumn();
        setupRow();
        enhancementSelection();
        addContextMenu();
        initItems();
    }

    private void addColumn() {
        // 从 Session 中获取列的顺序及可视状态而后依序添加列
        HashMap<Integer, String> order = Session.getColumnsOrder();
        HashMap<String, Boolean> visibleStatus = Session.getColumnVisibleStatus();
        for (int i = 0; i < order.size(); i++) {
            TableColumn<AudioFileData, String> column = Column.getColumnByName(order.get(i));
            column.setVisible(visibleStatus.get(column.getText()));
            // 监听并更新 TableColumn 的可见性变化
            column.visibleProperty().addListener((ob, o, n) -> Session.updateVisibleStatus(column.getText(), n));
            tableView.getColumns().add(column);
        }

        // 监听并更新 TableColumn 的顺序变化
        tableView.getColumns().addListener((ListChangeListener<? super TableColumn<AudioFileData, ?>>) c -> {
            while (c.next()) {
                if (c.wasPermutated()) {
                    HashMap<Integer, String> map = new HashMap<>(12);
                    for (int i = 0; i < tableView.getColumns().size(); i++) {
                        map.put(i, tableView.getColumns().get(i).getText());
                    }
                    Session.updateColumnOrder(map);
                }
            }
        });
    }

    private void setupRow() {
        tableView.setRowFactory(tableView -> {
            TableRow<AudioFileData> row = new TableRow<>();
            // 使行可拖拽
            TableViewUtil.makeRowDraggable(tableView, row);
            // 更改被选中行的背景色
            row.selectedProperty().addListener((ob, o, n) -> {
                if (n) {
                    row.setStyle("-fx-background-color: #e4f5fc;");
                } else {
                    row.setStyle("");
                }
            });
            return row;
        });
    }

    private void enhancementSelection() {
        tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        dragSelectSwitch = TableViewUtil.enableDragSelection(tableView, buildCallback(), ROW_FIXED_HEIGHT);
    }

    private DragSelectCallback buildCallback() {
        return new DragSelectCallback() {
            @Override
            public void onRowSelectedDuringDrag(int rowIndex) {
                // 获取当前行对应的数据
                AudioFileData audioFileData = tableView.getItems().get(rowIndex);
                // 展示被选中行的可编辑部分
                UiCoordinator.showSelectedItem(audioFileData);
            }

            @Override
            public void onMouseClicked(MouseEvent event, Integer rowIndex) {
                if (event.getButton().equals(MouseButton.PRIMARY) && rowIndex != null && rowIndex >= 0) {
                    AudioFileData audioFileData = tableView.getItems().get(rowIndex);
                    UiCoordinator.showSelectedItem(audioFileData);

                    // 左键双击：使用系统默认方式打开文件
                    if (event.getClickCount() == 2) {
                        try {
                            Desktop.getDesktop().open(new File(audioFileData.getAbsolutePath()));
                        } catch (IOException e) {
                            UiCoordinator.showNotification("打开文件时遇到错误");
                        }
                    }
                }
            }
        };
    }

    private void addContextMenu() {
        new TableViewContextMenu(tableView);
    }

    private void initItems() {
        List<String> paths = Session.getOpenedPaths();
        List<AudioFileData> dataList = new ArrayList<>(paths.size());
        for (String path : paths) {
            AudioFileData data = AudioFileReader.readFile(new File(path));
            if (data != null) {
                dataList.add(data);
            }
        }
        setTableViewItems(dataList, false);
    }

    public void setTableViewItems(List<AudioFileData> dataList, boolean isAdditional) {
        if (!isAdditional) {
            tableView.getItems().clear();
        }
        tableView.getItems().addAll(dataList);
        Session.recordItems(dataList, isAdditional);
    }

    public void enableDragRowOrDragSelection(boolean isEnableDragRow) {
        if (dragRowSwitch == null) {
            dragRowSwitch = TableViewUtil.getDragRowSwitch(tableView);
            if (dragRowSwitch == null) return;
        }

        if (isEnableDragRow) {
            dragRowSwitch.turnOn();
            dragSelectSwitch.turnOff();
        } else {
            dragRowSwitch.turnOff();
            dragSelectSwitch.turnOn();
        }
    }

    public TableView<AudioFileData> getNode() {
        return tableView;
    }

    public void selectAllItems() {
        tableView.getSelectionModel().selectAll();
    }

    public void clearAllItems() {
        tableView.getItems().clear();
    }

    public void refreshTableView() {
        tableView.refresh();
        Session.recordItems(tableView.getItems(), false);
    }

    public List<AudioFileData> getAllItems() {
        return tableView.getItems();
    }
}
