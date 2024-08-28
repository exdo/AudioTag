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
import xyz.idaoteng.audiotag.bean.Filename;
import xyz.idaoteng.audiotag.component.Center;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class RenameBaseOnTag {
    private static final TextField TEMPLATE_TEXT_FIELD = new TextField();
    private static final MenuButton MENU_BUTTON = new MenuButton("选择标签");
    private static final Font FONT = new Font(13);
    private static final RadioButton GIVE_UP_RADIO_BUTTON = new RadioButton("放弃重命名");

    private static final Stage STAGE = new Stage();

    private static final List<AudioMetaData> DATA_LIST = new ArrayList<>();

    static {
        STAGE.initModality(Modality.APPLICATION_MODAL);
        STAGE.setTitle("根据标签重命名");

        VBox body = new VBox();
        body.setPadding(new Insets(15, 30, 10, 30));
        body.setSpacing(10);

        Label model = new Label("文件名构成模板：");
        model.setFont(FONT);

        CommonConfig.configMenuButton(TEMPLATE_TEXT_FIELD, MENU_BUTTON);

        HBox templateAndMenuButton = CommonConfig.packageIntoHBox(TEMPLATE_TEXT_FIELD, MENU_BUTTON);

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
        configConfirmButton(confirm);
        Button cancel = new Button("取消");
        cancel.setOnAction(event -> STAGE.close());

        HBox confirmAndCancel = new HBox(15);
        confirmAndCancel.setAlignment(Pos.CENTER_RIGHT);
        confirmAndCancel.getChildren().addAll(confirm, cancel);

        body.getChildren().addAll(model, templateAndMenuButton, strategy,
                GIVE_UP_RADIO_BUTTON, fillBlank, confirmAndCancel);

        Scene scene = new Scene(body, 500, 200);
        STAGE.setResizable(false);
        STAGE.setScene(scene);
    }

    private static void configConfirmButton(Button confirm) {
        confirm.setOnAction(event -> {
            if (DATA_LIST.isEmpty()) {
                STAGE.close();
                return;
            }

            String template = TEMPLATE_TEXT_FIELD.getText().trim();
            if (template.equals("")) {
                Alert alert = Utils.generateBasicErrorAlert("模板不能为空");
                alert.show();
            } else {
                boolean skip = GIVE_UP_RADIO_BUTTON.isSelected();
                List<Filename> previewList = new ArrayList<>(DATA_LIST.size());
                for (AudioMetaData data : DATA_LIST) {
                    String newFilename = parseTemplate(template, data, skip);
                    if (newFilename != null) {
                        previewList.add(new Filename(data, buildNewFile(data, newFilename)));
                    }
                }

                if (!previewList.isEmpty()) {
                    Preview.show(previewList);
                }

                STAGE.close();
            }

            Center.updateTableView(null);
        });
    }

    private static File buildNewFile(AudioMetaData data, String newFilename) {
        File originalFile = new File(data.getAbsolutePath());
        return new File(originalFile.getParentFile(), newFilename);
    }

    public static String parseTemplate(String template, AudioMetaData data, boolean skipWhenEmpty) {
        StringBuilder sb = new StringBuilder(template.length());

        int i = 0;
        while (i < template.length()) {
            if (template.charAt(i) == '`') {
                int end = template.indexOf('`', i + 1);
                if (end > i) {
                    String placeholder = template.substring(i + 1, end);
                    String actualValue = convertToActualValue(placeholder, data);
                    if ("".equals(actualValue)) {
                        if (skipWhenEmpty) {
                            return null;
                        } else {
                            sb.append(' ');
                        }
                    } else {
                        sb.append(actualValue);
                    }
                    i = end + 1;
                } else {
                    sb.append('`');
                    i++;
                }
            } else {
                sb.append(template.charAt(i));
                i++;
            }
        }

        String ext = Utils.getExtension(new File(data.getAbsolutePath()));
        sb.append('.').append(ext);
        return sb.toString();
    }

    private static String convertToActualValue(String placeholder, AudioMetaData data) {
        return switch (placeholder) {
            case "title" -> data.getTitle();
            case "artist" -> data.getArtist();
            case "album" -> data.getAlbum();
            case "date" -> data.getDate();
            case "track" -> data.getTrack();
            default -> "";
        };
    }

    public static void show(List<AudioMetaData> metaDataList) {
        DATA_LIST.clear();
        DATA_LIST.addAll(metaDataList);
        STAGE.show();
    }
}
