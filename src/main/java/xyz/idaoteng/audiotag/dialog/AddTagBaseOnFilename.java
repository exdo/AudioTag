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
import xyz.idaoteng.audiotag.core.MetaDataWriter;

import java.util.*;

public class AddTagBaseOnFilename{
    private static final TextField TEMPLATE_TEXT_FIELD = new TextField();
    private static final MenuButton MENU_BUTTON = new MenuButton("选择标签");
    private static final Font FONT = new Font(13);
    private static final RadioButton SKIP_RADIO_BUTTON = new RadioButton("保持原有的值");
    private static final Stage STAGE = new Stage();

    private static final List<AudioMetaData> DATA_LIST = new ArrayList<>();

    private static final String[] PLACEHOLDERS = {"title", "artist", "album", "date", "track"};
    private static final HashSet<String> VALID_PLACEHOLDERS = new HashSet<>();

    static {
        VALID_PLACEHOLDERS.addAll(Arrays.asList(PLACEHOLDERS));

        VBox body = new VBox();
        body.setPadding(new Insets(15, 30, 10, 30));
        body.setSpacing(10);

        Label model = new Label("文件名构成模板：");
        model.setFont(FONT);

        TEMPLATE_TEXT_FIELD.setMinWidth(350);

        CommonConfig.configMenuButton(TEMPLATE_TEXT_FIELD, MENU_BUTTON);

        HBox templateAndMenuButton = CommonConfig.packageIntoHBox(TEMPLATE_TEXT_FIELD, MENU_BUTTON);

        Label strategy = new Label("标签添加策略-当原来的标签有值时：");
        strategy.setFont(FONT);
        ToggleGroup strategyGroup = new ToggleGroup();
        SKIP_RADIO_BUTTON.setFont(FONT);
        SKIP_RADIO_BUTTON.setToggleGroup(strategyGroup);
        SKIP_RADIO_BUTTON.setSelected(true);
        RadioButton rewrite = new RadioButton("覆盖原有的的值");
        rewrite.setFont(FONT);
        rewrite.setToggleGroup(strategyGroup);

        Button confirm = new Button("确定");
        configConfirmButton(confirm);
        Button cancel = new Button("取消");
        cancel.setOnAction(event -> STAGE.close());

        HBox confirmAndCancel = new HBox(15);
        confirmAndCancel.setAlignment(Pos.CENTER_RIGHT);
        confirmAndCancel.getChildren().addAll(confirm, cancel);

        body.getChildren().addAll(model, templateAndMenuButton, strategy,
                SKIP_RADIO_BUTTON, rewrite, confirmAndCancel);

        STAGE.initModality(Modality.APPLICATION_MODAL);
        STAGE.setTitle("基于文件名添加标签");
        Scene scene = new Scene(body, 500, 200);
        STAGE.setResizable(false);
        STAGE.setScene(scene);
    }

    private static void configConfirmButton(Button confirm) {
        confirm.setOnAction(event -> {
            String template = TEMPLATE_TEXT_FIELD.getText().trim();
            if (template.equals("")) {
                Alert alert = Utils.generateBasicErrorAlert("模板不能为空");
                alert.show();
            }

            // 解析模板，找出所有分隔符和待添加的标签名
            if (parseTemplate(template)) {
                for (AudioMetaData metaData : DATA_LIST) {
                    parsFilename(metaData);
                }
            } else {
                Alert alert = Utils.generateBasicErrorAlert("模板不合法");
                alert.show();
            }

            Center.updateTableView(null);
            Aside.refresh();
            STAGE.close();
        });
    }

    public static void show(List<AudioMetaData> metaDataList) {
        DATA_LIST.clear();
        DATA_LIST.addAll(metaDataList);
        STAGE.show();
    }

    private static final ArrayDeque<String> TAG_NAMES = new ArrayDeque<>();
    private static final ArrayDeque<String> SEPARATORS = new ArrayDeque<>();
    // 返回true表示模板合法，返回false表示模板不合法
    public static boolean parseTemplate(String template) {
        TAG_NAMES.clear();
        SEPARATORS.clear();
        for (int i = 0; i < template.length(); i++) {
            if (template.charAt(i) == '`') {
                // int first = i;
                int second = template.indexOf('`', i + 1);
                if (second != -1) {
                    String placeholder = template.substring(i + 1, second);
                    if (VALID_PLACEHOLDERS.contains(placeholder)) {
                        TAG_NAMES.add(placeholder);
                    } else {
                        // 无效的占位符（标签名）
                        return false;
                    }

                    int third = template.indexOf('`', second + 1);
                    if (third != -1) {
                        // 从 second + 1 开始，到 third - 1 结束的子字符串就是分隔符
                        SEPARATORS.add(template.substring(second + 1, third));
                        i = third - 1;
                    } else {
                        // 第三个'`'没找到，但第二个'`'又不是最后一个字符
                        if (second != template.length() - 1) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    private static void parsFilename(AudioMetaData metaData) {
        String filename = Utils.getFilenameWithoutExtension(metaData.getFilename());

        ArrayDeque<String> tempTagNames = new ArrayDeque<>(TAG_NAMES);
        ArrayDeque<String> tempSeparators = new ArrayDeque<>(SEPARATORS);
        String separator = tempSeparators.poll();
        while (separator != null) {
            // 如果文件名包含分隔符
            if (filename.contains(separator)) {
                String[] slice = filename.split(separator, 2);
                // 则第一个切片为下一个标签（如果有的话）的标签值
                if (!tempTagNames.isEmpty()) {
                    addTag(metaData, tempTagNames.poll(), slice[0]);
                }
                // 剩下的所有字符接着分割
                filename = slice[1];
            } else {
                // 如果文件名不包含分隔符
                // 则剩下的所有字符为下一个标签（如果有的话）的标签值
                if (!tempTagNames.isEmpty()) {
                    addTag(metaData, tempTagNames.poll(), filename);
                }
            }

            separator = tempSeparators.poll();
        }
        // 处理最后一次分割的第二个切片
        if (!tempTagNames.isEmpty() && !"".equals(filename)) {
            addTag(metaData, tempTagNames.poll(), filename);
        }
    }

    private static void addTag(AudioMetaData metaData, String tagName, String value) {
        switch (tagName) {
            case "title" -> {
                if (SKIP_RADIO_BUTTON.isSelected()) {
                    // 如果选择跳过的话，只在原标签值为空时添加（下同）
                    if ("".equals(metaData.getTitle())) {
                        metaData.setTitle(value);
                        MetaDataWriter.write(metaData);
                    }
                } else {
                    metaData.setTitle(value);
                    MetaDataWriter.write(metaData);
                }
            }
            case "artist" -> {
                if (SKIP_RADIO_BUTTON.isSelected()) {
                    if ("".equals(metaData.getArtist())) {
                        metaData.setArtist(value);
                        MetaDataWriter.write(metaData);
                    }
                } else {
                    metaData.setArtist(value);
                    MetaDataWriter.write(metaData);
                }
            }
            case "album" -> {
                if (SKIP_RADIO_BUTTON.isSelected()) {
                    if ("".equals(metaData.getAlbum())) {
                        metaData.setAlbum(value);
                        MetaDataWriter.write(metaData);
                    }
                } else {
                    metaData.setAlbum(value);
                    MetaDataWriter.write(metaData);
                }
            }
            case "date" ->{
                if (SKIP_RADIO_BUTTON.isSelected()) {
                    if ("".equals(metaData.getDate())) {
                        metaData.setDate(value);
                        MetaDataWriter.write(metaData);
                    }
                } else {
                    metaData.setDate(value);
                    MetaDataWriter.write(metaData);
                }
            }
            case "track" -> {
                if (SKIP_RADIO_BUTTON.isSelected()) {
                    if ("".equals(metaData.getTrack())) {
                        metaData.setTrack(value);
                        MetaDataWriter.write(metaData);
                    }
                } else {
                    metaData.setTrack(value);
                    MetaDataWriter.write(metaData);
                }
            }
        }
    }
}
