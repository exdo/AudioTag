package xyz.idaoteng.audiotag;

import xyz.idaoteng.audiotag.bean.AudioFileData;
import xyz.idaoteng.audiotag.component.Aside;
import xyz.idaoteng.audiotag.component.Center;
import xyz.idaoteng.audiotag.component.Head;
import xyz.idaoteng.audiotag.component.Message;
import xyz.idaoteng.audiotag.dialog.Filter;

import java.util.ArrayList;
import java.util.List;

public class UiCoordinator {
    private static Head head;
    private static Center center;
    private static Aside aside;
    private static Message message;

    private static final List<AudioFileData> itemsBeforeFilter = new ArrayList<>();

    public static void setComponent(Head head, Center center, Aside aside, Message message) {
        UiCoordinator.head = head;
        UiCoordinator.center = center;
        UiCoordinator.aside = aside;
        UiCoordinator.message = message;
    }

    public static void showSelectedItem(AudioFileData audioFileData) {
        aside.showData(audioFileData);
    }

    public static void addTableViewItems(List<AudioFileData> dataList) {
        center.setTableViewItems(dataList, true);
    }

    public static void setTableViewItems(List<AudioFileData> dataList) {
        center.setTableViewItems(dataList, false);
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
        message.showMessage(msg);
    }
}
