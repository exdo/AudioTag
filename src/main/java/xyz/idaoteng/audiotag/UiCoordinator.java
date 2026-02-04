package xyz.idaoteng.audiotag;

import javafx.scene.Node;
import xyz.idaoteng.audiotag.bean.AudioFileData;
import xyz.idaoteng.audiotag.component.*;
import xyz.idaoteng.audiotag.dialog.Filter;

import java.util.ArrayList;
import java.util.List;

public class UiCoordinator {
    private static Head head;
    private static Center center;
    private static Aside aside;
    private static Message message;
    private static Modal modal;

    public static void setComponent(Head head, Center center, Aside aside, Message message, Modal modal) {
        UiCoordinator.head = head;
        UiCoordinator.center = center;
        UiCoordinator.aside = aside;
        UiCoordinator.message = message;
        UiCoordinator.modal = modal;
    }

    public static void showSelectedItem(AudioFileData audioFileData) {
        aside.showData(audioFileData);
    }

    public static void addTableViewItems(List<AudioFileData> dataList) {
        center.setTableViewItems(dataList, true);
    }

    public static void setTableViewItems(List<AudioFileData> dataList) {
        center.setTableViewItems(dataList, false);
        aside.showBlank();
    }

    public static void enableDragRow(boolean isEnableDragRow) {
        center.enableDragRowOrDragSelection(isEnableDragRow);
    }

    public static void selectAllItems() {
        center.selectAllItems();
    }

    public static void clearAllItems() {
        center.clearAllItems();
    }

    public static void refreshTableView() {
        center.refreshTableView();
    }

    private static final List<AudioFileData> itemsBeforeFilter = new ArrayList<>();
    public static void openFilterDialog() {
        List<AudioFileData> allItems = center.getAllItems();
        itemsBeforeFilter.clear();
        itemsBeforeFilter.addAll(allItems);
        boolean filterViewOn = Filter.show(allItems);
        if (filterViewOn) {
            head.switchFilterButtonText(Head.FilterText.OFF);
        } else {
            head.switchFilterButtonText(Head.FilterText.ON);
        }
    }

    public static void recoverTableViewItems() {
        head.switchFilterButtonText(Head.FilterText.ON);
        if (!itemsBeforeFilter.isEmpty()) {
            setTableViewItems(itemsBeforeFilter);
        }
    }

    public static void refreshAsideData() {
        aside.reload();
    }

    public static void showNotification(String msg) {
        if (message == null) return;
        UiCoordinator.refreshTableView();
        UiCoordinator.refreshAsideData();
        message.showMessage(msg);
    }

    public static void showModal(Node node) {
        modal.show(node);
    }
}
