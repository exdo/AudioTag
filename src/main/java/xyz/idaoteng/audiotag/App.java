package xyz.idaoteng.audiotag;

import atlantafx.base.theme.PrimerLight;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import xyz.idaoteng.audiotag.component.*;
import xyz.idaoteng.audiotag.exception.FileCreationException;
import xyz.idaoteng.audiotag.jaudiotagger.TagOption;
import xyz.idaoteng.audiotag.util.ImageInApp;
import xyz.idaoteng.audiotag.util.Utils;

import java.util.logging.Handler;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class App extends Application {
    private static Stage primaryStage;

    private void initSession(Stage primaryStage) {
        try {
            Session.init();
        } catch (FileCreationException e) {
            Scene scene = new Scene(new StackPane());
            primaryStage.setScene(scene);
            primaryStage.initStyle(StageStyle.TRANSPARENT);
            primaryStage.show();
            Alert alert = Utils.errorAlert("程序运行时出现错误");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
            System.exit(555);
        }
    }

    private StackPane buildRootNode() {
        StackPane root = new StackPane();

        BorderPane borderPane = new BorderPane();
        borderPane.setMaxHeight(Screen.getPrimary().getVisualBounds().getHeight());
        borderPane.setPrefHeight(735);
        borderPane.setPrefWidth(1100);

        Center center = new Center();
        Head head = new Head();
        Aside aside = new Aside();
        borderPane.setCenter(center.getNode());
        borderPane.setTop(head.getNode());
        borderPane.setRight(aside.getNode());

        Message message = new Message();

        Modal modal = new Modal();

        UiCoordinator.setComponent(head, center, aside, message, modal);

        root.getChildren().addAll(borderPane, message.getNode(), modal.getNode());
        return root;
    }

    @Override
    public void start(Stage primaryStage) {
        App.primaryStage = primaryStage;

        initSession(primaryStage);

        Scene scene = new Scene(buildRootNode());

        primaryStage.setScene(scene);
        primaryStage.sizeToScene();
        primaryStage.setTitle("音乐信息编辑器");
        primaryStage.getIcons().add(ImageInApp.getAppIcon());
        primaryStage.show();
    }

    @Override
    public void stop() {
        Session.saveSession();
        Session.saveConfig();
    }

    public static void main(String[] args) {
        ImageInApp.loadAllImage();

        // 配置 Jaudiotagger 库
        setupJaudiotagger();

        // 设置主题为 AtlantaFX 的 PrimerLight
        Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());

        launch(args);
    }

    private static void setupJaudiotagger() {
        Logger rootLogger = LogManager.getLogManager().getLogger("");
        for (Handler handler : rootLogger.getHandlers()) {
            rootLogger.removeHandler(handler);
        }
        TagOption.setupOptions();
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }
}
