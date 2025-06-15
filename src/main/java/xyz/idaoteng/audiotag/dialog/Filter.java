package xyz.idaoteng.audiotag.dialog;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import xyz.idaoteng.audiotag.bean.AudioMetaData;
import xyz.idaoteng.audiotag.component.Center;
import xyz.idaoteng.audiotag.constant.MusicGenre;

import java.util.ArrayList;
import java.util.List;

/**
 *  Filter 类用于创建一个过滤音频元数据的对话框。
 *  允许用户根据标题、艺术家、专辑、流派和封面等条件过滤音频文件。
 */
public class Filter {
    // 定义静态变量，保证所有实例共享同一个窗口和数据
    private static final Stage STAGE = new Stage(); // 过滤窗口
    private static final VBox BODY = new VBox(10); // 主体垂直布局，组件间距为10
    private static final CheckBox TITLE_CHECK_BOX = new CheckBox("标题："); // 标题过滤复选框
    private static final TextField TITLE_TEXT_FIELD = new TextField(); // 标题过滤文本框
    private static final ComboBox<String> ARTIST_COMBO_BOX = new ComboBox<>(); // 艺术家下拉框
    private static final CheckBox ARTIST_CHECK_BOX = new CheckBox("艺术家："); // 艺术家过滤复选框
    private static final ComboBox<String> ALBUM_COMBO_BOX = new ComboBox<>(); // 专辑下拉框
    private static final CheckBox ALBUM_CHECK_BOX = new CheckBox("专辑："); // 专辑过滤复选框
    private static final ComboBox<String> GENRE_COMBO_BOX = new ComboBox<>(); // 流派下拉框
    private static final CheckBox GENRE_CHECK_BOX = new CheckBox("流派："); // 流派过滤复选框
    private static final CheckBox COVER_CHECK_BOX = new CheckBox("封面为空"); // 封面为空过滤复选框

    private static final Font FONT = new Font(13); // 默认字体

    private static final List<AudioMetaData> ALL_ITEMS = new ArrayList<>(); // 存储所有音频元数据
    private static final List<AudioMetaData> FILTERED_ITEMS = new ArrayList<>(); // 存储过滤后的音频元数据

    private static Button filterButton; // 主界面上的过滤按钮
    private static final String FILTER_ON = "过滤"; // 过滤按钮文本：开启过滤
    private static final String FILTER_OFF = "关闭过滤"; // 过滤按钮文本：关闭过滤

    // 静态初始化块，在类加载时执行，用于初始化UI组件和布局
    static {
        Label LABEL = new Label("请勾选需要参与过滤的条件"); // 提示标签
        LABEL.setFont(FONT);

        VBox allCheckBox = new VBox(26); // 复选框垂直布局，组件间距26
        allCheckBox.setMinWidth(100); // 最小宽度
        allCheckBox.setMaxWidth(100); // 最大宽度
        allCheckBox.getChildren().addAll(TITLE_CHECK_BOX, ARTIST_CHECK_BOX, ALBUM_CHECK_BOX,
                GENRE_CHECK_BOX, COVER_CHECK_BOX); // 添加复选框到布局

        // 监听标题复选框的事件，启用/禁用标题文本框
        TITLE_CHECK_BOX.setOnAction(event -> TITLE_TEXT_FIELD.setDisable(!TITLE_CHECK_BOX.isSelected()));

        // 监听艺术家复选框的事件，启用/禁用艺术家下拉框
        ARTIST_CHECK_BOX.setOnAction(event -> ARTIST_COMBO_BOX.setDisable(!ARTIST_CHECK_BOX.isSelected()));

        // 监听专辑复选框的事件，启用/禁用专辑下拉框
        ALBUM_CHECK_BOX.setOnAction(event -> ALBUM_COMBO_BOX.setDisable(!ALBUM_CHECK_BOX.isSelected()));

        // 监听流派复选框的事件，启用/禁用流派下拉框
        GENRE_CHECK_BOX.setOnAction(event -> GENRE_COMBO_BOX.setDisable(!GENRE_CHECK_BOX.isSelected()));

        TITLE_TEXT_FIELD.setMinWidth(275); // 最小宽度
        TITLE_TEXT_FIELD.setMinWidth(275); // 再次设置最小宽度（可能存在重复）
        TITLE_TEXT_FIELD.setDisable(true); // 默认禁用

        ARTIST_COMBO_BOX.setMinWidth(250); // 最小宽度
        ARTIST_COMBO_BOX.setMaxWidth(250); // 最大宽度
        ARTIST_COMBO_BOX.setEditable(true); // 允许手动输入
        ARTIST_COMBO_BOX.setDisable(true); // 默认禁用

        ALBUM_COMBO_BOX.setMinWidth(275); // 最小宽度
        ALBUM_COMBO_BOX.setMaxWidth(275); // 最大宽度
        ALBUM_COMBO_BOX.setEditable(true); // 允许手动输入
        ALBUM_COMBO_BOX.setDisable(true); // 默认禁用

        GENRE_COMBO_BOX.setMinWidth(200); // 最小宽度
        GENRE_COMBO_BOX.setMaxWidth(200); // 最大宽度
        GENRE_COMBO_BOX.setEditable(true); // 允许手动输入
        GENRE_COMBO_BOX.setDisable(true); // 默认禁用
        GENRE_COMBO_BOX.getItems().add(""); // 添加一个空选项
        GENRE_COMBO_BOX.getItems().addAll(MusicGenre.getGenres()); // 添加所有流派到下拉框

        VBox optionHBox = new VBox(10); // 选项垂直布局，组件间距10
        optionHBox.getChildren().addAll(TITLE_TEXT_FIELD, ARTIST_COMBO_BOX, ALBUM_COMBO_BOX, GENRE_COMBO_BOX); // 添加选项到布局

        HBox mainContent = new HBox(10); // 主内容水平布局，组件间距10
        mainContent.getChildren().addAll(allCheckBox, optionHBox); // 添加复选框和选项到布局

        Button confirmButton = new Button("确定"); // 确定按钮
        confirmButton.setFont(FONT);
        configConfirmButton(confirmButton); // 配置确定按钮的事件处理

        Button cancelButton = new Button("取消"); // 取消按钮
        cancelButton.setFont(FONT);
        cancelButton.setOnAction(event -> { // 取消按钮事件处理：关闭窗口，设置过滤按钮文本为开启
            STAGE.close();
            filterButton.setText(FILTER_ON);
        });

        HBox buttonHBox = new HBox(confirmButton, cancelButton); // 按钮水平布局
        buttonHBox.setSpacing(10); // 按钮间距
        buttonHBox.setAlignment(Pos.CENTER_RIGHT); // 居右对齐
        buttonHBox.setPadding(new Insets(15, 0, 0, 0)); // 上边距

        BODY.setPadding(new Insets(10, 15, 0, 15)); // 主体布局内边距
        BODY.getChildren().addAll(LABEL, mainContent, buttonHBox); // 添加组件到主体布局
        Scene scene = new Scene(BODY, 450, 330); // 创建场景
        STAGE.setScene(scene); // 设置场景
        STAGE.setTitle("过滤"); // 设置标题
        STAGE.setResizable(false); // 禁止调整大小
        STAGE.initModality(Modality.APPLICATION_MODAL); // 设置为模态窗口
    }

    /**
     *  配置确认按钮的事件处理器
     *  @param confirmButton 确认按钮实例
     */
    private static void configConfirmButton(Button confirmButton) {
        confirmButton.setOnAction(event -> { // 确认按钮事件处理
            FILTERED_ITEMS.clear(); // 清空过滤结果
            // 使用流式处理过滤音频元数据
            List<AudioMetaData> filtered = ALL_ITEMS.stream().filter(metaData -> {
                // 艺术家过滤
                if (ARTIST_CHECK_BOX.isSelected()) {
                    // 如果开启艺术家过滤，则判断艺术家是否包含下拉框中的值
                    return metaData.getArtist().contains(ARTIST_COMBO_BOX.getValue().trim());
                } else {
                    // 如果未开启，则直接返回true，即所有音频都通过此过滤器
                    return true;
                }
            }).filter(metaData -> { // 专辑过滤，同艺术家过滤
                if (ALBUM_CHECK_BOX.isSelected()) {
                    return metaData.getAlbum().contains(ALBUM_COMBO_BOX.getValue().trim());
                } else {
                    return true;
                }
            }).filter(metaData -> { // 流派过滤，同艺术家过滤
                if (GENRE_CHECK_BOX.isSelected()) {
                    return metaData.getGenre().contains(GENRE_COMBO_BOX.getValue().trim());
                } else {
                    return true;
                }
            }).filter(metaData -> { // 标题过滤
                if (TITLE_CHECK_BOX.isSelected()) {
                    // 标题过滤和前三个有所不同：如果输入框为空，则判断标题是否为空
                    if ("".equals(TITLE_TEXT_FIELD.getText().trim())) {
                        return metaData.getTitle().equals("");
                    } else {
                        // 否则判断标题是否包含输入框中的值
                        return metaData.getTitle().contains(TITLE_TEXT_FIELD.getText());
                    }
                } else {
                    return true;
                }
            }).filter(metaData -> { // 封面过滤
                if (COVER_CHECK_BOX.isSelected()) {
                    // 判断封面是否为空
                    return metaData.getCover() == null;
                } else {
                    return true;
                }
            }).toList(); // 将结果转换为List

            FILTERED_ITEMS.addAll(filtered); // 添加过滤结果到过滤列表
            Center.updateTableView(FILTERED_ITEMS); // 更新表格视图
            filterButton.setText(FILTER_OFF); // 设置过滤按钮文本为关闭过滤
            STAGE.close(); // 关闭窗口
        });
    }

    /**
     *  显示过滤对话框并设置初始数据
     *  @param dataList  音频元数据列表
     */
    public static void show(List<AudioMetaData> dataList) {
        ALL_ITEMS.clear(); // 清空所有音频数据
        ALL_ITEMS.addAll(dataList); // 添加新的音频数据

        ARTIST_COMBO_BOX.getItems().clear(); // 清空艺术家下拉框选项
        ARTIST_COMBO_BOX.getItems().add(""); // 添加空选项
        ARTIST_COMBO_BOX.getItems().addAll(Center.getAlternativeArtists()); // 添加备选艺术家

        ALBUM_COMBO_BOX.getItems().clear(); // 清空专辑下拉框选项
        ALBUM_COMBO_BOX.getItems().add(""); // 添加空选项
        ALBUM_COMBO_BOX.getItems().addAll(Center.getAlternativeAlbums()); // 添加备选专辑

        STAGE.showAndWait(); // 显示对话框并等待用户操作
    }

    /**
     *  接管主界面上的过滤按钮，并配置其事件处理
     *  @param filter 主界面上的过滤按钮
     */
    public static void takeOverFilterButton(Button filter) {
        filterButton = filter; // 保存过滤按钮的引用
        filterButton.setOnAction(event -> { // 过滤按钮事件处理
            switch (filter.getText()) {
                case FILTER_ON -> Center.filter(); // 如果是开启状态，则执行过滤操作
                case FILTER_OFF -> { // 如果是关闭状态，则关闭过滤
                    Center.turnOffFilter(); // 关闭过滤
                    filter.setText(FILTER_ON); // 设置按钮文本为开启过滤
                }
            }
        });
    }

    /**
     *  设置过滤按钮为开启状态（即关闭过滤）
     */
    public static void setFilterOn() {
        filterButton.setText(FILTER_ON); // 设置按钮文本为开启过滤
        Center.turnOffFilter(); // 关闭过滤
    }
}
