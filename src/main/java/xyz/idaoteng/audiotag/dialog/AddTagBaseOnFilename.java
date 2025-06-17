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
import xyz.idaoteng.audiotag.Utils;
import xyz.idaoteng.audiotag.bean.AudioMetaData;
import xyz.idaoteng.audiotag.component.Aside;
import xyz.idaoteng.audiotag.component.Center;
import xyz.idaoteng.audiotag.constant.EditableTag;
import xyz.idaoteng.audiotag.core.MetaDataWriter;
import xyz.idaoteng.audiotag.component.Notification;

import java.util.*;

/**
 * AddTagBaseOnFilename类实现基于文件名解析并添加标签的功能
 * 通过特定格式的模板来解析文件名，并提取相应的内容作为音频文件的元数据标签
 */
public class AddTagBaseOnFilename {
    // UI组件定义
    private static final TextField TEMPLATE_TEXT_FIELD = new TextField(); // 模板输入文本框
    private static final MenuButton MENU_BUTTON = new MenuButton("选择标签"); // 标签选择菜单
    private static final Font FONT = new Font(13); // 字体样式
    private static final RadioButton SKIP_RADIO_BUTTON = new RadioButton("保持原有的值"); // 跳过已有值的单选按钮
    private static final Stage STAGE = new Stage(); // 对话框窗口

    // 数据存储
    private static final List<AudioMetaData> DATA_LIST = new ArrayList<>(); // 存储待处理的音频元数据列表

    // 模板相关常量
    private static final String[] PLACEHOLDERS = {"title", "artist", "album", "date", "track", "ignore"};
    private static final HashSet<String> VALID_PLACEHOLDERS = new HashSet<>(); // 有效的标签占位符集合

    // 静态初始化块 - 在类加载时执行UI初始化
    static {
        // 初始化有效的标签占位符集合
        VALID_PLACEHOLDERS.addAll(Arrays.asList(PLACEHOLDERS));

        // 创建主布局容器
        VBox body = new VBox();
        body.setPadding(new Insets(15, 20, 10, 20)); // 设置内边距
        body.setSpacing(10); // 设置组件间距

        // 添加模板标签
        Label model = new Label("文件名构成模板：");
        model.setFont(FONT);

        // 配置模板输入框
        TEMPLATE_TEXT_FIELD.setMinWidth(350);

        // 将文本框和菜单按钮关联
        CommonConfig.linkTextAndButton(TEMPLATE_TEXT_FIELD, MENU_BUTTON, true);

        // 将输入框和按钮打包到水平布局
        HBox templateAndMenuButton = CommonConfig.packageIntoHBox(TEMPLATE_TEXT_FIELD, MENU_BUTTON);

        // 添加策略标签
        Label strategy = new Label("标签添加策略-当原来的标签有值时：");
        strategy.setFont(FONT);

        // 创建策略选择单选按钮组
        ToggleGroup strategyGroup = new ToggleGroup();
        SKIP_RADIO_BUTTON.setFont(FONT);
        SKIP_RADIO_BUTTON.setToggleGroup(strategyGroup);
        SKIP_RADIO_BUTTON.setSelected(true); // 默认选择"保持原有值"

        RadioButton rewrite = new RadioButton("覆盖原有的的值");
        rewrite.setFont(FONT);
        rewrite.setToggleGroup(strategyGroup);

        // 创建确定和取消按钮
        Button confirm = new Button("确定");
        setupConfirmButton(confirm); // 配置确认按钮的事件处理

        Button cancel = new Button("取消");
        cancel.setOnAction(event -> STAGE.close()); // 点击取消关闭窗口

        // 将按钮放入水平布局并右对齐
        HBox confirmAndCancel = new HBox(15);
        confirmAndCancel.setAlignment(Pos.CENTER_RIGHT);
        confirmAndCancel.getChildren().addAll(confirm, cancel);

        // 将所有组件添加到主布局
        body.getChildren().addAll(model, templateAndMenuButton, strategy,
                SKIP_RADIO_BUTTON, rewrite, confirmAndCancel);

        // 配置对话框窗口属性
        STAGE.initModality(Modality.APPLICATION_MODAL); // 设置为模态窗口
        STAGE.setTitle("基于文件名添加标签");
        Scene scene = new Scene(body, 510, 240); // 设置窗口大小
        STAGE.setResizable(false); // 禁止调整窗口大小
        STAGE.setScene(scene);
    }

    /**
     * 配置"确定"按钮的事件处理器
     * @param confirm 确定按钮实例
     */
    private static void setupConfirmButton(Button confirm) {
        confirm.setOnAction(event -> {
            String template = TEMPLATE_TEXT_FIELD.getText().trim();

            // 检查模板是否为空
            if (template.equals("")) {
                Alert alert = Utils.generateBasicErrorAlert("模板不能为空");
                alert.show();
                return; // 模板为空时直接返回，不执行后续操作
            }

            // 解析模板并处理文件名
            if (parseTemplate(template)) {
                for (AudioMetaData metaData : DATA_LIST) {
                    parseFilename(metaData); // 根据模板解析每个文件名并添加标签
                }
            } else {
                Alert alert = Utils.generateBasicErrorAlert("模板不合法");
                alert.show();
            }

            // 更新UI并关闭窗口
            Center.updateTableView(null); // 刷新表格视图
            Aside.refresh(); // 刷新侧边栏
            STAGE.close(); // 关闭对话框
            Notification.showNotification("标签已添加"); // 显示操作成功的通知
        });
    }

    // 用于存储解析出的标签名和分隔符的队列
    private static final ArrayDeque<String> TAG_NAMES = new ArrayDeque<>();
    private static final ArrayDeque<String> SEPARATORS = new ArrayDeque<>();

    /**
     * 解析用户输入的模板
     * @param template 用户输入的模板字符串
     * @return 如果模板合法返回true，否则返回false
     */
    public static boolean parseTemplate(String template) {
        TAG_NAMES.clear(); // 清空标签名队列
        SEPARATORS.clear(); // 清空分隔符队列

        if (template.charAt(0) != '`' || template.charAt(template.length() - 1) != '`') {
            return false; // 模板必须以 ` 开头和结尾
        }

        for (int i = 0; i < template.length(); i++) {
            // 查找模板中的占位符(用 ` 包围的标签名)
            if (template.charAt(i) == '`') {
                int second = template.indexOf('`', i + 1); // 找第二个 `

                if (second != -1) {
                    String placeholder = template.substring(i + 1, second); // 提取占位符内容

                    // 检查占位符是否有效
                    if (VALID_PLACEHOLDERS.contains(placeholder)) {
                        TAG_NAMES.add(placeholder); // 添加到标签队列
                    } else {
                        return false; // 无效的占位符
                    }

                    // 查找分隔符(下一对 ` 之间的内容)
                    int third = template.indexOf('`', second + 1);
                    if (third != -1) {
                        // 从第二个 ` 之后到第三个 ` 之前的是分隔符
                        SEPARATORS.add(template.substring(second + 1, third));
                        i = third - 1; // 跳过已处理的部分
                    }
                }
            }
        }
        return true; // 模板解析成功
    }

    /**
     * 根据模板解析文件名并添加标签
     * @param metaData 音频文件元数据对象
     */
    private static void parseFilename(AudioMetaData metaData) {
        // 获取不带扩展名的文件名
        String filename = Utils.getFilenameWithoutExtension(metaData.getFilename());

        // 创建临时队列以便复用
        ArrayDeque<String> tempTagNames = new ArrayDeque<>(TAG_NAMES);
        ArrayDeque<String> tempSeparators = new ArrayDeque<>(SEPARATORS);

        String separator = tempSeparators.poll(); // 获取第一个分隔符

        while (separator != null) {
            // 如果文件名包含当前分隔符
            if (filename.contains(separator)) {
                // 按分隔符分割文件名
                String[] slice = filename.split(separator, 2); // 最多分割成两部分

                // 如果有标签需要处理
                if (!tempTagNames.isEmpty()) {
                    addTag(metaData, tempTagNames.poll(), slice[0]); // 第一部分作为标签值
                }

                // 剩余部分继续处理
                filename = slice[1];
            } else {
                // 文件名不包含分隔符，剩余部分作为下一个标签值
                if (!tempTagNames.isEmpty()) {
                    addTag(metaData, tempTagNames.poll(), filename);
                }
            }

            // 获取下一个分隔符
            separator = tempSeparators.poll();
        }

        // 处理最后一次分割的剩余部分
        if (!tempTagNames.isEmpty() && !"".equals(filename)) {
            addTag(metaData, tempTagNames.poll(), filename);
        }

        writeTags(metaData);
    }

    // 待写入的标签名
    private static final ArrayList<EditableTag> TAGS = new ArrayList<>();
    /**
     * @param metaData 音频文件元数据对象
     */
    private static void writeTags(AudioMetaData metaData) {
        MetaDataWriter.write(metaData, TAGS);
        // 重置数据
        TAGS.clear();
    }

    /**
     * 根据策略添加标签到元数据对象
     * @param metaData 音频文件元数据对象
     * @param tagName 标签名
     * @param value 标签值
     */
    private static void addTag(AudioMetaData metaData, String tagName, String value) {
        switch (tagName) {
            case "title" -> {
                if (SKIP_RADIO_BUTTON.isSelected()) {
                    // 如果选择跳过已有值，则只在原值为空时添加
                    if ("".equals(metaData.getTitle())) {
                        metaData.setTitle(value);
                        TAGS.add(EditableTag.TITLE);
                    }
                } else {
                    // 覆盖模式，直接设置值
                    metaData.setTitle(value);
                    TAGS.add(EditableTag.TITLE);
                }
            }
            case "artist" -> {
                if (SKIP_RADIO_BUTTON.isSelected()) {
                    if ("".equals(metaData.getArtist())) {
                        metaData.setArtist(value);
                        TAGS.add(EditableTag.ARTIST);
                    }
                } else {
                    metaData.setArtist(value);
                    TAGS.add(EditableTag.ARTIST);
                }
            }
            case "album" -> {
                if (SKIP_RADIO_BUTTON.isSelected()) {
                    if ("".equals(metaData.getAlbum())) {
                        metaData.setAlbum(value);
                        TAGS.add(EditableTag.ALBUM);
                    }
                } else {
                    metaData.setAlbum(value);
                    TAGS.add(EditableTag.ALBUM);
                }
            }
            case "date" ->{
                if (SKIP_RADIO_BUTTON.isSelected()) {
                    if ("".equals(metaData.getDate())) {
                        metaData.setDate(value);
                        TAGS.add(EditableTag.DATE);
                    }
                } else {
                    metaData.setDate(value);
                    TAGS.add(EditableTag.DATE);
                }
            }
            case "track" -> {
                if (SKIP_RADIO_BUTTON.isSelected()) {
                    if ("".equals(metaData.getTrack())) {
                        metaData.setTrack(value);
                        TAGS.add(EditableTag.TRACK);
                    }
                } else {
                    metaData.setTrack(value);
                    TAGS.add(EditableTag.TRACK);
                }
            }
            case "ignore" -> {} // 忽略该部分
        }
    }

    /**
     * 显示对话框并设置待处理的数据
     * @param metaDataList 待处理的音频元数据列表
     */
    public static void show(List<AudioMetaData> metaDataList) {
        DATA_LIST.clear(); // 清空现有数据
        DATA_LIST.addAll(metaDataList); // 添加新数据
        STAGE.show(); // 显示对话框
    }
}
