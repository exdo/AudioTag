package xyz.idaoteng.audiotag.dialog;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import xyz.idaoteng.audiotag.Session;
import xyz.idaoteng.audiotag.bean.AppConfiguration;
import xyz.idaoteng.audiotag.util.Utils;

import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

public class SetupAppConfig {
    private static AppConfiguration workingConfig;
    // 封面重绘设置
    private static final CheckBox NEED_RETOUCH = new CheckBox("统一封面图片格式和大小");
    private static final ComboBox<String> FORMAT = new ComboBox<>();
    private static final TextField WIDTH = new TextField();
    private static final TextField HEIGHT = new TextField();

    // 歌词保存策略
    private static final CheckBox WRITE_IN_TAG = new CheckBox("将歌词写入标签内");
    private static final CheckBox WRITE_IN_FILE = new CheckBox("将歌词写入 .lrc 文件");
    private static final CheckBox CREATE_LYRICS_FOLDER = new CheckBox("创建 lyrics 文件夹");

    private static final Stage STAGE = new Stage();

    static {
        STAGE.setTitle("修改配置");
        STAGE.initModality(Modality.APPLICATION_MODAL);
        STAGE.setResizable(false);
        
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.TOP_LEFT);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));

        int row = 0;

        // 封面重绘设置
        Label coverSettingsLabel = new Label("图片参数设置:");
        coverSettingsLabel.setStyle("-fx-font-weight: bold;");
        grid.add(coverSettingsLabel, 0, row++, 2, 1);

        grid.add(NEED_RETOUCH, 0, row++, 2, 1);

        Label lblFormat = new Label("格式:");
        FORMAT.getItems().addAll("jpg", "png", "bmp");
        grid.add(lblFormat, 0, row);
        grid.add(FORMAT, 1, row++);

        Label lblWidth = new Label("宽:");
        setupNumericTextField(WIDTH);
        grid.add(lblWidth, 0, row);
        grid.add(WIDTH, 1, row++);

        Label lblHeight = new Label("高:");
        setupNumericTextField(HEIGHT);
        grid.add(lblHeight, 0, row);
        grid.add(HEIGHT, 1, row++);

        // 绑定重绘选项的启用/禁用状态
        FORMAT.disableProperty().bind(NEED_RETOUCH.selectedProperty().not());
        WIDTH.disableProperty().bind(NEED_RETOUCH.selectedProperty().not());
        HEIGHT.disableProperty().bind(NEED_RETOUCH.selectedProperty().not());

        row++; // 添加空隙

        // 歌词保存策略
        Label lyricsSettingsLabel = new Label("歌词保存配置:");
        lyricsSettingsLabel.setStyle("-fx-font-weight: bold;");
        grid.add(lyricsSettingsLabel, 0, row++, 2, 1);

        grid.add(WRITE_IN_TAG, 0, row++, 2, 1);
        grid.add(WRITE_IN_FILE, 0, row++, 2, 1);
        grid.add(CREATE_LYRICS_FOLDER, 0, row++, 2, 1);

        row++; // 添加空隙

        // 按钮区
        Button btnSave = new Button("保存");
        Button btnCancel = new Button("取消");

        HBox hbButtons = new HBox(10);
        hbButtons.setAlignment(Pos.BOTTOM_RIGHT);
        hbButtons.getChildren().addAll(btnSave, btnCancel);
        grid.add(hbButtons, 0, row, 2, 1);

        btnSave.setOnAction(e -> saveConfig());

        btnCancel.setOnAction(e -> STAGE.close());
        
        Scene scene = new Scene(grid);
        STAGE.setScene(scene);
    }
    
    private static void saveConfig() {
        try {
            // 读取UI数据并更新workingConfig
            workingConfig.setNeedRetouch(NEED_RETOUCH.isSelected());
            workingConfig.setFormat(FORMAT.getValue());

            // 只有当needRetouch选中时，才读取宽度和高度，否则保持workingConfig中的当前值
            if (NEED_RETOUCH.isSelected()) {
                int width = Integer.parseInt(WIDTH.getText());
                int height = Integer.parseInt(HEIGHT.getText());

                if (width <= 0 || height <= 0) {
                    Utils.errorAlert("宽和高须为正数");
                    return; // 验证失败，不关闭窗口
                }
                workingConfig.setWidth(width);
                workingConfig.setHeight(height);
            }

            workingConfig.setWriteInTag(WRITE_IN_TAG.isSelected());
            workingConfig.setWriteInFile(WRITE_IN_FILE.isSelected());
            workingConfig.setCreateLyricsFolder(CREATE_LYRICS_FOLDER.isSelected());
            Session.setConfig(workingConfig);
            STAGE.close();
        } catch (NumberFormatException ex) {
            Utils.errorAlert("输入的数字无效");
        }
    }
    
    /**
     * 设置TextField只允许输入数字。
     * @param textField 要设置的TextField
     */
    private static void setupNumericTextField(TextField textField) {
        Pattern pattern = Pattern.compile("\\d*"); // 匹配0个或多个数字
        UnaryOperator<TextFormatter.Change> filter = change -> {
            String newText = change.getControlNewText();
            if (pattern.matcher(newText).matches()) {
                return change;
            }
            return null; // 拒绝输入
        };
        textField.setTextFormatter(new TextFormatter<>(filter));
    }

    /**
     * 显示应用程序配置对话框。
     *
     */
    public static void show() {
        // 创建一个用于UI操作的配置副本，避免直接修改传入的原始对象
        workingConfig = new AppConfiguration(Session.getConfig());

        NEED_RETOUCH.setSelected(workingConfig.getNeedRetouch());
        FORMAT.getSelectionModel().select(workingConfig.getFormat());
        WIDTH.setText(String.valueOf(workingConfig.getWidth()));
        HEIGHT.setText(String.valueOf(workingConfig.getHeight()));
        WRITE_IN_TAG.setSelected(workingConfig.isWriteInTag());
        WRITE_IN_FILE.setSelected(workingConfig.isWriteInFile());
        CREATE_LYRICS_FOLDER.setSelected(workingConfig.isCreateLyricsFolder());

        STAGE.show();
    }
}
