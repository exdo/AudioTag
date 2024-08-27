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
import xyz.idaoteng.audiotag.AudioMetaData;
import xyz.idaoteng.audiotag.component.Center;

import java.util.ArrayList;
import java.util.List;

public class AddTagBaseOnFilename{
    private static final TextField TEMPLATE_TEXT_FIELD = new TextField();
    private static final MenuButton MENU_BUTTON = new MenuButton("选择标签");
    private static final Font FONT = new Font(13);
    private static final RadioButton SKIP_RADIO_BUTTON = new RadioButton("保持原有的值");
    private static final Stage STAGE = new Stage();

    private static final List<AudioMetaData> DATA_LIST = new ArrayList<>();

    static {
        STAGE.initModality(Modality.APPLICATION_MODAL);
        STAGE.setTitle("基于文件名添加标签");

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

        Scene scene = new Scene(body, 500, 200);
        STAGE.setResizable(false);
        STAGE.setScene(scene);
    }

    private static void configConfirmButton(Button confirm) {
        confirm.setOnAction(event -> {

            Center.updateTableView(null);
        });
    }

    public static void show(List<AudioMetaData> metaDataList) {
        DATA_LIST.clear();
        DATA_LIST.addAll(metaDataList);
        STAGE.show();
    }
}
