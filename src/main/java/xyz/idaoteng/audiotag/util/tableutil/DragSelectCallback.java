package xyz.idaoteng.audiotag.util.tableutil;


import javafx.scene.input.MouseEvent;

public interface DragSelectCallback {
    /**
     * 在拖拽过程中当某行被选中时，用户的自定义处理逻辑
     *
     * @param rowIndex 在拖拽过程中被选中的行号。rowIndex >= 0
     */
    default void onRowSelectedDuringDrag(int rowIndex) {
    }

    /**
     * 实现拖拽功能时会占用 TableView 的 setOnMousePressed、setOnMouseClicked、
     * setOnMouseReleased 以及 setOnMouseDragged 方法，但用户可能对此还有其他需求
     * 鼠标按下后用户的自定义处理逻辑
     *
     * @param event 鼠标事件
     */
    default void onMousePressed(MouseEvent event) {
    }

    /**
     * 鼠标点击后用户的自定义处理逻辑
     *
     * @param event    鼠标事件
     * @param rowIndex 点击鼠标时鼠标指针所在位置对应的行的索引，或 null 如果当前位置没有对应的行
     */
    default void onMouseClicked(MouseEvent event, Integer rowIndex) {
    }

    /**
     * 鼠标释放后用户的自定义处理逻辑
     *
     * @param event 鼠标事件
     */
    default void onMouseReleased(MouseEvent event) {
    }

    /**
     * 鼠标拖动时用户的自定义处理逻辑
     *
     * @param event 鼠标事件
     */
    default void onMouseDragged(MouseEvent event) {
    }
}
