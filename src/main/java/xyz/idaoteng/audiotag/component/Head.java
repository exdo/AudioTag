package xyz.idaoteng.audiotag.component;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

import xyz.idaoteng.audiotag.Session;
import xyz.idaoteng.audiotag.StartUp;
import xyz.idaoteng.audiotag.bean.AudioMetaData;
import xyz.idaoteng.audiotag.core.MetaDataReader;
import xyz.idaoteng.audiotag.core.SupportedFileTypes;
import xyz.idaoteng.audiotag.dialog.Filter;
import xyz.idaoteng.audiotag.exception.CantReadException;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Head类代表应用程序的顶部工具栏控件
 * 包含各种操作按钮和功能菜单
 */
public class Head {
    // 静态工具栏实例
    private static final ToolBar HEAD = new ToolBar();

    // 菜单项定义
    private static final MenuItem SELECT_FILE = new MenuItem("选择文件");
    private static final MenuItem SELECT_FOLDER_WITH_CHILD = new MenuItem("选择文件夹<含子文件夹>");
    public static final MenuItem SELECT_FOLDER_WITHOUT_CHILD = new MenuItem("选择文件夹<不含子文件夹>");

    // 刷新按钮
    private static final Button REFRESH_BUTTON = new Button("刷新");

    // 文件扩展名过滤器（仅显示支持的音频文件类型）
    private static final ExtensionFilter FILTER = new ExtensionFilter("audio file", SupportedFileTypes.getTypes());

    // 静态初始化块 - 在类加载时执行
    static {
        try {
            // 确保Center类已加载
            Class.forName("xyz.idaoteng.audiotag.component.Center");
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        // 创建下拉菜单按钮，包含文件/文件夹选择选项
        MenuButton menuButton  = new MenuButton("文件");
        menuButton.getItems().addAll(
                SELECT_FILE,
                new SeparatorMenuItem(),
                SELECT_FOLDER_WITH_CHILD,
                new SeparatorMenuItem(),
                SELECT_FOLDER_WITHOUT_CHILD
        );
        // 配置菜单项的事件处理
        setupMenuItems();

        // 配置刷新按钮的事件处理
        setupRefreshButton();

        // 全选按钮 - 选择表格中所有项
        Button selectAll = new Button("全选");
        selectAll.setOnAction(event -> Center.selectAll());

        // 过滤按钮 - 由Filter类接管处理
        Button filter = new Button("过滤");
        Filter.takeOverFilterButton(filter);

        // 根据标签重命名按钮
        Button renameBaseOnTag = new Button("根据标签重命名");
        renameBaseOnTag.setOnAction(event -> Center.renameBaseOnTags());

        // 基于文件名添加标签按钮
        Button addTag = new Button("基于文件名添加标签");
        addTag.setOnAction(event -> Center.addTagBaseOnFilename());

        // 删除文件按钮
        Button delete = new Button("删除文件");
        delete.setOnAction(event -> Center.deleteSelectedItems());

        // 重命名按钮 - 由Center类接管处理
        Button rename = new Button("重命名");
        Center.takeOverRenameButton(rename);

        // 其他功能菜单
        MenuButton other = new MenuButton("其他");
        MenuItem remove = new MenuItem("从表格中移除");
        remove.setOnAction(event -> Center.removeSelectedItems());

        MenuItem addOrder = new MenuItem("从上至下依次添加序号");
        addOrder.setOnAction(event -> Center.addOrder());

        // 生成删除特定标签的菜单
        Menu deleteSpecialTag = Center.generateDeleteSpecificTagMenu();
        Center.configDeleteSpecificTagMenu(deleteSpecialTag);

        // 设置成同一专辑菜单项
        MenuItem packageToAlbum = new MenuItem("设置成同一专辑");
        packageToAlbum.setOnAction(event -> Center.packageToAlbum());

        // 添加各项到"其他"菜单
        other.getItems().addAll(addOrder, deleteSpecialTag, packageToAlbum,
                Center.generateSameAlbumOptionMenu(), Center.generateTidyMenu());

        // 允许拖拽行的单选按钮
        RadioButton enableDragRow = new RadioButton("允许拖拽行");
        enableDragRow.setPadding(new Insets(4, 0, 0, 0));
        Center.takeOverEnableDragRow(enableDragRow);

        // 将所有组件添加到工具栏
        HEAD.getItems().addAll(menuButton, REFRESH_BUTTON, selectAll, filter,
                renameBaseOnTag, addTag, delete, rename, other, enableDragRow);
    }

    /**
     * 配置菜单项的事件处理器
     */
    private static void setupMenuItems() {
        // "选择文件"菜单项的事件处理
        SELECT_FILE.setOnAction(event -> {
            // 创建文件选择对话框
            FileChooser fileChooser = new FileChooser();
            // 设置初始目录为上次选择的文件夹
            fileChooser.setInitialDirectory(new File(Session.getFolderPathOfTheLastSelectedFile()));
            // 添加文件过滤器
            fileChooser.getExtensionFilters().addAll(FILTER);
            fileChooser.setTitle("选择音频文件（多选）");

            // 显示对话框并获取选择的文件列表
            List<File> files = fileChooser.showOpenMultipleDialog(StartUp.getPrimaryStage());
            if (files != null && !files.isEmpty()) {
                // 读取每个文件的元数据
                List<AudioMetaData> dataList = new ArrayList<>(files.size());
                files.forEach(file -> {
                    try {
                        dataList.add(MetaDataReader.readFile(file));
                    } catch (CantReadException ignored) {}
                });

                // 更新表格视图
                Filter.setFilterOn();
                Center.updateTableView(dataList);

                // 更新会话中最后选择的文件路径
                String folderPath = files.get(0).getParentFile().getAbsolutePath();
                Session.setFolderPathOfTheLastSelectedFile(folderPath);
            }
        });

        // "选择文件夹(含子文件夹)"菜单项的事件处理
        SELECT_FOLDER_WITH_CHILD.setOnAction(event -> {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setInitialDirectory(new File(Session.getPathToTheLastSelectedFolder()));
            directoryChooser.setTitle("选择文件夹（含子文件夹）");
            File dir = directoryChooser.showDialog(StartUp.getPrimaryStage());
            if (dir != null) {
                // 读取文件夹及其子文件夹中的音频文件元数据
                List<AudioMetaData> audioMetaData = MetaDataReader.readDirectory(dir, true);
                Filter.setFilterOn();
                Center.updateTableView(audioMetaData);
                Session.setPathToTheLastSelectedFolder(dir.getAbsolutePath());
            }
        });

        // "选择文件夹(不含子文件夹)"菜单项的事件处理
        SELECT_FOLDER_WITHOUT_CHILD.setOnAction(event -> {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setInitialDirectory(new File(Session.getPathToTheLastSelectedFolder()));
            directoryChooser.setTitle("选择文件夹（不含子文件夹）");
            File dir = directoryChooser.showDialog(StartUp.getPrimaryStage());
            if (dir != null) {
                // 仅读取当前文件夹中的音频文件元数据(不含子文件夹)
                List<AudioMetaData> audioMetaData = MetaDataReader.readDirectory(dir, false);
                Filter.setFilterOn();
                Center.updateTableView(audioMetaData);
                Session.setPathToTheLastSelectedFolder(dir.getAbsolutePath());
            }
        });
    }

    /**
     * 配置刷新按钮的事件处理器
     */
    private static void setupRefreshButton() {
        REFRESH_BUTTON.setOnAction(event -> {
            // 获取当前表格视图中的文件路径
            List<String> current = Session.getCurrentTableViewContentPaths();
            List<AudioMetaData> refreshed = new ArrayList<>(current.size());

            // 使用迭代器遍历，以便在文件不存在的删除项
            Iterator<String> iterator = current.iterator();
            while (iterator.hasNext()) {
                String path = iterator.next();
                File file = new File(path);
                if (file.exists()) {
                    try {
                        // 重新读取文件的元数据
                        refreshed.add(MetaDataReader.readFile(file));
                    } catch (CantReadException ignored) {}
                } else {
                    // 文件不存在则从列表中移除
                    iterator.remove();
                }
            }

            // 清空侧边栏的元数据显示
            Aside.showMetaData(new AudioMetaData());
            // 更新表格视图
            Center.updateTableView(refreshed);
        });
    }

    /**
     * 获取工具栏实例
     * @return 工具栏Node对象
     */
    public static Node getHead() {
        return HEAD;
    }
}
