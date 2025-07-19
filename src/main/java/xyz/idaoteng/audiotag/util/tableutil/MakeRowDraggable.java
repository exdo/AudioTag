package xyz.idaoteng.audiotag.util.tableutil;

import javafx.collections.ObservableList;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.input.*;

/**
 * 拖拽控制器，允许动态启用/禁用拖拽功能
 */
public class MakeRowDraggable<T> {
    private final TableView<T> tableView;
    private final TableRow<T> row;
    private final DragRowSwitch dragRowSwitch;


    public MakeRowDraggable(TableView<T> tableView, TableRow<T> row, DragRowSwitch dragRowSwitch) {
        this.tableView = tableView;
        this.dragRowSwitch = dragRowSwitch;
        this.row = row;
        setupDragHandlers();
    }

    private void setupDragHandlers() {
        row.setOnDragDetected(this::handleDragDetected);
        row.setOnDragOver(this::handleDragOver);
        row.setOnDragDropped(this::handleDragDropped);
    }

    private void handleDragDetected(MouseEvent event) {
        if (dragRowSwitch.isDisable() || row.isEmpty()) return;

        Integer index = row.getIndex();
        Dragboard db = row.startDragAndDrop(TransferMode.MOVE);
        db.setDragView(row.snapshot(null, null));

        ClipboardContent cc = new ClipboardContent();
        cc.put(DataFormat.PLAIN_TEXT, index);
        db.setContent(cc);

        event.consume();
    }

    private void handleDragOver(DragEvent event) {
        if (dragRowSwitch.isDisable()) return;

        Dragboard db = event.getDragboard();
        if (db.hasContent(DataFormat.PLAIN_TEXT)) {
            if (row.getIndex() != Integer.parseInt(db.getContent(DataFormat.PLAIN_TEXT).toString())) {
                event.acceptTransferModes(TransferMode.MOVE);
                event.consume();
            }
        }
    }

    private void handleDragDropped(DragEvent event) {
        if (dragRowSwitch.isDisable()) return;

        Dragboard db = event.getDragboard();
        if (db.hasContent(DataFormat.PLAIN_TEXT)) {
            int draggedIndex = Integer.parseInt(db.getContent(DataFormat.PLAIN_TEXT).toString());

            if (draggedIndex != row.getIndex()) {
                ObservableList<T> items = tableView.getItems();
                if (items != null) {
                    T draggedItem = items.remove(draggedIndex);
                    int dropIndex = row.isEmpty() ? items.size() : row.getIndex();
                    items.add(dropIndex, draggedItem);
                    tableView.getSelectionModel().select(dropIndex);
                }
            }
            event.setDropCompleted(true);
            event.consume();
        }
    }
}
