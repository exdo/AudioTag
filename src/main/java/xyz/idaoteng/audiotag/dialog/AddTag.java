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
import xyz.idaoteng.audiotag.UiCoordinator;
import xyz.idaoteng.audiotag.bean.AudioFileData;
import xyz.idaoteng.audiotag.constant.EditableTag;
import xyz.idaoteng.audiotag.jaudiotagger.AudioFileWriter;
import xyz.idaoteng.audiotag.util.SameLayout;
import xyz.idaoteng.audiotag.util.Utils;

import java.util.*;

/**
 * AddTagBaseOnFilename类实现基于文件名解析并添加标签的功能
 * 通过特定格式的模板来解析文件名，并提取相应的内容作为音频文件的元数据标签
 */
public class AddTag {
    private static final TextField TEMPLATE_TEXT_FIELD = new TextField(); // 模板输入文本框
    private static final Font FONT = new Font(13);
    private static final RadioButton SKIP_RADIO_BUTTON = new RadioButton("保持原有的值");
    private static final Stage STAGE = new Stage();

    private static final List<AudioFileData> DATA_LIST = new ArrayList<>(); // 存储待处理的音频元数据列表

    private static final String[] PLACEHOLDERS = {"title", "artist", "album", "date", "track", "ignore"};
    private static final HashSet<String> VALID_PLACEHOLDERS = new HashSet<>(Arrays.asList(PLACEHOLDERS));

    // 静态初始化块 - 在类加载时执行UI初始化
    static {
        // 创建主布局容器
        VBox body = new VBox();
        body.setPadding(new Insets(15, 20, 10, 20)); // 设置内边距
        body.setSpacing(10); // 设置组件间距

        // 添加模板标签
        Label templateLabel = new Label("文件名构成模板：");
        templateLabel.setFont(FONT);

        // 配置模板输入框
        TEMPLATE_TEXT_FIELD.setMinWidth(350);
        MenuButton menuButton = new MenuButton("选择标签"); // 标签选择菜单

        // 将文本框和菜单按钮关联
        SameLayout.linkTextAndButton(TEMPLATE_TEXT_FIELD, menuButton, true);

        // 将输入框和按钮打包到水平布局
        HBox templateAndMenuButton = SameLayout.packageIntoHBox(TEMPLATE_TEXT_FIELD, menuButton);

        // 添加策略标签
        Label strategyLabel = new Label("标签添加策略-当原来的标签有值时：");
        strategyLabel.setFont(FONT);

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
        confirm.setOnAction(event -> startAddTag());

        Button cancel = new Button("取消");
        cancel.setOnAction(event -> STAGE.close()); // 点击取消关闭窗口

        // 将按钮放入水平布局并右对齐
        HBox confirmAndCancel = new HBox(15);
        confirmAndCancel.setAlignment(Pos.CENTER_RIGHT);
        confirmAndCancel.getChildren().addAll(confirm, cancel);

        // 将所有组件添加到主布局
        body.getChildren().addAll(
                templateLabel,
                templateAndMenuButton,
                strategyLabel,
                SKIP_RADIO_BUTTON,
                rewrite,
                confirmAndCancel
        );

        // 配置对话框窗口属性
        STAGE.initModality(Modality.APPLICATION_MODAL);
        STAGE.setTitle("基于文件名添加标签");
        Scene scene = new Scene(body, 510, 240);
        STAGE.setResizable(false); // 禁止调整窗口大小
        STAGE.setScene(scene);
    }

    /**
     * 配置"确定"按钮的事件处理器
     */
    private static void startAddTag() {
        String template = TEMPLATE_TEXT_FIELD.getText().trim();

        // 检查模板是否为空
        if (template.equals("")) {
            Utils.errorAlert("模板不能为空").show();
            return; // 模板为空时直接返回，不执行后续操作
        }

        // 解析模板并处理文件名
        if (parseTemplate(template)) {
            for (AudioFileData metaData : DATA_LIST) {
                addTagByTemplateFromFilename(metaData); // 根据模板解析每个文件名并添加标签
            }
        } else {
            Utils.errorAlert("模板不合法").show();
            return;
        }

        STAGE.close(); // 关闭对话框
        UiCoordinator.showNotification("标签已添加"); // 显示操作成功的通知
    }

    // 用于存储解析出的标签名和分隔符的队列
    private static final ArrayDeque<String> TAG_NAMES = new ArrayDeque<>();
    private static final ArrayDeque<String> SEPARATORS = new ArrayDeque<>();

    /**
     * 解析用户输入的模板
     *
     * @param template 用户输入的模板字符串
     * @return 如果模板合法返回true，否则返回false
     */
    private static boolean parseTemplate(String template) {
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
     *
     * @param data 音频文件元数据对象
     */
    private static void addTagByTemplateFromFilename(AudioFileData data) {
        // 获取不带扩展名的文件名
        String filename = Utils.getFilenameWithoutExtension(data.getFilename());

        // 创建临时队列以便复用
        ArrayDeque<String> tempTagNames = new ArrayDeque<>(TAG_NAMES);
        ArrayDeque<String> tempSeparators = new ArrayDeque<>(SEPARATORS);

        // 待更新的标签名
        ArrayList<EditableTag> changedTags = new ArrayList<>();
        String separator = tempSeparators.poll(); // 获取第一个分隔符
        while (separator != null) {
            // 如果文件名包含当前分隔符
            if (filename.contains(separator)) {
                // 按分隔符分割文件名
                String[] slice = filename.split(separator, 2); // 最多分割成两部分

                // 如果有标签需要处理
                if (!tempTagNames.isEmpty()) {
                    changedTags.addAll(updateTag(data, tempTagNames.poll(), slice[0])); // 第一部分作为标签值
                }

                // 剩余部分继续处理
                filename = slice[1];
            } else {
                // 文件名不包含分隔符，剩余部分作为下一个标签值
                if (!tempTagNames.isEmpty()) {
                    changedTags.addAll(updateTag(data, tempTagNames.poll(), filename));
                }
            }

            // 获取下一个分隔符
            separator = tempSeparators.poll();
        }

        // 处理最后一次分割的剩余部分
        if (!tempTagNames.isEmpty() && !"".equals(filename)) {
            changedTags.addAll(updateTag(data, tempTagNames.poll(), filename));
        }

        AudioFileWriter.updateTag(data, changedTags);
    }

    /**
     * 根据策略添加标签到元数据对象
     *
     * @param data    音频文件元数据对象
     * @param tagName 标签名
     * @param value   标签值
     */
    private static ArrayList<EditableTag> updateTag(AudioFileData data, String tagName, String value) {
        // 待更新的标签名
        ArrayList<EditableTag> tags = new ArrayList<>();
        if (SKIP_RADIO_BUTTON.isSelected()) {
            switch (tagName) {
                case "title" -> {
                    // 如果选择跳过已有值的标签，则只在原值为空时添加
                    if ("".equals(data.getTitle())) {
                        data.setTitle(value);
                        tags.add(EditableTag.TITLE);
                    }
                }
                case "artist" -> {
                    if ("".equals(data.getArtist())) {
                        data.setArtist(value);
                        tags.add(EditableTag.ARTIST);
                    }
                }
                case "album" -> {
                    if ("".equals(data.getAlbum())) {
                        data.setAlbum(value);
                        tags.add(EditableTag.ALBUM);
                    }
                }
                case "date" -> {
                    if ("".equals(data.getDate())) {
                        data.setDate(value);
                        tags.add(EditableTag.DATE);
                    }
                }
                case "track" -> {
                    if ("".equals(data.getTrack())) {
                        data.setTrack(value);
                        tags.add(EditableTag.TRACK);
                    }
                }
                case "ignore" -> {
                } // 忽略该部分
            }
        } else {
            switch (tagName) {
                case "title" -> {
                    // 覆盖模式，直接设置值
                    data.setTitle(value);
                    tags.add(EditableTag.TITLE);
                }
                case "artist" -> {
                    data.setArtist(value);
                    tags.add(EditableTag.ARTIST);
                }
                case "album" -> {
                    data.setAlbum(value);
                    tags.add(EditableTag.ALBUM);
                }
                case "date" -> {
                    data.setDate(value);
                    tags.add(EditableTag.DATE);
                }
                case "track" -> {
                    data.setTrack(value);
                    tags.add(EditableTag.TRACK);
                }
                case "ignore" -> {
                } // 忽略该部分
            }
        }
        return tags;
    }

    /**
     * 显示对话框并设置待处理的数据
     *
     * @param dataList 待处理的音频元数据列表
     */
    public static void show(List<AudioFileData> dataList) {
        DATA_LIST.clear(); // 清空现有数据
        DATA_LIST.addAll(dataList); // 添加新数据
        STAGE.show(); // 显示对话框
    }
}
