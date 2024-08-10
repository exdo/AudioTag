package xyz.idaoteng.lesson1.component;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import xyz.idaoteng.lesson1.ApplicationContext;

import java.io.File;

public class Head {
    private static final HBox head = new HBox(6);

    static {
        init();
    }

    private static void init() {
        head.setMaxHeight(30);
        head.setMinHeight(30);
        head.setPadding(new Insets(5, 0, 0, 0));
        head.setStyle("-fx-border-style: solid; -fx-border-color: #cccccc; -fx-border-width: 0 0 1 0");

        Button selectFileButton = new Button("选择文件");
        ApplicationContext.register("selectFileButton", selectFileButton);
        setSelectFileButtonController();

        Button selectDirectoryButton = new Button("选择文件夹");
        ApplicationContext.register("selectDirectoryButton", selectDirectoryButton);
        setSelectDirectoryButtonController();

        Button artistListButton = new Button("备选艺术家表");
        Button albumListButton = new Button("备选专辑表");

        ApplicationContext.register("artistListButton", artistListButton);
        ApplicationContext.register("albumListButton", albumListButton);

        head.getChildren().addAll(selectFileButton, selectDirectoryButton, artistListButton, albumListButton);
    }

    private static void setSelectDirectoryButtonController() {
        Stage stage = (Stage) ApplicationContext.get("stage");
        Button selectDirectoryButton = (Button) ApplicationContext.get("selectDirectoryButton");
        selectDirectoryButton.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("选择文件夹");
            File file = fileChooser.showOpenDialog(stage);
            if (file == null) {
                System.out.println("未选择文件夹");
                return;
            }
            System.out.println(file.getAbsolutePath());
        });
    }

    public static Node getHead() {
        return head;
    }

    private static void setSelectFileButtonController() {
        Stage stage = (Stage) ApplicationContext.get("stage");
        Button selectFileButton = (Button) ApplicationContext.get("selectFileButton");
        selectFileButton.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("选择文件");
            File file = fileChooser.showOpenDialog(stage);
            if (file == null) {
                System.out.println("未选择文件");
                return;
            }
            System.out.println(file.getAbsolutePath());
        });
    }
}
