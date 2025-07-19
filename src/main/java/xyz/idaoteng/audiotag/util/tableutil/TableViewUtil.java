package xyz.idaoteng.audiotag.util.tableutil;

import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;

import java.util.HashMap;

/**
 * 为 TableView 实例实现框选功能和行可拖拽功能
 */
public class TableViewUtil {
    private static final HashMap<TableView<?>, DragRowSwitch> DRAG_ROW_SWITCH = new HashMap<>();

    /**
     * 使指定 TableView 实例的行可拖拽
     *
     * @param tableView TableView 实例
     * @param row       TableView 实例的 TableRow 实例
     * @param <T>       TableView 中存储的数据类型
     */
    public static <T> void makeRowDraggable(TableView<T> tableView, TableRow<T> row) {
        DragRowSwitch dragRowSwitch = getDragRowSwitch(tableView);
        if (dragRowSwitch == null) {
            dragRowSwitch = new DragRowSwitch();
            DRAG_ROW_SWITCH.put(tableView, dragRowSwitch);
        }
        new MakeRowDraggable<>(tableView, row, dragRowSwitch);
    }

    /**
     * 拖拽行功能开关，默认关闭
     *
     * @param tableView TableView 实例
     * @return TableView 实例对应的拖拽行功能开关或 null 如果没有对该实例调用过 makeRowDraggable 方法
     */
    public static DragRowSwitch getDragRowSwitch(TableView<?> tableView) {
        return DRAG_ROW_SWITCH.get(tableView);
    }

    private static final double DEFAULT_ROW_HEIGHT = 24.0; // 默认行高

    /**
     * 为指定的 TableView 实例实现拖拽选择功能
     * 此方法会配置 TableView 的鼠标事件监听器，并管理拖拽选择过程中的所有状态
     *
     * @param tableView     TableView 实例
     * @param processor     用户的自定义回调
     * @param fixedCellSize 表格的固定行高。若行高不固定则无法通过此方法实现拖拽功能，若值不为正数则取默认值：24.0
     * @param <T>           TableView 中存储的数据类型
     * @return DragSelectSwitch 框选功能开关，默认打开
     */
    public static <T> DragSelectSwitch enableDragSelection(TableView<T> tableView, DragSelectCallback processor, double fixedCellSize) {
        DragSelectSwitch dragSelectSwitch = new DragSelectSwitch();
        fixedCellSize = fixedCellSize > 0 ? fixedCellSize : DEFAULT_ROW_HEIGHT;
        tableView.setFixedCellSize(fixedCellSize);
        new EnableDragSelection<>(tableView, processor, dragSelectSwitch);
        return dragSelectSwitch;
    }
}
