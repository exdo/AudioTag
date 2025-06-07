package xyz.idaoteng.audiotag.component;

import atlantafx.base.theme.Styles;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.skin.TableHeaderRow;
import javafx.scene.control.skin.TableViewSkin;
import javafx.scene.input.*;
import xyz.idaoteng.audiotag.Session;
import xyz.idaoteng.audiotag.Utils;
import xyz.idaoteng.audiotag.bean.AudioMetaData;
import xyz.idaoteng.audiotag.constant.EditableTag;
import xyz.idaoteng.audiotag.core.MetaDataReader;
import xyz.idaoteng.audiotag.core.MetaDataWriter;
import xyz.idaoteng.audiotag.dialog.*;
import xyz.idaoteng.audiotag.exception.CantReadException;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 中央区域表格视图组件，用于展示和操作音频文件元数据
 */
public class Center {
    // 主表格视图
    private static final TableView<AudioMetaData> TABLE_VIEW = new TableView<>();
    // 右键上下文菜单
    private static final ContextMenu CONTEXT_MENU = new ContextMenu();
    // 行位置常量
    private static final int ABOVE_VIEWPORT = -1; // 视口上方
    private static final int BLOW_VIEWPORT = -2; // 视口下方
    private static final int IN_VIEWPORT_BLANK = 0; // 视口内空白处
    // 右键菜单项
    private static final MenuItem ENABLE_DRAG_ROW_MENU_ITEM = new MenuItem("允许拖拽行");
    private static final MenuItem RENAME_MENU_ITEM = new MenuItem("重命名");
    private static final MenuItem OPEN_BY_BROWSER = new MenuItem("打开文件所在的目录");
    // 备选艺术家和专辑集合
    private static final HashSet<String> ALTERNATIVE_ARTISTS = new HashSet<>();
    private static final HashSet<String> ALTERNATIVE_ALBUMS = new HashSet<>();
    private static final String ALLOW = "允许拖拽行";
    private static final String BAN = "禁止拖拽行";
    // 拖拽数据传输格式
    private static final DataFormat DATA_FORMAT = new DataFormat("application/x-java-serialized-object");
    // 用于过滤功能的临时存储
    private static final ArrayList<AudioMetaData> BEFORE_FILTERING = new ArrayList<>();
    // 滚动条相关
    private static ScrollBar verticalScrollBar = null;
    private static double horizontalScrollBarHeight;
    private static double tableHeadRowHeight;
    private static RadioButton enableDragRowRadioButton = null;
    // 拖拽行相关控制
    private static boolean disableDragRow = true;
    // 删除特定标签的菜单
    private static Menu deleteSpecificTagMenu;
    // 在 javafx 中一个完整的拖动事件也是单击事件
    // 鼠标是否进行过拖动：用来确认是到底是拖动还是单击
    private static boolean mouseDragged = false;
    // 起始行号：鼠标按下时的行号
    private static Integer indexWhenDragStart = null;
    // 鼠标拖动结束时最后被选中的行的行号，对应行的内容将展示在侧边栏
    private static Integer indexOfLastSelectedRow = null;
    private static ScheduledExecutorService executorService;
    // 滚动条是否在向上滚动
    private static boolean moveScrollBarUp;

    // 表格初始化块
    static {
        // 设置表格样式
        TABLE_VIEW.getStyleClass().add(Styles.BORDERED);
        TABLE_VIEW.getStyleClass().add(Styles.DENSE);

        // 创建表格列
        createColumn();

        // 设置行工厂，使行可拖拽
        TABLE_VIEW.setRowFactory(table -> {
            TableRow<AudioMetaData> row = new TableRow<>();
            makeRowDraggable(row);
            return row;
        });

        // 显示表头菜单按钮
        TABLE_VIEW.setTableMenuButtonVisible(true);
        // 允许多选
        TABLE_VIEW.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // 配置上下文菜单
        configContextMenu();

        // 监听选中项变化，更新相关按钮状态
        ObservableList<AudioMetaData> items = TABLE_VIEW.getSelectionModel().getSelectedItems();
        items.addListener((ListChangeListener<AudioMetaData>) listener -> {
            // 只在选中一个时允许重命名
            RENAME_MENU_ITEM.setDisable(listener.getList().size() != 1);
            // 只在选中一个时允许打开文件所在目录
            OPEN_BY_BROWSER.setDisable(listener.getList().size() != 1);
            // 没有选中时禁用删除特定标签菜单
            deleteSpecificTagMenu.setDisable(items.isEmpty());
        });

        // 初始化表格内容(从Session中读取上次展示的内容)
        initContent();
    }

    /**
     * 创建表格列
     */
    private static void createColumn() {
        // 文件名列
        TableColumn<AudioMetaData, String> filenameColumn = new TableColumn<>("文件名");
        filenameColumn.setPrefWidth(235);
        filenameColumn.setId("filename");
        filenameColumn.setCellValueFactory(new PropertyValueFactory<>("filename"));
        filenameColumn.setReorderable(false);

        // 标题列
        TableColumn<AudioMetaData, String> titleColumn = new TableColumn<>("标题");
        titleColumn.setPrefWidth(175);
        titleColumn.setId("title");
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));

        // 艺术家列
        TableColumn<AudioMetaData, String> artistColumn = new TableColumn<>("艺术家");
        artistColumn.setPrefWidth(150);
        artistColumn.setId("artist");
        artistColumn.setCellValueFactory(new PropertyValueFactory<>("artist"));

        // 专辑列
        TableColumn<AudioMetaData, String> albumColumn = new TableColumn<>("专辑");
        albumColumn.setPrefWidth(175);
        albumColumn.setId("album");
        albumColumn.setCellValueFactory(new PropertyValueFactory<>("album"));

        // 出版日期列
        TableColumn<AudioMetaData, String> dateColumn = new TableColumn<>("出版日期");
        dateColumn.setPrefWidth(100);
        dateColumn.setId("date");
        dateColumn.setStyle("-fx-alignment: CENTER-RIGHT");
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));

        // 流派列
        TableColumn<AudioMetaData, String> genreColumn = new TableColumn<>("流派");
        genreColumn.setPrefWidth(85);
        genreColumn.setId("genre");
        genreColumn.setCellValueFactory(new PropertyValueFactory<>("genre"));

        // 音轨序号列
        TableColumn<AudioMetaData, String> trackColumn = new TableColumn<>("音轨序号");
        trackColumn.setPrefWidth(75);
        trackColumn.setId("track");
        trackColumn.setCellValueFactory(new PropertyValueFactory<>("track"));

        // 备注列
        TableColumn<AudioMetaData, String> commentColumn = new TableColumn<>("备注");
        commentColumn.setPrefWidth(200);
        commentColumn.setId("comment");
        commentColumn.setCellValueFactory(new PropertyValueFactory<>("comment"));

        // 比特率列
        TableColumn<AudioMetaData, String> bitrateColumn = new TableColumn<>("比特率");
        bitrateColumn.setPrefWidth(105);
        bitrateColumn.setId("bitrate");
        bitrateColumn.setStyle("-fx-alignment: CENTER-RIGHT");
        bitrateColumn.setCellValueFactory(new PropertyValueFactory<>("bitrate"));

        // 时长列
        TableColumn<AudioMetaData, String> lengthColumn = new TableColumn<>("时长");
        lengthColumn.setPrefWidth(65);
        lengthColumn.setId("length");
        lengthColumn.setStyle("-fx-alignment: CENTER-RIGHT");
        lengthColumn.setCellValueFactory(new PropertyValueFactory<>("length"));

        // 按照Session中记录的列顺序添加列
        HashMap<String, TableColumn<AudioMetaData, String>> idToColumn = new HashMap<>(10);
        idToColumn.put("filename", filenameColumn);
        idToColumn.put("title", titleColumn);
        idToColumn.put("artist", artistColumn);
        idToColumn.put("album", albumColumn);
        idToColumn.put("date", dateColumn);
        idToColumn.put("genre", genreColumn);
        idToColumn.put("track", trackColumn);
        idToColumn.put("comment", commentColumn);
        idToColumn.put("bitrate", bitrateColumn);
        idToColumn.put("length", lengthColumn);

        // 从Session获取列顺序并添加列
        HashMap<Integer, String> columnsOrder = Session.getColumnsOrder();
        for (int i = 0; i < 10; i++) {
            String id = columnsOrder.get(i);
            TABLE_VIEW.getColumns().add(idToColumn.get(id));
        }
    }

    /**
     * 使表格行可拖拽
     *
     * @param row 表格行
     */
    private static void makeRowDraggable(TableRow<AudioMetaData> row) {
        // 拖拽检测 - 鼠标按下开始拖拽
        row.setOnDragDetected(event -> {
            if (disableDragRow) return;

            if (!row.isEmpty()) {
                Integer index = row.getIndex();
                Dragboard db = row.startDragAndDrop(TransferMode.MOVE);
                db.setDragView(row.snapshot(null, null)); // 设置拖拽视觉反馈
                ClipboardContent cc = new ClipboardContent();
                cc.put(DATA_FORMAT, index); // 设置拖拽数据
                db.setContent(cc);
                event.consume();
            }
        });

        // 拖拽悬停 - 接受拖拽数据
        row.setOnDragOver(event -> {
            if (disableDragRow) return;

            Dragboard db = event.getDragboard();
            if (db.hasContent(DATA_FORMAT)) {
                if (row.getIndex() != (Integer) db.getContent(DATA_FORMAT)) {
                    event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
                    event.consume();
                }
            }
        });

        // 拖拽释放 - 完成拖拽操作
        row.setOnDragDropped(event -> {
            if (disableDragRow) return;

            Dragboard db = event.getDragboard();
            if (db.hasContent(DATA_FORMAT)) {
                int draggedIndex = (Integer) db.getContent(DATA_FORMAT);
                AudioMetaData draggedMetaData = TABLE_VIEW.getItems().remove(draggedIndex);

                int dropIndex = row.isEmpty() ? TABLE_VIEW.getItems().size() : row.getIndex();
                TABLE_VIEW.getItems().add(dropIndex, draggedMetaData); // 插入到目标位置

                event.setDropCompleted(true);
                TABLE_VIEW.getSelectionModel().clearSelection();
                TABLE_VIEW.getSelectionModel().select(dropIndex); // 选中移动后的行
                event.consume();
            }
        });
    }

    /**
     * 配置右键上下文菜单
     */
    private static void configContextMenu() {
        // 全选菜单项
        MenuItem selectAll = new MenuItem("全选");
        selectAll.setOnAction(event -> selectAll());

        // 重命名菜单项
        RENAME_MENU_ITEM.setOnAction(event -> Rename.show(TABLE_VIEW.getSelectionModel().getSelectedItem()));

        // 允许/禁止拖拽行菜单项
        ENABLE_DRAG_ROW_MENU_ITEM.setOnAction(event -> {
            switch (ENABLE_DRAG_ROW_MENU_ITEM.getText()) {
                case ALLOW -> {
                    enableDragRow(true);
                    enableDragRowRadioButton.setSelected(true);
                    ENABLE_DRAG_ROW_MENU_ITEM.setText(BAN);
                }
                case BAN -> {
                    enableDragRow(false);
                    enableDragRowRadioButton.setSelected(false);
                    ENABLE_DRAG_ROW_MENU_ITEM.setText(ALLOW);
                }
            }
        });

        // 基于标签重命名菜单项
        MenuItem renameBaseOnTags = new MenuItem("根据标签重命名");
        renameBaseOnTags.setOnAction(event -> renameBaseOnTags());

        // 基于文件名添加标签菜单项
        MenuItem addTagsBaseOnFilename = new MenuItem("基于文件名添加标签");
        addTagsBaseOnFilename.setOnAction(event -> addTagBaseOnFilename());

        // 从表格中移除菜单项
        MenuItem deleteFromTable = new MenuItem("从表格中移除");
        deleteFromTable.setOnAction(event -> removeSelectedItems());

        // 删除文件菜单项
        MenuItem deleteFile = new MenuItem("删除文件");
        deleteFile.setOnAction(event -> deleteSelectedItems());

        // 添加序号菜单项
        MenuItem addOrder = new MenuItem("从上至下依次添加序号");
        addOrder.setOnAction(event -> addOrder());

        // 生成删除特定标签菜单
        deleteSpecificTagMenu = generateDeleteSpecificTagMenu();

        // 打包为同一专辑菜单项
        MenuItem packageToAlbum = new MenuItem("设置成同一专辑");
        packageToAlbum.setOnAction(event -> packageToAlbum());

        // 打开文件所在目录菜单项
        OPEN_BY_BROWSER.setOnAction(event -> {
            AudioMetaData audioMetaData = TABLE_VIEW.getSelectionModel().getSelectedItem();
            String path = audioMetaData.getAbsolutePath();
            try {
                Desktop.getDesktop().open(new File(path).getParentFile()); // 使用系统文件浏览器打开
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        // 取消菜单项
        MenuItem cancel = new MenuItem("取消");
        cancel.setOnAction(event -> TABLE_VIEW.getSelectionModel().clearSelection());

        // 添加所有菜单项到上下文菜单
        CONTEXT_MENU.getItems().addAll(selectAll,
                ENABLE_DRAG_ROW_MENU_ITEM,
                RENAME_MENU_ITEM,
                renameBaseOnTags,
                addTagsBaseOnFilename,
                deleteFromTable,
                deleteFile,
                addOrder,
                deleteSpecificTagMenu,
                packageToAlbum,
                generateSameAlbumOptionMenu(), // 批量设置专辑选项菜单
                generateTidyMenu(), // 整理菜单
                OPEN_BY_BROWSER,
                cancel);

        TABLE_VIEW.setContextMenu(CONTEXT_MENU);
    }

    /**
     * 全选表格内容
     */
    public static void selectAll() {
        TABLE_VIEW.getSelectionModel().selectAll();
    }

    /**
     * 启用/禁用行拖拽功能
     *
     * @param enable 是否启用
     */
    private static void enableDragRow(boolean enable) {
        disableDragRow = !enable;
    }

    /**
     * 基于标签重命名文件
     */
    public static void renameBaseOnTags() {
        List<AudioMetaData> selectedItems = checkSelectedRows();
        if (selectedItems == null) return;

        RenameBaseOnTag.show(selectedItems);
        updateTableView(null);
    }

    /**
     * 检查选中行，如果未选中任何行且NeedToSelectAll设为true，则返回全部行
     *
     * @return 选中行列表，或null(如果未选中且不需要全选)
     */
    public static List<AudioMetaData> checkSelectedRows() {
        List<AudioMetaData> selectedItems = TABLE_VIEW.getSelectionModel().getSelectedItems();
        if (selectedItems.isEmpty()) {
            return NeedToSelectAll.isSelectAll() ? TABLE_VIEW.getItems() : null;
        }
        return selectedItems;
    }

    /**
     * 更新表格视图
     *
     * @param dataList 新数据列表，如果为null则刷新当前表格
     */
    public static void updateTableView(List<AudioMetaData> dataList) {
        if (dataList == null) {
            // 刷新当前表格
            recordeUpdate(TABLE_VIEW.getItems());
            TABLE_VIEW.refresh();
        } else {
            // 显示新数据
            Aside.showBlank();
            recordeUpdate(dataList);
            TABLE_VIEW.getItems().clear();
            TABLE_VIEW.getItems().addAll(dataList);
            TABLE_VIEW.refresh();
        }
    }

    /**
     * 记录表格内容更新，生成备选艺术家和专辑集合
     *
     * @param dataList 数据列表
     */
    private static void recordeUpdate(List<AudioMetaData> dataList) {
        ALTERNATIVE_ARTISTS.clear();
        ALTERNATIVE_ALBUMS.clear();

        List<String> paths = new ArrayList<>(dataList.size());

        // 收集所有非空艺术家和专辑
        for (AudioMetaData metaData : dataList) {
            String artist = metaData.getArtist();
            if (artist != null && !"".equals(artist.trim())) {
                ALTERNATIVE_ARTISTS.add(artist.trim());
            }

            String album = metaData.getAlbum();
            if (album != null && !"".equals(album.trim())) {
                ALTERNATIVE_ALBUMS.add(album.trim());
            }

            paths.add(metaData.getAbsolutePath());
        }

        // 记录当前表格内容路径到Session
        Session.setCurrentTableViewContentPaths(paths);
    }

    /**
     * 获取备选艺术家列表
     *
     * @return 艺术家列表
     */
    public static List<String> getAlternativeArtists() {
        return new ArrayList<>(ALTERNATIVE_ARTISTS);
    }

    /**
     * 获取备选专辑列表
     *
     * @return 专辑列表
     */
    public static List<String> getAlternativeAlbums() {
        return new ArrayList<>(ALTERNATIVE_ALBUMS);
    }

    /**
     * 基于文件名添加标签
     */
    public static void addTagBaseOnFilename() {
        List<AudioMetaData> selectedItems = checkSelectedRows();
        if (selectedItems == null) return;

        AddTagBaseOnFilename.show(selectedItems);
        updateTableView(null);
    }

    /**
     * 从表格中移除选中项
     */
    public static void removeSelectedItems() {
        List<AudioMetaData> selectedItems = TABLE_VIEW.getSelectionModel().getSelectedItems();

        if (selectedItems.isEmpty()) {
            // 如果未选中但设定了全选，则清空表格
            if (NeedToSelectAll.isSelectAll()) {
                TABLE_VIEW.getItems().clear();
            }
        } else {
            TABLE_VIEW.getItems().removeAll(selectedItems);

            updateTableView(null);
        }
    }

    /**
     * 删除选中文件
     */
    public static void deleteSelectedItems() {
        List<AudioMetaData> selectedItems = checkSelectedRows();
        if (selectedItems == null) return;

        List<AudioMetaData> succeed = Delete.show(selectedItems);
        TABLE_VIEW.getItems().removeAll(succeed);
        updateTableView(null);
    }

    /**
     * 为选中行添加序号
     */
    public static void addOrder() {
        List<AudioMetaData> selectedItems = checkSelectedRows();
        if (selectedItems == null) return;

        // 从1开始依次添加序号
        for (int i = 0; i < selectedItems.size(); i++) {
            selectedItems.get(i).setTrack(String.valueOf(i + 1));
        }
        Aside.refresh();
        updateTableView(null);
        Notification.showNotification("序号已添加");
    }

    /**
     * 生成删除特定标签菜单
     *
     * @return 删除标签菜单
     */
    public static Menu generateDeleteSpecificTagMenu() {
        Menu deleteSpecificTag = new Menu("删除特定标签");

        // 创建各种标签删除菜单项
        MenuItem deleteTitle = new MenuItem("删除标题");
        deleteTitle.setOnAction(event -> deleteTag(EditableTag.TITLE));

        MenuItem deleteArtist = new MenuItem("删除艺术家");
        deleteArtist.setOnAction(event -> deleteTag(EditableTag.ARTIST));

        MenuItem deleteAlbum = new MenuItem("删除专辑");
        deleteAlbum.setOnAction(event -> deleteTag(EditableTag.ALBUM));

        MenuItem deleteDate = new MenuItem("删除出版日期");
        deleteDate.setOnAction(event -> deleteTag(EditableTag.DATE));

        MenuItem deleteGenre = new MenuItem("删除流派");
        deleteGenre.setOnAction(event -> deleteTag(EditableTag.GENRE));

        MenuItem deleteTrack = new MenuItem("删除音轨序号");
        deleteTrack.setOnAction(event -> deleteTag(EditableTag.TRACK));

        MenuItem deleteComment = new MenuItem("删除备注");
        deleteComment.setOnAction(event -> deleteTag(EditableTag.COMMENT));

        MenuItem deleteCover = new MenuItem("删除封面");
        deleteCover.setOnAction(event -> deleteTag(EditableTag.COVER));

        MenuItem deleteAll = new MenuItem("删除全部标签");
        deleteAll.setOnAction(event -> deleteTag(EditableTag.ALL));

        // 添加所有菜单项
        deleteSpecificTag.getItems().addAll(deleteTitle, deleteArtist, deleteAlbum, deleteDate,
                deleteGenre, deleteTrack, deleteComment, deleteCover, deleteAll);
        return deleteSpecificTag;
    }

    /**
     * 删除指定类型标签
     *
     * @param tag 标签类型
     */
    private static void deleteTag(EditableTag tag) {
        ObservableList<AudioMetaData> items = TABLE_VIEW.getSelectionModel().getSelectedItems();

        if (items.isEmpty()) {
            return;
        }

        // 根据标签类型清除相应字段
        for (AudioMetaData item : items) {
            switch (tag) {
                case TITLE -> item.setTitle("");
                case ARTIST -> item.setArtist("");
                case ALBUM -> item.setAlbum("");
                case DATE -> item.setDate("");
                case GENRE -> item.setGenre("");
                case TRACK -> item.setTrack("");
                case COMMENT -> item.setComment("");
                case COVER -> item.setCover(null);
                case ALL -> { // 清除所有标签
                    item.setTitle("");
                    item.setArtist("");
                    item.setAlbum("");
                    item.setDate("");
                    item.setGenre("");
                    item.setTrack("");
                    item.setComment("");
                    item.setCover(null);
                }
            }
            MetaDataWriter.write(item); // 写入文件
        }

        Aside.refresh();
        updateTableView(null);
        Notification.showNotification("删除完毕");
    }

    /**
     * 将选中项设置为同一专辑
     */
    public static void packageToAlbum() {
        List<AudioMetaData> selectedItems = checkSelectedRows();
        if (selectedItems == null) return;

        String albumName = PackageToAlbum.show();
        if (albumName == null) return;

        // 为所有选中项设置相同专辑名
        for (AudioMetaData selectedItem : selectedItems) {
            selectedItem.setAlbum(albumName);
            MetaDataWriter.write(selectedItem);
        }
        Aside.refresh();
        updateTableView(null);
        Notification.showNotification("已设置为同一专辑");
    }

    /**
     * 生成批量设置同一专辑选项菜单
     *
     * @return 专辑选项菜单
     */
    public static Menu generateSameAlbumOptionMenu() {
        Menu menu = new Menu("批量设置专辑");

        // 添加同一封面菜单项
        MenuItem addCover = new MenuItem("为同一专辑添加同一封面");
        addCover.setOnAction(event -> addCoverForSameAlbum());

        // 添加同一艺术家菜单项
        MenuItem addArtist = new MenuItem("为同一专辑添加同一艺术家");
        addArtist.setOnAction(event -> addArtistForSameAlbum());

        // 添加同一流派菜单项
        MenuItem addGenre = new MenuItem("为同一专辑添加同一流派");
        addGenre.setOnAction(event -> addGenreForSameAlbum());

        // 添加同一日期菜单项
        MenuItem addDate = new MenuItem("为同一专辑添加同一出版日期");
        addDate.setOnAction(event -> addDateForSameAlbum());

        menu.getItems().addAll(addCover, addArtist, addGenre, addDate);

        return menu;
    }

    /**
     * 为同一专辑添加同一封面
     */
    public static void addCoverForSameAlbum() {
        List<AudioMetaData> selectedItems = checkSelectedRows();
        if (selectedItems == null) return;
        // albumCovers 专辑 -> 封面 映射表
        HashMap<String, byte[]> albumCovers = new HashMap<>();
        for (AudioMetaData selectedItem : selectedItems) {
            String album = selectedItem.getAlbum();
            byte[] cover = selectedItem.getCover();
            if (!"".equals(album) && cover != null) {
                cover = Utils.retouchCover(cover);
                albumCovers.put(album, cover);
            }
        }

        for (AudioMetaData selectedItem : selectedItems) {
            String album = selectedItem.getAlbum();
            if (!"".equals(album)) {
                // 根据 专辑 找到 封面
                selectedItem.setCover(albumCovers.get(album));
                MetaDataWriter.write(selectedItem);
            }
        }
        Aside.refresh();
        updateTableView(null);
        Notification.showNotification("专辑封面更新完毕");
    }

    /**
     * 为同一专辑添加同一艺术家
     */
    public static void addArtistForSameAlbum() {
        List<AudioMetaData> selectedItems = checkSelectedRows();
        if (selectedItems == null) return;
        // albumArtist 专辑 -> 艺术家 映射表
        HashMap<String, String> albumArtist = new HashMap<>();
        for (AudioMetaData selectedItem : selectedItems) {
            String album = selectedItem.getAlbum();
            String artist = selectedItem.getArtist();
            if (!"".equals(album) && !"".equals(artist)) {
                albumArtist.put(album, artist);
            }
        }

        for (AudioMetaData selectedItem : selectedItems) {
            String album = selectedItem.getAlbum();
            if (!"".equals(album)) {
                // 根据 专辑 找到 艺术家
                selectedItem.setArtist(albumArtist.get(album));
            }
        }
        Aside.refresh();
        updateTableView(null);
        Notification.showNotification("专辑艺术家更新完毕");
    }

    /**
     * 为同一专辑添加同一流派
     */
    private static void addGenreForSameAlbum() {
        List<AudioMetaData> selectedItems = checkSelectedRows();
        if (selectedItems == null) return;
        // albumGenre 专辑 -> 流派 映射表
        HashMap<String, String> albumGenre = new HashMap<>();
        for (AudioMetaData item : selectedItems) {
            String album = item.getAlbum();
            String genre = item.getGenre();
            if (!"".equals(album) && !"".equals(genre)) {
                albumGenre.put(album, genre);
            }
        }

        for (AudioMetaData item : selectedItems) {
            String album = item.getAlbum();
            if (!"".equals(album)) {
                // 根据 专辑 找到 流派
                item.setGenre(albumGenre.get(album));
                MetaDataWriter.write(item);
            }
        }
        Aside.refresh();
        updateTableView(null);
        Notification.showNotification("专辑流派更新完毕");
    }

    /**
     * 为同一专辑添加同一日期
     */
    private static void addDateForSameAlbum() {
        List<AudioMetaData> selectedItems = checkSelectedRows();
        if (selectedItems == null) return;
        // albumDate 专辑 -> 发行日期 映射表
        HashMap<String, String> albumDate = new HashMap<>();
        for (AudioMetaData selectedItem : selectedItems) {
            String album = selectedItem.getAlbum();
            String date = selectedItem.getDate();
            if (!"".equals(album) && !"".equals(date)) {
                albumDate.put(album, date);
            }
        }

        for (AudioMetaData selectedItem : selectedItems) {
            String album = selectedItem.getAlbum();
            if (!"".equals(album)) {
                // 根据 专辑 找到 发行日期
                selectedItem.setDate(albumDate.get(album));
                MetaDataWriter.write(selectedItem);
            }
        }
        Aside.refresh();
        updateTableView(null);
        Notification.showNotification("专辑发行日期更新完毕");
    }

    /**
     * 生成整理菜单
     */
    public static Menu generateTidyMenu() {
        Menu tidyMenu = new Menu("整理");

        MenuItem tidyByArtist = new MenuItem("将同一歌手的文件放置在同一文件夹");
        tidyByArtist.setOnAction(event -> {
            List<AudioMetaData> dataList = checkSelectedRows();
            if (dataList == null) return;

            Tidy.show(dataList, true);
        });

        MenuItem tidyByAlbum = new MenuItem("将同一专辑的文件放置在同一文件夹");
        tidyByAlbum.setOnAction(event -> {
            List<AudioMetaData> dataList = checkSelectedRows();
            if (dataList == null) return;

            Tidy.show(dataList, false);
        });

        tidyMenu.getItems().addAll(tidyByArtist, tidyByAlbum);
        return tidyMenu;
    }

    /**
     * 初始化表格内容
     */
    private static void initContent() {
        List<String> paths = Session.getCurrentTableViewContentPaths();
        List<AudioMetaData> audioMetaData = new ArrayList<>(paths.size());
        paths.forEach(path -> {
            try {
                audioMetaData.add(MetaDataReader.readFile(new File(path)));
            } catch (CantReadException ignored) {
                // 忽略无法读取的文件
            }
        });
        updateTableView(audioMetaData);
    }

    /**
     * 表格渲染完成后配置相关设置
     */
    public static void configWhenTableAlreadyRendered() {
        // 获取表头高度
        TableViewSkin<?> skin = (TableViewSkin<?>) TABLE_VIEW.getSkin();
        ObservableList<Node> childrenList = skin.getChildren();
        for (Node node : childrenList) {
            if (node instanceof TableHeaderRow headerRow) {
                tableHeadRowHeight = headerRow.getHeight();
            }
        }

        // 处理鼠标点击事件
        handleMouseClicked();
        // 获取并配置滚动条
        getAndConfigScrollBar();
        // 实现拖选功能
        implementDragAndSelect();
    }

    /**
     * 处理鼠标点击事件
     */
    private static void handleMouseClicked() {
        TABLE_VIEW.setOnMouseClicked(event -> {
            // 处理单击事件
            if (event.getClickCount() == 1) {
                // 处理鼠标左键单击
                if (event.getButton().equals(MouseButton.PRIMARY)) {
                    handlePrimaryButtonClicked(event);
                }
                // 处理鼠标右键单击
                if (event.getButton().equals(MouseButton.SECONDARY)) {
                    handleSecondaryButtonClick(event);
                }
            }

            // 处理双击事件
            if (event.getClickCount() == 2) {
                if (event.getButton().equals(MouseButton.PRIMARY)) {
                    // 获取当前行号
                    int currentIndex = getItemIndex(event.getY());
                    if (currentIndex > 0) {
                        // 获取当前行
                        AudioMetaData currentItem = TABLE_VIEW.getItems().get(currentIndex - 1);
                        // 将当前行选中
                        selectItem(currentItem);
                        // 使用系统默认方式打开文件
                        try {
                            Desktop.getDesktop().open(new File(currentItem.getAbsolutePath()));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        });
    }

    /**
     * 根据鼠标位置计算出该位置对应的实际行号
     * 如果有对应的行，返回的行号大于0（行号从1开始）
     * 否则返回 ABOVE_VIEWPORT（-1：在视口上方）
     * 或 BLOW_VIEWPORT（-2：在视口下方）
     * 或 IN_VIEWPORT_BLANK（0：在视口内的空白处）
     * currentY：鼠标位置的 y 坐标（通过 event.getY() 得到的值）
     */
    private static int getItemIndex(double currentY) {
        // 视口底部的 y 坐标
        double upperLimitY = TABLE_VIEW.getHeight() - horizontalScrollBarHeight;
        // 当鼠标位于视口上方，返回 ABOVE_VIEWPORT
        if (currentY <= tableHeadRowHeight) {
            return ABOVE_VIEWPORT;
        }
        // 当鼠标位于视口下方，返回 BLOW_VIEWPORT
        if (currentY >= upperLimitY) {
            return BLOW_VIEWPORT;
        }

        double rowHeight = getRowHeight();
        // 内容的实际高度（以内容的顶部为原点，越向下，高度值递增，行号递增）
        double contentHeight = rowHeight * TABLE_VIEW.getItems().size();
        if (verticalScrollBar.isVisible()) { // 垂直滚动条可见时行高的计算方式
            // 除视口外的高度 = 表头的高度 + 水平滚动条的高度
            // 视口的高度 = 表格的高度 - 除视口外的高度
            double heightExceptViewport = tableHeadRowHeight + horizontalScrollBarHeight;
            double viewportHeight = TABLE_VIEW.getHeight() - heightExceptViewport;
            // 最大偏移量 = 内容的实际高度 - 视口的高度
            double maxOffset = contentHeight - viewportHeight;
            // 偏移量 = 最大偏移量 * 滚动条的偏移比例
            double proportion = verticalScrollBar.getValue() / verticalScrollBar.getMax();
            double offset = maxOffset * proportion;
            // total: 鼠标相对于内容的实际高度
            double total = offset + (currentY - tableHeadRowHeight);
            // 行号 = 鼠标相对于内容的实际高度 / 行高 （向上取整）
            return (int) Math.ceil(total / rowHeight);
        } else {
            // 垂直滚动条不可见时行高的计算方式
            double validY = contentHeight + tableHeadRowHeight;
            if (currentY > validY) {
                return IN_VIEWPORT_BLANK;
            } else {
                return (int) Math.ceil((currentY - tableHeadRowHeight) / rowHeight);
            }
        }
    }

    /**
     * 获取行高
     */
    private static double getRowHeight() {
        // 确保表格有内容
        if (TABLE_VIEW.getItems().isEmpty()) {
            TABLE_VIEW.getItems().add(null); // 临时添加空项
        }

        // 获取第一行
        TableRow<?> row = (TableRow<?>) TABLE_VIEW.lookup(".table-row-cell");

        // 恢复表格状态
        if (TABLE_VIEW.getItems().get(0) == null) {
            TABLE_VIEW.getItems().remove(0);
        }

        if (row != null) {
            return row.getHeight();
        }

        // 如果无法获取行，返回默认值或估算值
        return 24; // 默认行高
    }

    /**
     * 处理鼠标左键单击事件
     */
    private static void handlePrimaryButtonClicked(MouseEvent event) {
        // 在 javafx 中一个完整的拖动事件也是单击事件
        // 如果是拖动行为，则不执行下面的左键单击逻辑
        if (mouseDragged) {
            mouseDragged = false;
            return;
        }
        // 左键单击选中某行后在侧边栏中显示该行数据,否则显示空白
        int itemIndex = getItemIndex(event.getY());
        if (itemIndex > 0) {
            Aside.showMetaData(TABLE_VIEW.getItems().get(itemIndex - 1));
        } else {
            TABLE_VIEW.getSelectionModel().clearSelection();
            Aside.showBlank();
        }
    }

    /**
     * 处理鼠标右键单击事件
     */
    private static void handleSecondaryButtonClick(MouseEvent event) {
        int itemIndex = getItemIndex(event.getY());
        if (itemIndex > 0) { // 非空白处右键单击
            // 在侧边栏中显示该行数据
            Aside.showMetaData(TABLE_VIEW.getItems().get(itemIndex - 1));
        } else { // 空白处右键单击
            // 侧边栏显示空白
            Aside.showBlank();
        }
    }

    /**
     * 获取竖向滚动条 监听横向滚动条高度
     */
    private static void getAndConfigScrollBar() {
        Set<Node> nodes = TABLE_VIEW.lookupAll(".scroll-bar");
        for (Node node : nodes) {
            if (node instanceof ScrollBar scrollBar) {
                if (scrollBar.getOrientation().equals(Orientation.VERTICAL)) {
                    verticalScrollBar = scrollBar;
                }

                // 监听横向滚动条的高度
                if (scrollBar.getOrientation().equals(Orientation.HORIZONTAL)) {
                    scrollBar.heightProperty().addListener((ob, o, n) -> horizontalScrollBarHeight = n.doubleValue());
                }
            }
        }
    }

    /**
     * 实现框选功能
     */
    public static void implementDragAndSelect() {
        // 鼠标左键按下/拖动开始的时候，记录其位置对应的行号
        TABLE_VIEW.setOnMousePressed(event -> {
            // 如果是在视口以外的地方按下该语句不会执行， 所以 indexWhenDragStart 不会等于 -1
            // 如果该值为 0 ，则表示鼠标按下的时候没有选中某一行（鼠标从空白处按下）
            if (event.getButton().equals(MouseButton.PRIMARY)) {
                indexWhenDragStart = getItemIndex(event.getY());
            }
        });

        TABLE_VIEW.setOnMouseDragged(Center::handleMouseDragged);

        TABLE_VIEW.setOnMouseReleased(event -> {
            stopAutoScrolling();
            indexWhenDragStart = null;
        });
    }

    /**
     * 处理鼠标拖拽事件
     */
    private static void handleMouseDragged(MouseEvent event) {
        // 右键拖动不处理
        if (event.getButton().equals(MouseButton.SECONDARY)) return;

        mouseDragged = true;

        // 视口底部的 y 坐标
        double upperLimitY = TABLE_VIEW.getHeight() - horizontalScrollBarHeight;
        // 结束行号：鼠标拖动到某位置时对应的行号
        int indexWhenDragged;
        if (verticalScrollBar.isVisible()) {
            if (event.getY() >= upperLimitY) {
                // 鼠标拖动到位于视口下方时，自动向下滚动
                verticalScrollBar.increment();
                // 获取行号时将鼠标的位置调整为视口底部偏上一点点
                indexWhenDragged = getItemIndex(upperLimitY - 0.1);
                int start = Math.min(indexWhenDragStart, indexWhenDragged);
                int end = Math.max(indexWhenDragStart, indexWhenDragged);
                selectIndices(start, end);
                // 此时最后选中的行号是鼠标拖动到的行号
                Aside.showMetaData(TABLE_VIEW.getItems().get(indexWhenDragged - 1));
                // 如果鼠标在视口下方按下鼠标不动，则接着自动向下滚动
                moveScrollBarUp = false;
                startAutoScrolling();
            } else if (event.getY() <= tableHeadRowHeight) {
                // tableHeadRowHeight等同于视口顶部的 y 坐标
                // 鼠标拖动到位于视口上方时，自动向上滚动
                verticalScrollBar.decrement();
                // 获取行号时将鼠标的位置调整为视口顶部偏下一点点
                indexWhenDragged = getItemIndex(tableHeadRowHeight + 0.1);
                int start = Math.min(indexWhenDragStart, indexWhenDragged);
                int end = Math.max(indexWhenDragStart, indexWhenDragged);
                selectIndices(start, end);
                // 此时最后选中的行号是鼠标拖动到的行号
                Aside.showMetaData(TABLE_VIEW.getItems().get(indexWhenDragged - 1));
                // 如果鼠标在视口上方按下鼠标不动，则接着自动向上滚动
                moveScrollBarUp = true;
                startAutoScrolling();
            } else {
                // 出现了滚动条，但鼠标没有拖动到视口外面，
                // 此时鼠标已经回到了视口中，应当停止滚动条自动滚动
                stopAutoScrolling();
                // 此时鼠标的位置必定对应着某一行，
                // 故此直接获取鼠标位置对应的行号
                indexWhenDragged = getItemIndex(event.getY());
                int start = Math.min(indexWhenDragStart, indexWhenDragged);
                int end = Math.max(indexWhenDragStart, indexWhenDragged);
                selectIndices(start, end);
                // 此时最后选中的行号是鼠标拖动到的行号
                Aside.showMetaData(TABLE_VIEW.getItems().get(indexWhenDragged - 1));
            }
        } else {
            // 实际内容高度没有超过视口的高度，
            // 此时鼠标的位置可能在某一行上，也可能在视口内的空白处，也可能在视口外面
            // 下面会进行分类讨论
            indexWhenDragged = getItemIndex(event.getY());

            // 从某一行开始
            if (indexWhenDragStart > 0) {
                // 到某一行结束
                if (indexWhenDragged > 0) {
                    int start = Math.min(indexWhenDragStart, indexWhenDragged);
                    int end = Math.max(indexWhenDragStart, indexWhenDragged);
                    selectIndices(start, end);
                    // 此时最后选中的行号是鼠标拖动到的行号
                    indexOfLastSelectedRow = indexWhenDragged;
                }
                // 到视口空白处或视口下方结束
                if (indexWhenDragged == IN_VIEWPORT_BLANK || indexWhenDragged == BLOW_VIEWPORT) {
                    int start = indexWhenDragStart;
                    int end = TABLE_VIEW.getItems().size();
                    selectIndices(start, end);
                    // 此时最后选中的行号固定就是表格的最后一行
                    indexOfLastSelectedRow = end;
                }
                // 到视口上方结束
                if (indexWhenDragged == ABOVE_VIEWPORT) {
                    int start = 1;
                    int end = indexWhenDragStart;
                    selectIndices(start, end);
                    // 此时最后选中的行号固定就是表格的第一行
                    indexOfLastSelectedRow = 1;
                }
            }

            // 从视口空白处开始（空白处必然在在视口下部）
            if (indexWhenDragStart == IN_VIEWPORT_BLANK) {
                // 到某一行结束
                if (indexWhenDragged > 0) {
                    int end = TABLE_VIEW.getItems().size();
                    selectIndices(indexWhenDragged, end);
                    // 此时最后选中的行号是鼠标拖动到的行号
                    indexOfLastSelectedRow = indexWhenDragged;
                }
                // 到视口空白处或视口下方结束
                if (indexWhenDragged == IN_VIEWPORT_BLANK || indexWhenDragged == BLOW_VIEWPORT) {
                    TABLE_VIEW.getSelectionModel().clearSelection();
                    indexOfLastSelectedRow = null;
                }
                // 到视口上方结束（全选）
                if (indexWhenDragged == ABOVE_VIEWPORT) {
                    TABLE_VIEW.getSelectionModel().selectAll();
                    indexOfLastSelectedRow = 1;
                }
            }

            if (indexOfLastSelectedRow != null) {
                Aside.showMetaData(TABLE_VIEW.getItems().get(indexOfLastSelectedRow - 1));
            }
            // 重置变量
            indexOfLastSelectedRow = null;
        }
    }

    /**
     * 选中指定范围内的行
     */
    private static void selectIndices(int start, int end) {
        TABLE_VIEW.getSelectionModel().clearSelection();
        for (int i = start; i <= end; i++) {
            TABLE_VIEW.getSelectionModel().selectIndices(i - 1);
        }
    }

    /**
     * 启动滚动条自动滚动线程
     */
    public static void startAutoScrolling() {
        // 定时任务存在时不再创建新的
        if (executorService != null) return;

        executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(() -> Platform.runLater(() -> {
            int indexWhenDragged;
            if (moveScrollBarUp) {
                verticalScrollBar.decrement();
                // 获取行号时将鼠标的位置调整为视口顶部偏下一点点
                indexWhenDragged = getItemIndex(tableHeadRowHeight + 0.1);
            } else {
                verticalScrollBar.increment();
                // 获取行号时将鼠标的位置调整为视口底部偏上一点点
                double upperLimitY = TABLE_VIEW.getHeight() - horizontalScrollBarHeight;
                indexWhenDragged = getItemIndex(upperLimitY - 0.1);
            }
            // 当有滚动条时，经过上面的调整，框选动作总是从某一行开始到某一行结束
            int start = Math.min(indexWhenDragStart, indexWhenDragged);
            int end = Math.max(indexWhenDragStart, indexWhenDragged);
            selectIndices(start, end);

            Aside.showMetaData(TABLE_VIEW.getItems().get(indexWhenDragged - 1));
        }), 0, 70, TimeUnit.MILLISECONDS); // 每隔70毫秒执行一次
    }

    public static void stopAutoScrolling() {
        if (executorService != null) {
            // 强制终止
            executorService.shutdownNow();
            executorService = null;
        }
    }

    /**
     * 获取中央区域节点
     *
     * @return 表格视图节点
     */
    public static Node getCenter() {
        return TABLE_VIEW;
    }

    /**
     * 选中指定项目
     *
     * @param audioMetaData 要选中的音频元数据
     */
    public static void selectItem(AudioMetaData audioMetaData) {
        int index = TABLE_VIEW.getItems().indexOf(audioMetaData);
        TABLE_VIEW.getSelectionModel().select(index);
    }

    /**
     * 同步重命名按钮状态与右键菜单
     *
     * @param rename 重命名按钮
     */
    public static void takeOverRenameButton(Button rename) {
        rename.disableProperty().bind(RENAME_MENU_ITEM.disableProperty());
        rename.setOnAction(event -> Rename.show(TABLE_VIEW.getSelectionModel().getSelectedItem()));
    }

    /**
     * 同步允许拖拽行选项与右键菜单
     *
     * @param radioButton 单选按钮
     */
    public static void takeOverEnableDragRow(RadioButton radioButton) {
        enableDragRowRadioButton = radioButton;
        enableDragRowRadioButton.setOnAction(event -> {
            boolean enable = enableDragRowRadioButton.isSelected();
            if (enable) {
                enableDragRow(true);
                ENABLE_DRAG_ROW_MENU_ITEM.setText(BAN);
            } else {
                enableDragRow(false);
                ENABLE_DRAG_ROW_MENU_ITEM.setText(ALLOW);
            }
        });
    }

    /**
     * 同步删除特定标签菜单状态
     *
     * @param menu 菜单
     */
    public static void configDeleteSpecificTagMenu(Menu menu) {
        menu.disableProperty().bind(deleteSpecificTagMenu.disableProperty());
    }

    /**
     * 应用过滤
     */
    public static void filter() {
        List<AudioMetaData> before = TABLE_VIEW.getItems();
        BEFORE_FILTERING.addAll(before);
        Filter.show(before);
    }

    /**
     * 关闭过滤
     */
    public static void turnOffFilter() {
        if (BEFORE_FILTERING.isEmpty()) return;
        updateTableView(BEFORE_FILTERING);
        BEFORE_FILTERING.clear();
    }

    /**
     * 获取当前列顺序
     *
     * @return 列ID到顺序的映射
     */
    public static HashMap<Integer, String> getColumnOrder() {
        HashMap<Integer, String> columnOrder = new HashMap<>();
        ObservableList<TableColumn<AudioMetaData, ?>> columns = TABLE_VIEW.getColumns();
        for (int i = 0; i < columns.size(); i++) {
            columnOrder.put(i, columns.get(i).getId());
        }
        return columnOrder;
    }
}