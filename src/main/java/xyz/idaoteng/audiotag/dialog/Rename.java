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
import xyz.idaoteng.audiotag.bean.AudioFileData;
import xyz.idaoteng.audiotag.bean.FilenamePreview;
import xyz.idaoteng.audiotag.exception.InvalidPlaceholderException;
import xyz.idaoteng.audiotag.util.SameLayout;
import xyz.idaoteng.audiotag.util.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Rename {
    private static final TextField TEMPLATE_TEXT_FIELD = new TextField();
    private static final MenuButton MENU_BUTTON = new MenuButton("选择标签");
    private static final Font FONT = new Font(13);
    private static final RadioButton GIVE_UP_RADIO_BUTTON = new RadioButton("放弃重命名");

    private static final Stage STAGE = new Stage();

    private static final List<AudioFileData> DATA_LIST = new ArrayList<>();

    static {
        VBox body = new VBox();
        body.setPadding(new Insets(15, 20, 10, 20));
        body.setSpacing(10);

        Label model = new Label("文件名构成模板：");
        model.setFont(FONT);

        SameLayout.linkTextAndButton(TEMPLATE_TEXT_FIELD, MENU_BUTTON, false);

        HBox templateAndMenuButton = SameLayout.packageIntoHBox(TEMPLATE_TEXT_FIELD, MENU_BUTTON);

        Label strategy = new Label("重命名策略-当模板中某标签为空时：");
        strategy.setFont(FONT);
        ToggleGroup strategyGroup = new ToggleGroup();
        GIVE_UP_RADIO_BUTTON.setFont(FONT);
        GIVE_UP_RADIO_BUTTON.setToggleGroup(strategyGroup);
        GIVE_UP_RADIO_BUTTON.setSelected(true);
        RadioButton fillBlank = new RadioButton("填入空字符串");
        fillBlank.setFont(FONT);
        fillBlank.setToggleGroup(strategyGroup);

        Button confirm = new Button("确定");
        confirm.setOnAction(event -> generatePreview());
        Button cancel = new Button("取消");
        cancel.setOnAction(event -> STAGE.close());

        HBox confirmAndCancel = new HBox(15);
        confirmAndCancel.setAlignment(Pos.CENTER_RIGHT);
        confirmAndCancel.getChildren().addAll(confirm, cancel);

        body.getChildren().addAll(model, templateAndMenuButton, strategy,
                GIVE_UP_RADIO_BUTTON, fillBlank, confirmAndCancel);

        Scene scene = new Scene(body, 510, 240);
        STAGE.initModality(Modality.APPLICATION_MODAL);
        STAGE.setTitle("根据标签重命名");
        STAGE.setResizable(false);
        STAGE.setScene(scene);
    }

    private static void generatePreview() {
        String template = TEMPLATE_TEXT_FIELD.getText().trim();
        if (template.equals("")) {
            Utils.errorAlert("模板不能为空").show();
        } else {
            List<FilenamePreview> previewList = new ArrayList<>(DATA_LIST.size());
            try {
                for (AudioFileData data : DATA_LIST) {
                    String newFilename = buildNameByTemplateOfTag(template, data);
                    if (newFilename != null) {
                        File originalFile = new File(data.getAbsolutePath());
                        File newFile = new File(originalFile.getParentFile(), newFilename);
                        previewList.add(new FilenamePreview(data, newFile));
                    }
                }
            } catch (InvalidPlaceholderException e) {
                Utils.errorAlert(e.getMessage()).show();
                return;
            }

            if (!previewList.isEmpty()) {
                PreviewRename.show(previewList);
            }

            STAGE.close();
        }
    }

    public static String buildNameByTemplateOfTag(String template, AudioFileData data) throws InvalidPlaceholderException {
        StringBuilder sb = new StringBuilder(template.length());

        int i = 0;
        while (i < template.length()) {
            if (template.charAt(i) == '`') {
                int end = template.indexOf('`', i + 1);
                if (end > i) {
                    String placeholder = template.substring(i + 1, end);
                    String actualValue = convertToActualValue(placeholder, data);

                    if (actualValue == null) {
                        throw new InvalidPlaceholderException(placeholder);
                    }

                    if ("".equals(actualValue)) {
                        if (GIVE_UP_RADIO_BUTTON.isSelected()) {
                            // 来自标签的实际值为空且策略为放弃重命名，则返回 null
                            return null;
                        } else {
                            // 来自标签的实际值为空且策略为填充空字符串，则用空格填充
                            sb.append(' ');
                        }
                    } else {
                        // 将两个 ` 间的 占位符 替换成来自标签的实际值
                        sb.append(actualValue);
                    }
                    i = end + 1;
                } else {
                    // 模板中存在未闭合的占位符，原样保留
                    sb.append('`');
                    i++;
                }
            } else {
                // 占位符以外的字符原样保留
                sb.append(template.charAt(i));
                i++;
            }
        }

        return sb + "." + data.getFormat();
    }

    private static String convertToActualValue(String placeholder, AudioFileData data) {
        return switch (placeholder) {
            case "title" -> data.getTitle();
            case "artist" -> data.getArtist();
            case "album" -> data.getAlbum();
            case "date" -> data.getDate();
            case "track" -> data.getTrack();
            default -> null;
        };
    }

    public static void show(List<AudioFileData> dataList) {
        DATA_LIST.clear();
        DATA_LIST.addAll(dataList);
        STAGE.show();
    }
}
