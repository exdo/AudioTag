package xyz.idaoteng.audiotag.util.tableutil;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.Skin;
import javafx.scene.control.TableView;
import javafx.scene.control.skin.TableHeaderRow;
import javafx.scene.control.skin.TableViewSkin;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 用于封装单个 TableView 实例的拖拽选择逻辑和状态。
 * 避免静态变量在多个 TableView 实例之间共享，导致行为异常。
 *
 * @param <T> TableView 中存储的数据类型。
 */
public class EnableDragSelection<T> {
    private final TableView<T> tableView;
    private final DragSelectCallback callback;
    private final DragSelectSwitch dragSelectSwitch;

    // 行位置常量
    private static final int ABOVE_VIEWPORT = -1; // 视口上方
    private static final int BLOW_VIEWPORT = -2; // 视口下方
    private static final int IN_VIEWPORT_BLANK = -3; // 视口内空白处

    // 默认值和偏移量常量
    private static final double SCROLL_EDGE_OFFSET = 0.1; // 鼠标拖动到视口边缘时，用于计算行号的偏移量
    private static final long SCROLL_RATE_MS = 70; // 自动滚动的时间间隔（毫秒）

    // 鼠标是否进行过拖动：用来确认是到底是拖动还是单击
    private boolean mouseDragged = false;

    // 起始行号：鼠标按下时的行号。只有当鼠标按下在有效行上时才会被设置。
    private Integer indexWhenDragStart = null;

    // 滚动条相关
    private ScrollBar vScrollBar = null; // 竖直滚动条
    private ScrollBar hScrollBar = null; // 水平滚动条
    private double hScrollBarHeight = 0; // 水平滚动条高度
    private boolean moveScrollBarUp; // 竖直滚动条是否向上滚动
    private ScheduledExecutorService executorService; // 竖直滚动条自动滚动线程

    private double tableHeadRowHeight = 0; // 表头高度
    private final double rowHeight; // 行高

    public EnableDragSelection(
            TableView<T> tableView,
            DragSelectCallback callback,
            DragSelectSwitch dragSelectSwitch) {
        this.tableView = tableView;
        this.callback = callback;
        this.dragSelectSwitch = dragSelectSwitch;

        // 监听 TableView 的 skin 属性，确保在皮肤可用时初始化内部组件
        ChangeListener<Skin<?>> skinChangeListener = (obs, oldSkin, newSkin) -> {
            if (newSkin != null) {
                Platform.runLater(this::performInitialPropertyLookup);
            }
        };
        tableView.skinProperty().addListener(skinChangeListener);

        // 如果 TableView 已经有皮肤，则立即尝试初始化
        if (tableView.getSkin() != null) {
            Platform.runLater(this::performInitialPropertyLookup);
        }

        rowHeight = tableView.getFixedCellSize();

        applyEventHandlers();
    }

    /**
     * 初始化 TableView 的内部属性：表头高度、滚动条
     * 必须在 TableView 渲染完成后调用。
     */
    private void performInitialPropertyLookup() {
        // 避免重复初始化，如果已经成功获取到关键属性则不再执行
        if (tableHeadRowHeight > 0 && vScrollBar != null && rowHeight > 0 && hScrollBar != null) {
            return;
        }

        TableViewSkin<?> skin = (TableViewSkin<?>) tableView.getSkin();
        if (skin != null) {
            for (Node node : skin.getChildren()) {
                if (node instanceof TableHeaderRow headerRow) {
                    tableHeadRowHeight = headerRow.getHeight();
                    break;
                }
            }
        }

        Set<Node> nodes = tableView.lookupAll(".scroll-bar");
        for (Node node : nodes) {
            if (node instanceof ScrollBar scrollBar) {
                if (scrollBar.getOrientation().equals(Orientation.VERTICAL)) {
                    vScrollBar = scrollBar;
                }
                if (scrollBar.getOrientation().equals(Orientation.HORIZONTAL)) {
                    hScrollBar = scrollBar; // 赋值给成员变量
                    hScrollBarHeight = scrollBar.getHeight();
                    // 监听横向滚动条的高度变化
                    ChangeListener<Number> listener = (ob, o, n) -> hScrollBarHeight = n.doubleValue();
                    hScrollBar.heightProperty().addListener(listener);
                }
            }
        }
    }

    /**
     * 应用鼠标事件处理器到 TableView。
     */
    private void applyEventHandlers() {
        // 鼠标左键按下/拖动开始的时候，记录其位置对应的行号
        tableView.setOnMousePressed(event -> {
            if (event.getButton().equals(MouseButton.PRIMARY)) {
                int pressedIndex = getItemIndex(event.getY());
                indexWhenDragStart = dragSelectSwitch.isEnable() ? pressedIndex : null;
                mouseDragged = false; // 在每次按下时重置拖动状态
            }
            // 调用相应的自定义逻辑
            callback.onMousePressed(event);
        });

        tableView.setOnMouseDragged(event -> {
            if (dragSelectSwitch.isEnable()) {
                handleMouseDragged(event);
            }
            mouseDragged = true; // 标记为拖动行为
            // 调用相应的自定义逻辑
            callback.onMouseDragged(event);
        });

        tableView.setOnMouseReleased(event -> {
            stopAutoScrolling();
            // 拖动结束后，重置起始行号，避免下次单击被误判为拖动
            indexWhenDragStart = null;
            // 调用相应的自定义逻辑
            callback.onMouseReleased(event);
        });

        // 处理点击事件，调用相应的自定义逻辑的时机更为复杂
        tableView.setOnMouseClicked(this::handleMouseClicked);
    }

    /**
     * 根据鼠标位置计算出该位置对应的实际行号。
     * 如果有对应的行，返回的行号大于-1
     * 否则返回 ABOVE_VIEWPORT（-1：在视口上方）
     * 或 BLOW_VIEWPORT（-2：在视口下方）
     * 或 IN_VIEWPORT_BLANK（-3：在视口内的空白处）
     *
     * @param currentY 鼠标位置的 y 坐标（通过 event.getY() 得到的值）
     * @return 对应的行号或常量
     */
    public int getItemIndex(double currentY) {
        // 当鼠标位于视口上方，返回 ABOVE_VIEWPORT
        if (currentY <= tableHeadRowHeight) {
            return ABOVE_VIEWPORT;
        }
        // 视口底部的 y 坐标
        double upperLimitY = tableView.getHeight() - hScrollBarHeight;
        // 当鼠标位于视口下方，返回 BLOW_VIEWPORT
        if (currentY >= upperLimitY) {
            return BLOW_VIEWPORT;
        }

        // 完整内容的实际高度（以内容的顶部为原点，越向下，高度值递增，行号递增）
        double contentHeight = rowHeight * tableView.getItems().size();

        // 检查 verticalScrollBar 是否已初始化且可见
        if (vScrollBar != null && vScrollBar.isVisible()) {
            // 垂直滚动条可见时行高的计算方式
            // 除视口外的高度 = 表头的高度 + 水平滚动条的高度
            // 视口的高度 = 表格的高度 - 除视口外的高度
            double heightExceptViewport = tableHeadRowHeight + hScrollBarHeight;
            double viewportHeight = tableView.getHeight() - heightExceptViewport;
            // 最大偏移量 = 内容的实际高度 - 视口的高度
            double maxOffset = contentHeight - viewportHeight;
            // 偏移量 = 最大偏移量 * 滚动条的偏移比例
            double proportion = vScrollBar.getValue() / vScrollBar.getMax();
            double offset = maxOffset * proportion;
            // total: 鼠标相对于内容的实际高度
            double total = offset + (currentY - tableHeadRowHeight);
            // 行号 = 鼠标相对于内容的实际高度 / 行高 （向上取整）
            int index = (int) Math.ceil(total / rowHeight);
            // 因为 TableView 使用0索引，实际行号需要再 - 1
            return index - 1;
        } else {
            // 垂直滚动条不可见时行高的计算方式
            // 如果鼠标 Y 坐标超出了实际内容 + 表头的高度，则认为是空白处
            double validY = contentHeight + tableHeadRowHeight;
            if (currentY > validY) {
                return IN_VIEWPORT_BLANK;
            } else {
                int index = (int) Math.ceil((currentY - tableHeadRowHeight) / rowHeight);
                // 因为 TableView 使用0索引，实际行号需要再 - 1
                return index - 1;
            }
        }
    }

    /**
     * 处理鼠标左键单击事件。
     */
    private void handleMouseClicked(MouseEvent event) {
        // 在 javafx 中一个完整的拖动事件也可能被识别为点击事件
        // 如果此前有拖动行为，则不再执行后续逻辑
        if (mouseDragged) {
            // 此时框选已结束，故应改为 false
            mouseDragged = false;
            return;
        }

        int itemIndex = getItemIndex(event.getY());
        // 处理单击事件（继承 TableView 的默认实现：单击时选中该行，如果有的话）
        if (event.getClickCount() == 1) {
            if (itemIndex >= 0) {
                tableView.getSelectionModel().select(itemIndex);
            } else {
                // 如果点击在空白处，通常会清除选择
                tableView.getSelectionModel().clearSelection();
            }
        }
        callback.onMouseClicked(event, itemIndex >= 0 ? itemIndex : null);
    }

    /**
     * 选中指定范围内的行
     */
    private void selectIndices(int num1, int num2) {
        int min = Math.min(num1, num2);
        int max = Math.max(num1, num2);
        tableView.getSelectionModel().clearSelection();
        for (int i = min; i <= max; i++) {
            tableView.getSelectionModel().selectIndices(i);
        }
    }

    /**
     * 处理鼠标拖拽事件。
     */
    private void handleMouseDragged(MouseEvent event) {
        // 右键拖动不处理
        if (event.getButton().equals(MouseButton.SECONDARY)) {
            return;
        }

        // 开始拖动时在非视口内则只会清除之前的选择，而不会执行拖拽逻辑
        if (indexWhenDragStart == null || indexWhenDragStart < 0 || tableView.getItems().isEmpty()) {
            tableView.getSelectionModel().clearSelection();
            return;
        }

        if (vScrollBar != null && vScrollBar.isVisible()) {
            handleDragWithScrollbar(event);
        } else {
            handleDragWithoutScrollbar(event);
        }
    }

    /**
     * 处理有垂直滚动条时的拖拽逻辑。
     */
    private void handleDragWithScrollbar(MouseEvent event) {
        double upperLimitY = tableView.getHeight() - hScrollBarHeight;

        if (event.getY() >= upperLimitY) {
            // 鼠标拖动到位于视口下方时，自动向下滚动
            vScrollBar.increment();
            moveScrollBarUp = false;
            startAutoScrolling();
        } else if (event.getY() <= tableHeadRowHeight) {
            // 鼠标拖动到位于视口上方时，自动向上滚动
            vScrollBar.decrement();
            moveScrollBarUp = true;
            startAutoScrolling();
        } else {
            // 鼠标在视口内，停止自动滚动
            stopAutoScrolling();
            // 鼠标拖动到某位置时对应的行号
            int currentIndexWhileDragging = getItemIndex(event.getY());
            // 在有滚动条时，鼠标应在有效行区域
            if (currentIndexWhileDragging < 0) {
                throw new RuntimeException("未能按预期结果运行");
            }
            selectIndices(indexWhenDragStart, currentIndexWhileDragging);
            callback.onRowSelectedDuringDrag(currentIndexWhileDragging);
        }
    }

    /**
     * 处理没有垂直滚动条时的拖拽逻辑。
     * 此时视口通常显示所有内容，或内容不足以填满视口。
     */
    private void handleDragWithoutScrollbar(MouseEvent event) {
        // 确保表格不为空
        if (tableView.getItems().isEmpty()) {
            tableView.getSelectionModel().clearSelection();
            return;
        }

        int currentIndexWhileDragging = getItemIndex(event.getY());
        int effectiveDraggedIndex;

        // 将特殊行号映射到实际的可见行索引
        if (currentIndexWhileDragging >= 0) {
            effectiveDraggedIndex = currentIndexWhileDragging;
        } else if (currentIndexWhileDragging == ABOVE_VIEWPORT) {
            // 拖动到视口上方，视为选中第一行
            effectiveDraggedIndex = 0;
        } else { // BLOW_VIEWPORT 或 IN_VIEWPORT_BLANK
            // 拖动到视口下方或空白处，视为选中最后一行
            effectiveDraggedIndex = tableView.getItems().size() - 1;
        }

        selectIndices(indexWhenDragStart, effectiveDraggedIndex);
        callback.onRowSelectedDuringDrag(effectiveDraggedIndex);
    }

    private void autoScrollingTask() {
        Platform.runLater(() -> {
            // 在使用 indexWhenDragStart 之前进行 null 检查
            if (indexWhenDragStart == null) {
                // 如果 indexWhenDragStart 已经为 null，说明拖动已经结束或被取消，
                // 此时不应再执行选择逻辑
                stopAutoScrolling(); // 确保自动滚动彻底停止
                return;
            }

            int currentIndexWhileDragging;
            if (moveScrollBarUp) {
                vScrollBar.decrement();
                currentIndexWhileDragging = getItemIndex(tableHeadRowHeight + SCROLL_EDGE_OFFSET);
            } else {
                vScrollBar.increment();
                double upperLimitY = tableView.getHeight() - hScrollBarHeight;
                currentIndexWhileDragging = getItemIndex(upperLimitY - SCROLL_EDGE_OFFSET);
            }

            // 当有滚动条时，经过 +\- SCROLL_EDGE_OFFSET 的调整，
            // 框选动作总是从某一行开始到某一行结束，
            // 所以 currentIndexWhileDragging 应总是 >= 0
            if (currentIndexWhileDragging < 0) {
                throw new RuntimeException("未能按预期结果运行");
            }

            selectIndices(indexWhenDragStart, currentIndexWhileDragging);
            callback.onRowSelectedDuringDrag(currentIndexWhileDragging);
        });
    }

    /**
     * 启动竖直滚动条自动滚动线程。
     */
    private void startAutoScrolling() {
        // 定时任务存在时不再创建新的
        if (executorService != null && !executorService.isShutdown()) {
            return;
        }

        executorService = Executors.newSingleThreadScheduledExecutor();
        Runnable task = this::autoScrollingTask;
        executorService.scheduleAtFixedRate(task, 0, SCROLL_RATE_MS, TimeUnit.MILLISECONDS);
    }


    /**
     * 强制终止滚动条自动滚动线程
     */
    private void stopAutoScrolling() {
        if (executorService != null) {
            executorService.shutdownNow();
            executorService = null;
        }
    }
}
