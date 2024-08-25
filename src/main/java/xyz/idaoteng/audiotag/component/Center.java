package xyz.idaoteng.audiotag.component;

import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.skin.TableHeaderRow;
import javafx.scene.control.skin.TableViewSkin;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import xyz.idaoteng.audiotag.AudioMetaData;
import xyz.idaoteng.audiotag.Session;
import xyz.idaoteng.audiotag.core.MetaDataReader;
import xyz.idaoteng.audiotag.dialog.Delete;
import xyz.idaoteng.audiotag.dialog.Rename;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Center {
    private static final TableView<AudioMetaData> TABLE_VIEW = new TableView<>();
    private static ScrollBar verticalScrollBar = null;
    private static double horizontalScrollBarHeight;
    private static double heightOutsideContentSection;
    private static double tableHeadRowHeight;
    private static final int ABOVE_VIEWPORT = -1;
    private static final int BLOW_VIEWPORT = -2;
    private static final int IN_VIEWPORT_BLANK = 0;
    private static final double ROW_HEIGHT = 25;
    private static final ContextMenu CONTEXT_MENU = new ContextMenu();
    private static final MenuItem RENAME_MENU_ITEM = new MenuItem("重命名");

    private static final HashSet<String> ALTERNATIVE_ARTISTS = new HashSet<>();
    private static final HashSet<String> ALTERNATIVE_ALBUMS = new HashSet<>();

    // 初始化表格
    static {
        // 设置表格样式
        TABLE_VIEW.setMinHeight(600); // 600 刚好可以将侧边栏（Aside）中的所有组件显示出来
        // 添加一个右边框，其他几个方向不需要
        TABLE_VIEW.setStyle("-fx-border-style: solid; -fx-border-color: #cccccc; -fx-border-width: 0 1 0 0");

        // 创建列
        createColumn();

        // TableView 的行高不知道的怎么获取，故此手动设置为 ROW_HEIGHT
        TABLE_VIEW.setRowFactory(table -> {
            TableRow<AudioMetaData> row = new TableRow<>();
            row.setMinHeight(ROW_HEIGHT);
            row.setMaxHeight(ROW_HEIGHT);
            return row;
        });

        // 显示表头最右侧的 + 按钮
        TABLE_VIEW.setTableMenuButtonVisible(true);
        // 允许多选
        TABLE_VIEW.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // 配置右键菜单
        configContextMenu();

        // 初始化内容。在 Session 中记录了上次所展示的内容
        initContent();
    }

    private static void createColumn() {
        TableColumn<AudioMetaData, String> filenameColumn = new TableColumn<>("文件名");
        filenameColumn.setPrefWidth(235);
        filenameColumn.setCellValueFactory(new PropertyValueFactory<>("filename"));

        TableColumn<AudioMetaData, String> titleColumn = new TableColumn<>("标题");
        titleColumn.setPrefWidth(175);
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));

        TableColumn<AudioMetaData, String> artistColumn = new TableColumn<>("艺术家");
        artistColumn.setPrefWidth(150);
        artistColumn.setCellValueFactory(new PropertyValueFactory<>("artist"));

        TableColumn<AudioMetaData, String> albumColumn = new TableColumn<>("专辑");
        albumColumn.setPrefWidth(175);
        albumColumn.setCellValueFactory(new PropertyValueFactory<>("album"));

        TableColumn<AudioMetaData, String> dateColumn = new TableColumn<>("出版日期");
        dateColumn.setPrefWidth(100);
        dateColumn.setStyle("-fx-alignment: CENTER-RIGHT");
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));

        TableColumn<AudioMetaData, String> genreColumn = new TableColumn<>("流派");
        genreColumn.setPrefWidth(85);
        genreColumn.setCellValueFactory(new PropertyValueFactory<>("genre"));

        TableColumn<AudioMetaData, String> trackColumn = new TableColumn<>("音轨序号");
        trackColumn.setPrefWidth(55);
        trackColumn.setCellValueFactory(new PropertyValueFactory<>("track"));

        TableColumn<AudioMetaData, String> commentColumn = new TableColumn<>("备注");
        commentColumn.setPrefWidth(200);
        commentColumn.setCellValueFactory(new PropertyValueFactory<>("comment"));

        TableColumn<AudioMetaData, String> bitrateColumn = new TableColumn<>("比特率");
        bitrateColumn.setPrefWidth(75);
        bitrateColumn.setStyle("-fx-alignment: CENTER-RIGHT");
        bitrateColumn.setCellValueFactory(new PropertyValueFactory<>("bitrate"));

        TableColumn<AudioMetaData, String> lengthColumn = new TableColumn<>("时长");
        lengthColumn.setPrefWidth(65);
        lengthColumn.setStyle("-fx-alignment: CENTER-RIGHT");
        lengthColumn.setCellValueFactory(new PropertyValueFactory<>("length"));

        TABLE_VIEW.getColumns().add(filenameColumn);
        TABLE_VIEW.getColumns().add(titleColumn);
        TABLE_VIEW.getColumns().add(artistColumn);
        TABLE_VIEW.getColumns().add(albumColumn);
        TABLE_VIEW.getColumns().add(dateColumn);
        TABLE_VIEW.getColumns().add(genreColumn);
        TABLE_VIEW.getColumns().add(trackColumn);
        TABLE_VIEW.getColumns().add(commentColumn);
        TABLE_VIEW.getColumns().add(bitrateColumn);
        TABLE_VIEW.getColumns().add(lengthColumn);
    }

    private static void configContextMenu() {
        RENAME_MENU_ITEM.setOnAction(event -> Rename.show(TABLE_VIEW.getSelectionModel().getSelectedItem()));

        MenuItem selectAll = new MenuItem("全选");
        selectAll.setOnAction(event -> TABLE_VIEW.getSelectionModel().selectAll());

        MenuItem renameBaseOnTags = new MenuItem("根据标签重命名");
        MenuItem addTagsBaseOnFilename = new MenuItem("基于文件名添加标签");

        MenuItem deleteFromTable = new MenuItem("从表格中移除");
        deleteFromTable.setOnAction(event -> {
            List<AudioMetaData> selectedItems = TABLE_VIEW.getSelectionModel().getSelectedItems();
            TABLE_VIEW.getItems().removeAll(selectedItems);
            updateTableView(null);
        });

        MenuItem deleteFile = new MenuItem("删除选中的文件");
        deleteFile.setOnAction(event -> {
            List<AudioMetaData> succeed = Delete.show(TABLE_VIEW.getSelectionModel().getSelectedItems());
            TABLE_VIEW.getItems().removeAll(succeed);
            updateTableView(null);
        });

        MenuItem cancel = new MenuItem("取消");
        cancel.setOnAction(event -> TABLE_VIEW.getSelectionModel().clearSelection());

        CONTEXT_MENU.getItems().add(selectAll);
        CONTEXT_MENU.getItems().add(RENAME_MENU_ITEM);
        CONTEXT_MENU.getItems().add(renameBaseOnTags);
        CONTEXT_MENU.getItems().add(addTagsBaseOnFilename);
        CONTEXT_MENU.getItems().add(deleteFromTable);
        CONTEXT_MENU.getItems().add(deleteFile);
        CONTEXT_MENU.getItems().add(cancel);
    }

    // 用于记录右键菜单是否被打开，以便在合适的时机将其关闭
    private static boolean contextMenuOpened = false;
    // 在 javafx 中一个完整的拖动事件也是单击事件
    // 鼠标是否进行过拖动：用来确认是到底是拖动还是单击
    private static boolean mouseDragged = false;
    // 起始行号：鼠标按下时的行号
    private static Integer indexWhenDragStart = null;
    // 鼠标拖动结束时最后被选中的行的行号
    private static Integer indexOfLastSelectedRow = null;
    public static void configActionHandleAfterTableShowed() {
        TABLE_VIEW.setOnMouseClicked(event -> {
            // 只处理单击事件
            if (event.getClickCount() == 1) {
                if (event.getButton().equals(MouseButton.PRIMARY)) {
                    handlerPrimaryButtonClick(event);
                }

                if (event.getButton().equals(MouseButton.SECONDARY)) {
                    handlerSecondaryButtonClick(event);
                }
            }
        });

        // 鼠标左键按下/拖动开始的时候，记录其位置对应的行号
        TABLE_VIEW.setOnMousePressed(event -> {
            // 如果是在视口以外的地方按下该语句不会执行， 所以 indexWhenDragStart 不会等于 -1
            // 如果该值为 0 ，则表示鼠标按下的时候没有选中某一行（鼠标从空白处按下）
            if (event.getButton().equals(MouseButton.PRIMARY)) {
                indexWhenDragStart = getItemIndex(event.getY());
            }
        });

        TABLE_VIEW.setOnMouseDragged(Center::handlerMouseDragged);
    }

    // 实现框选功能
    private static void handlerMouseDragged(MouseEvent event) {
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
            } else if (event.getY() <= tableHeadRowHeight) {
                // tableHeadRowHeight等同于视口顶部的 y 坐标
                // 鼠标拖动到位于视口上方时，自动向上滚动
                verticalScrollBar.decrement();
                // 获取行号时将鼠标的位置调整为视口顶部偏下一点点
                indexWhenDragged = getItemIndex(tableHeadRowHeight + 0.1);
            } else {
                // 出现了滚动条，但鼠标没有拖动到视口外面，直接获取鼠标位置对应的行号
                indexWhenDragged = getItemIndex(event.getY());
            }
            // 当有滚动条时，经过上面的调整，框选动作总是从某一行开始到某一行结束
            int start = Math.min(indexWhenDragStart, indexWhenDragged);
            int end = Math.max(indexWhenDragStart, indexWhenDragged);
            selectIndices(start, end);
            indexOfLastSelectedRow = indexWhenDragged;
        } else {
            // 实际内容高度没有超过视口的高度，直接获取鼠标位置对应的行号
            indexWhenDragged = getItemIndex(event.getY());

            // 从某一行开始
            if (indexWhenDragStart > 0) {
                // 到某一行结束
                if (indexWhenDragged > 0) {
                    int start = Math.min(indexWhenDragStart, indexWhenDragged);
                    int end = Math.max(indexWhenDragStart, indexWhenDragged);
                    selectIndices(start, end);
                    indexOfLastSelectedRow = indexWhenDragged;
                }
                // 到视口空白处或视口下方结束
                if (indexWhenDragged == IN_VIEWPORT_BLANK || indexWhenDragged == BLOW_VIEWPORT) {
                    int start = indexWhenDragStart;
                    int end = TABLE_VIEW.getItems().size();
                    selectIndices(start, end);
                    indexOfLastSelectedRow = end;
                }
                // 到视口上方结束
                if (indexWhenDragged == ABOVE_VIEWPORT) {
                    int start = 1;
                    int end = indexWhenDragStart;
                    selectIndices(start, end);
                    indexOfLastSelectedRow = 1;
                }
            }

            // 从视口空白处开始
            if (indexWhenDragStart == IN_VIEWPORT_BLANK) {
                // 到某一行结束
                if (indexWhenDragged > 0) {
                    int end = TABLE_VIEW.getItems().size();
                    selectIndices(indexWhenDragged, end);
                    indexOfLastSelectedRow = indexWhenDragged;
                }
                // 到视口空白处或视口下方结束
                if (indexWhenDragged == IN_VIEWPORT_BLANK || indexWhenDragged == BLOW_VIEWPORT) {
                    TABLE_VIEW.getSelectionModel().clearSelection();
                    indexOfLastSelectedRow = null;
                }
                // 到视口上方结束
                if (indexWhenDragged == ABOVE_VIEWPORT) {
                    TABLE_VIEW.getSelectionModel().selectAll();
                    indexOfLastSelectedRow = 1;
                }
            }
        }

        if (indexOfLastSelectedRow != null) {
            Aside.showMetaData(TABLE_VIEW.getItems().get(indexOfLastSelectedRow - 1));
        }
        indexOfLastSelectedRow = null;
    }

    // 选中指定范围内的行
    private static void selectIndices(int start, int end) {
        TABLE_VIEW.getSelectionModel().clearSelection();
        for (int i = start; i <= end; i++) {
            TABLE_VIEW.getSelectionModel().selectIndices(i - 1);
        }
    }

    private static void handlerSecondaryButtonClick(MouseEvent event) {
        int itemIndex = getItemIndex(event.getY());
        if (itemIndex > 0) { // 非空白处右键单击
            // 在侧边栏中显示该行数据
            Aside.showMetaData(TABLE_VIEW.getItems().get(itemIndex - 1));

            List<AudioMetaData> selectedItems = TABLE_VIEW.getSelectionModel().getSelectedItems();
            RENAME_MENU_ITEM.setDisable(selectedItems.size() != 1); // 重命名菜单项只在选中一个时可用
            // 显示右键菜单
            CONTEXT_MENU.show(TABLE_VIEW, event.getScreenX(), event.getScreenY());
            contextMenuOpened = true;
        } else { // 空白处右键单击
            // 侧边栏显示空白
            Aside.showBlank();
            // 关闭右键菜单
            CONTEXT_MENU.hide();
            contextMenuOpened = false;
        }
    }

    private static void handlerPrimaryButtonClick(MouseEvent event) {
        // 左键单击时关闭右键菜单
        if (contextMenuOpened) {
            CONTEXT_MENU.hide();
            contextMenuOpened = false;
        }
        // 如果是拖动行为，则不执行下面的左键单击逻辑
        if (mouseDragged) {
            mouseDragged = false;
            return;
        }

        int itemIndex = getItemIndex(event.getY());
        if (itemIndex > 0) {
            Aside.showMetaData(TABLE_VIEW.getItems().get(itemIndex - 1));
        } else {
            TABLE_VIEW.getSelectionModel().clearSelection();
            Aside.showBlank();
        }
        // 左键单击或拖选结束，重置拖动起点
        indexWhenDragStart = null;
    }

    // 根据鼠标位置计算出该位置对应的实际行号
    // 如果有对应的行，返回的行号大于0（行号从1开始）
    // 否则返回 ABOVE_VIEWPORT（-1） 或 BLOW_VIEWPORT（-2） 或 IN_VIEWPORT_BLANK（0）
    // currentY：鼠标位置的 y 坐标（通过 event.getY() 得到的值）
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

        // 内容的实际高度（以内容的顶部为原点，越向下，高度值递增，行号递增）
        double contentHeight = ROW_HEIGHT * TABLE_VIEW.getItems().size();
        if (verticalScrollBar.isVisible()) { // 垂直滚动条可见时行高的计算方式
            // 除视口外的高度 = 表头的高度 + 水平滚动条的高度
            // 视口的高度 = 表格的高度 - 除视口外的高度
            double viewportHeight = TABLE_VIEW.getHeight() - heightOutsideContentSection;
            // 最大偏移量 = 内容的实际高度 - 视口的高度
            double maxOffset = contentHeight - viewportHeight;
            // 偏移量 = 最大偏移量 * 滚动条的偏移比例
            double proportion = verticalScrollBar.getValue() / verticalScrollBar.getMax();
            double offset = maxOffset * proportion;
            // total: 鼠标相对于内容的实际高度
            double total = offset + (currentY - tableHeadRowHeight);
            // 行号 = 鼠标相对于内容的实际高度 / 行高 （向上取整）
            return (int)Math.ceil(total / ROW_HEIGHT);
        } else {
            // 垂直滚动条不可见时行高的计算方式
            double validY = contentHeight + tableHeadRowHeight;
            if (currentY > validY) {
                return IN_VIEWPORT_BLANK;
            } else {
                return (int)Math.ceil((currentY - tableHeadRowHeight) / ROW_HEIGHT);
            }
        }
    }

    private static void initContent() {
        List<String> paths = Session.getCurrentTableViewContentPaths();
        List<AudioMetaData> audioMetaData = new ArrayList<>(paths.size());
        paths.forEach(path -> audioMetaData.add(MetaDataReader.readFile(new File(path))));
        updateTableView(audioMetaData);
    }

    public static void updateTableView(List<AudioMetaData> dataList) {
        // 如果 dataList 为 null，则刷新当前表格
        if (dataList == null) {
            recordeUpdate(TABLE_VIEW.getItems());
            TABLE_VIEW.refresh();
        } else {
            Aside.showBlank();
            recordeUpdate(dataList);
            TABLE_VIEW.getItems().clear();
            TABLE_VIEW.getItems().addAll(dataList);
            TABLE_VIEW.refresh();
        }
    }

    private static void recordeUpdate(List<AudioMetaData> dataList) {
        ALTERNATIVE_ARTISTS.clear();
        ALTERNATIVE_ALBUMS.clear();

        List<String> paths = new ArrayList<>(dataList.size());

        for (AudioMetaData metaData : dataList) {
            String artist = metaData.getArtist();
            if (artist != null) {
                artist = artist.trim();
                if (!"".equals(artist)) {
                    ALTERNATIVE_ARTISTS.add(artist);
                }
            }

            String album = metaData.getAlbum();
            if (album != null) {
                album = album.trim();
                if (!"".equals(album)) {
                    ALTERNATIVE_ALBUMS.add(album);
                }
            }

            String genre = metaData.getGenre();
            if (genre != null) {
                genre = genre.trim();
                if (!"".equals(genre) && !Session.getAlternativeGenres().contains(genre)) {
                    Session.addGenre(genre);
                }
            }

            paths.add(metaData.getAbsolutePath());
        }

        Session.setCurrentTableViewContentPaths(paths);
    }

    public static List<String> getAlternativeArtists() {
        return new ArrayList<>(ALTERNATIVE_ARTISTS);
    }

    public static List<String> getAlternativeAlbums() {
        return new ArrayList<>(ALTERNATIVE_ALBUMS);
    }

    public static Node getCenter() {
        return TABLE_VIEW;
    }

    public static void selectItem(AudioMetaData audioMetaData) {
        int index = TABLE_VIEW.getItems().indexOf(audioMetaData);
        TABLE_VIEW.getSelectionModel().select(index);
    }

    public static void confirmHeadRowHeight() {
        // 表头的高度需要 skin 已经渲染完毕时才能获取
        TableViewSkin<?> skin = (TableViewSkin<?>) TABLE_VIEW.getSkin();
        ObservableList<Node> childrenList = skin.getChildren();
        for (Node node : childrenList) {
            if (node instanceof TableHeaderRow headerRow) {
                tableHeadRowHeight = headerRow.getHeight();
            }
        }

        // 完成其余组件的配置
        getAndConfigScrollBar();
        configActionHandleAfterTableShowed();
    }

    private static void getAndConfigScrollBar() {
        Set<Node> nodes = TABLE_VIEW.lookupAll(".scroll-bar");
        for (Node node : nodes) {
            if (node instanceof ScrollBar scrollBar) {
                if (scrollBar.getOrientation().equals(Orientation.VERTICAL)) {
                    verticalScrollBar = scrollBar;
                }

                if (scrollBar.getOrientation().equals(Orientation.HORIZONTAL)) {
                    horizontalScrollBarHeight = scrollBar.getHeight();
                    heightOutsideContentSection = horizontalScrollBarHeight + tableHeadRowHeight;
                }
            }
        }
    }
}
