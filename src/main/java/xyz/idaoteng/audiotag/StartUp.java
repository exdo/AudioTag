package xyz.idaoteng.audiotag;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import xyz.idaoteng.audiotag.component.Aside;
import xyz.idaoteng.audiotag.component.Bottom;
import xyz.idaoteng.audiotag.component.Center;
import xyz.idaoteng.audiotag.component.Head;

import java.io.InputStream;
import java.util.logging.LogManager;

public class StartUp extends Application {
    private static Stage stage;

    @Override
    public void start(Stage primaryStage) {
        StartUp.stage = primaryStage;

        BorderPane root = new BorderPane();
        root.setTop(Head.getHead());
        root.setCenter(Center.getCenter());
        root.setRight(Aside.getAside());
        root.setBottom(Bottom.getBottom());

        Scene scene = new Scene(root, 1131, 725);
        primaryStage.setScene(scene);
        primaryStage.setTitle("音乐信息编辑器");
        primaryStage.getIcons().add(ImageInApp.getAppIcon());
        primaryStage.setMinHeight(root.getHeight());
        primaryStage.setMinWidth(885);
        primaryStage.show();

        Center.configWhenTableAlreadyRendered();
    }

    @Override
    public void stop() {
        Session.saveSession();
    }

    public static Stage getPrimaryStage() {
        return stage;
    }

    public static void main(String[] args) {
        LogManager logManager = LogManager.getLogManager();
        try (InputStream input = StartUp.class.getResourceAsStream("log.properties")) {
            logManager.readConfiguration(input);
        } catch (Exception e) {
            System.out.println("读取日志配置失败");
            e.printStackTrace();
        }

        launch(args);
    }
}