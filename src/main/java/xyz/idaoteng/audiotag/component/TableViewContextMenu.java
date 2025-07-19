package xyz.idaoteng.audiotag.component;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.scene.control.*;
import xyz.idaoteng.audiotag.UiCoordinator;
import xyz.idaoteng.audiotag.bean.AudioFileData;
import xyz.idaoteng.audiotag.constant.EditableTag;
import xyz.idaoteng.audiotag.dialog.*;
import xyz.idaoteng.audiotag.jaudiotagger.AudioFileWriter;
import xyz.idaoteng.audiotag.util.FileBrowserUtil;
import xyz.idaoteng.audiotag.util.Utils;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class TableViewContextMenu {
    private final TableView<AudioFileData> tableView;

    public TableViewContextMenu(TableView<AudioFileData> tableView) {
        this.tableView = tableView;
    }

    private void doPreActionThen(Consumer<List<AudioFileData>> action) {
        List<AudioFileData> selectedItems = tableView.getSelectionModel().getSelectedItems();
        if (selectedItems.isEmpty()) {
            // 没有选中项，弹出对话框询问是否全选
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("确认操作");
            alert.setHeaderText("没有选中任何项。");
            alert.setContentText("是否要对所有项执行此操作？");
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                tableView.getSelectionModel().selectAll();
                // 用户点击了确定，执行对所有项的操作
                action.accept(tableView.getItems());
            }
        } else {
            // 有选中项，直接执行对选中项的操作
            action.accept(selectedItems);
        }
        UiCoordinator.refreshTableView();
        UiCoordinator.refreshAsideData();
    }

    private BooleanBinding noSelectedItem() {
        return Bindings.isNull(tableView.getSelectionModel().selectedItemProperty());
    }

    public void addContextMenu() {
        MenuItem selectAll = new MenuItem("全选");
        selectAll.setOnAction(event -> tableView.getSelectionModel().selectAll());

        // 基于标签重命名菜单项
        MenuItem renameBaseOnTags = new MenuItem("根据标签重命名");
        renameBaseOnTags.setOnAction(event -> doPreActionThen(Rename::show));

        // 基于文件名添加标签菜单项
        MenuItem addTagsBaseOnFilename = new MenuItem("基于文件名添加标签");
        addTagsBaseOnFilename.setOnAction(event -> doPreActionThen(AddTag::show));

        // 从表格中移除菜单项
        MenuItem deleteFromTable = new MenuItem("从表格中移除");
        deleteFromTable.disableProperty().bind(noSelectedItem());
        deleteFromTable.setOnAction(event -> {
            List<AudioFileData> selectedItems = tableView.getSelectionModel().getSelectedItems();
            tableView.getItems().removeAll(selectedItems);
        });

        // 删除文件菜单项
        MenuItem deleteFile = new MenuItem("从磁盘中删除文件");
        deleteFile.setOnAction(event -> doPreActionThen(items -> {
            List<AudioFileData> deleted = Delete.show(items);
            tableView.getItems().removeAll(deleted);
        }));

        // 添加序号菜单项
        MenuItem addOrder = new MenuItem("从上至下依次添加序号");
        addOrder.setOnAction(event -> doPreActionThen(this::addOrder));

        // 生成删除特定标签菜单
        Menu deleteSpecificTagMenu = deleteSpecificTagMenu();

        // 打包为同一专辑菜单项
        MenuItem packageToAlbum = new MenuItem("设置成同一专辑");
        packageToAlbum.setOnAction(event -> doPreActionThen(this::packageToAlbum));

        // 批量设置专辑选项菜单
        Menu albumOptionMenu = albumOptionMenu();

        // 整理菜单
        Menu tidyMenu = tidyMenu();

        // 打开文件所在目录菜单项
        MenuItem openFileInExplorer = new MenuItem("在文件浏览器中打开");
        openFileInExplorer.disableProperty().bind(noSelectedItem());
        openFileInExplorer.setOnAction(event -> openInFileBrowser());

        // 取消菜单项
        MenuItem cancel = new MenuItem("取消");
        cancel.setOnAction(event -> tableView.getSelectionModel().clearSelection());

        ContextMenu contextMenu = new ContextMenu();
        // 添加所有菜单项到上下文菜单
        contextMenu.getItems().addAll(
                selectAll,
                renameBaseOnTags,
                addTagsBaseOnFilename,
                deleteFromTable,
                deleteFile,
                addOrder,
                deleteSpecificTagMenu,
                packageToAlbum,
                albumOptionMenu,
                tidyMenu,
                openFileInExplorer,
                cancel
        );

        tableView.setContextMenu(contextMenu);
    }

    private Menu deleteSpecificTagMenu() {
        Menu deleteSpecificTag = new Menu("删除特定标签");

// 创建各种标签删除菜单项
        MenuItem deleteTitle = new MenuItem("删除标题");
        deleteTitle.setOnAction(e -> doPreActionThen(l -> deleteTag(l, EditableTag.TITLE)));

        MenuItem deleteArtist = new MenuItem("删除艺术家");
        deleteArtist.setOnAction(e -> doPreActionThen(l -> deleteTag(l, EditableTag.ARTIST)));

        MenuItem deleteAlbum = new MenuItem("删除专辑");
        deleteAlbum.setOnAction(e -> doPreActionThen(l -> deleteTag(l, EditableTag.ALBUM)));

        MenuItem deleteDate = new MenuItem("删除出版日期");
        deleteDate.setOnAction(e -> doPreActionThen(l -> deleteTag(l, EditableTag.DATE)));

        MenuItem deleteGenre = new MenuItem("删除流派");
        deleteGenre.setOnAction(e -> doPreActionThen(l -> deleteTag(l, EditableTag.GENRE)));

        MenuItem deleteTrack = new MenuItem("删除音轨序号");
        deleteTrack.setOnAction(e -> doPreActionThen(l -> deleteTag(l, EditableTag.TRACK)));

        MenuItem deleteComment = new MenuItem("删除备注");
        deleteComment.setOnAction(e -> doPreActionThen(l -> deleteTag(l, EditableTag.COMMENT)));

        MenuItem deleteCover = new MenuItem("删除封面");
        deleteCover.setOnAction(e -> doPreActionThen(l -> deleteTag(l, EditableTag.COVER)));

        MenuItem deleteAll = new MenuItem("删除全部标签");
        deleteAll.setOnAction(e -> doPreActionThen(l -> deleteTag(l, EditableTag.getAll())));

        // 添加所有菜单项
        deleteSpecificTag.getItems().addAll(
                deleteTitle,
                deleteArtist,
                deleteAlbum,
                deleteDate,
                deleteGenre,
                deleteTrack,
                deleteComment,
                deleteCover,
                deleteAll
        );
        return deleteSpecificTag;
    }

    private Menu albumOptionMenu() {
        Menu menu = new Menu("批量设置专辑");

        // 添加同一封面菜单项
        MenuItem addCover = new MenuItem("为同一专辑添加同一封面");
        addCover.setOnAction(event -> doPreActionThen(this::addCoverForSameAlbum));

        // 添加同一艺术家菜单项
        MenuItem addArtist = new MenuItem("为同一专辑添加同一艺术家");
        addArtist.setOnAction(event -> doPreActionThen(this::addArtistForSameAlbum));

        // 添加同一流派菜单项
        MenuItem addGenre = new MenuItem("为同一专辑添加同一流派");
        addGenre.setOnAction(event -> doPreActionThen(this::addGenreForSameAlbum));

        // 添加同一日期菜单项
        MenuItem addDate = new MenuItem("为同一专辑添加同一出版日期");
        addDate.setOnAction(event -> doPreActionThen(this::addDateForSameAlbum));

        menu.getItems().addAll(addCover, addArtist, addGenre, addDate);
        return menu;
    }

    private Menu tidyMenu() {
        Menu tidyMenu = new Menu("整理");

        MenuItem tidyByArtist = new MenuItem("将同一歌手的文件放置在同一文件夹");
        tidyByArtist.setOnAction(e -> doPreActionThen(l -> Tidy.show(l, true)));

        MenuItem tidyByAlbum = new MenuItem("将同一专辑的文件放置在同一文件夹");
        tidyByAlbum.setOnAction(e -> doPreActionThen(l -> Tidy.show(l, false)));

        tidyMenu.getItems().addAll(tidyByArtist, tidyByAlbum);
        return tidyMenu;
    }

    private void addOrder(List<AudioFileData> items) {
        // 从1开始依次添加序号
        for (int i = 0; i < items.size(); i++) {
            items.get(i).setTrack(String.valueOf(i + 1));
            AudioFileWriter.updateTag(items.get(i), EditableTag.TRACK);
        }
        tableView.refresh();
        UiCoordinator.showNotification("序号已添加");
    }

    private void packageToAlbum(List<AudioFileData> dataList) {
        String albumName = PackageToAlbum.show();
        if (albumName == null) return;

        // 为所有选中项设置相同专辑名
        for (AudioFileData data : dataList) {
            data.setAlbum(albumName);
            AudioFileWriter.updateTag(data, EditableTag.ALBUM);
        }
        UiCoordinator.showNotification("已设置为同一专辑");
    }

    private void deleteTag(List<AudioFileData> items, EditableTag tag) {
        if (items.isEmpty()) return;

        // 根据标签类型清除相应字段
        for (AudioFileData item : items) {
            deleteDataValeByEditableTag(item, tag);
            AudioFileWriter.deleteTag(item, tag);
        }
        UiCoordinator.showNotification("删除完毕");
    }

    private void deleteTag(List<AudioFileData> items, List<EditableTag> tags) {
        if (items.isEmpty() || tags.isEmpty()) return;

        // 根据标签类型清除相应字段
        for (AudioFileData item : items) {
            for (EditableTag tag : tags) {
                deleteDataValeByEditableTag(item, tag);
            }
            AudioFileWriter.deleteTag(item, tags);
        }
        UiCoordinator.showNotification("删除完毕");
    }

    private void deleteDataValeByEditableTag(AudioFileData data, EditableTag tag) {
        switch (tag) {
            case TITLE -> data.setTitle("");
            case ARTIST -> data.setArtist("");
            case ALBUM -> data.setAlbum("");
            case DATE -> data.setDate("");
            case GENRE -> data.setGenre("");
            case TRACK -> data.setTrack("");
            case COMMENT -> data.setComment("");
            case COVER -> data.setCover(null);
            case LYRIC -> data.setLyric("");
        }
    }

    /**
     * 为同一专辑添加同一封面
     */
    public void addCoverForSameAlbum(List<AudioFileData> items) {
        // albumCovers 专辑 -> 封面 映射表
        HashMap<String, byte[]> albumCovers = new HashMap<>();
        for (AudioFileData item : items) {
            String album = item.getAlbum();
            byte[] cover = item.getCover();
            if (!"".equals(album) && cover != null) {
                cover = Utils.retouchedOrItself(cover);
                albumCovers.put(album, cover);
            }
        }

        for (AudioFileData item : items) {
            String album = item.getAlbum();
            if (!"".equals(album) && albumCovers.get(album) != null) {
                // 根据 专辑 找到 封面
                item.setCover(albumCovers.get(album));
                AudioFileWriter.updateTag(item, EditableTag.COVER);
            }
        }
        UiCoordinator.showNotification("专辑封面更新完毕");
    }

    /**
     * 为同一专辑添加同一艺术家
     */
    public void addArtistForSameAlbum(List<AudioFileData> selectedItems) {
        HashMap<String, String> albumArtist = new HashMap<>();
        for (AudioFileData selectedItem : selectedItems) {
            String album = selectedItem.getAlbum();
            String artist = selectedItem.getArtist();
            if (!"".equals(album) && !"".equals(artist)) {
                albumArtist.put(album, artist);
            }
        }

        for (AudioFileData selectedItem : selectedItems) {
            String album = selectedItem.getAlbum();
            if (!"".equals(album) && albumArtist.get(album) != null) {
                // 根据 专辑 找到 艺术家
                selectedItem.setArtist(albumArtist.get(album));
            }
        }
        UiCoordinator.showNotification("专辑艺术家更新完毕");
    }

    /**
     * 为同一专辑添加同一流派
     */
    private void addGenreForSameAlbum(List<AudioFileData> selectedItems) {
        // albumGenre 专辑 -> 流派 映射表
        HashMap<String, String> albumGenre = new HashMap<>();
        for (AudioFileData item : selectedItems) {
            String album = item.getAlbum();
            String genre = item.getGenre();
            if (!"".equals(album) && !"".equals(genre)) {
                albumGenre.put(album, genre);
            }
        }

        for (AudioFileData item : selectedItems) {
            String album = item.getAlbum();
            if (!"".equals(album) && albumGenre.get(album) != null) {
                // 根据 专辑 找到 流派
                item.setGenre(albumGenre.get(album));
                AudioFileWriter.updateTag(item, EditableTag.GENRE);
            }
        }
        UiCoordinator.showNotification("专辑流派更新完毕");
    }

    /**
     * 为同一专辑添加同一日期
     */
    private void addDateForSameAlbum(List<AudioFileData> selectedItems) {
        // albumDate 专辑 -> 发行日期 映射表
        HashMap<String, String> albumDate = new HashMap<>();
        for (AudioFileData selectedItem : selectedItems) {
            String album = selectedItem.getAlbum();
            String date = selectedItem.getDate();
            if (!"".equals(album) && !"".equals(date)) {
                albumDate.put(album, date);
            }
        }

        for (AudioFileData selectedItem : selectedItems) {
            String album = selectedItem.getAlbum();
            if (!"".equals(album) && albumDate.get(album) != null) {
                // 根据 专辑 找到 发行日期
                selectedItem.setDate(albumDate.get(album));
                AudioFileWriter.updateTag(selectedItem, EditableTag.DATE);
            }
        }
        UiCoordinator.showNotification("专辑发行日期更新完毕");
    }

    private void openInFileBrowser() {
        List<AudioFileData> dataList = tableView.getSelectionModel().getSelectedItems();
        for (AudioFileData data : dataList) {
            File file = new File(data.getAbsolutePath());
            FileBrowserUtil.Message message = FileBrowserUtil.highlightFile(file);
            if (!message.equals(FileBrowserUtil.Message.INFO_OPERATION_DONE)) {
                UiCoordinator.showNotification(message.getMessage());
            }
        }
    }
}
