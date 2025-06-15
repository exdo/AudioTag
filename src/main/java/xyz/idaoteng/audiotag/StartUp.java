package xyz.idaoteng.audiotag;

import atlantafx.base.theme.PrimerLight;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import xyz.idaoteng.audiotag.component.Aside;
import xyz.idaoteng.audiotag.component.Center;
import xyz.idaoteng.audiotag.component.Head;
import xyz.idaoteng.audiotag.component.Modal;
import xyz.idaoteng.audiotag.core.TagOption;
import xyz.idaoteng.audiotag.exception.PreferencesError;

import java.io.InputStream;
import java.util.logging.LogManager;

public class StartUp extends Application {
    private static Stage stage;

    @Override
    public void start(Stage primaryStage) {
        initSession(primaryStage);

        StartUp.stage = primaryStage;

        StackPane root = new StackPane();

        BorderPane borderPane = new BorderPane();
        borderPane.setCenter(Center.getCenter());
        borderPane.setTop(Head.getHead());
        borderPane.setRight(Aside.getAside());

        Node modal = Modal.getModal();
        root.getChildren().addAll(borderPane, modal);
        StackPane.setAlignment(modal, Pos.CENTER);

        Scene scene = new Scene(root, 1200, 725);
        primaryStage.setScene(scene);
        primaryStage.setTitle("音乐信息编辑器");
        primaryStage.getIcons().add(ImageInApp.getAppIcon());
        primaryStage.setMinHeight(borderPane.getHeight());
        primaryStage.setMinWidth(1000);
        primaryStage.show();

        Center.configWhenTableAlreadyRendered();
    }

    /**
     * 初始化Session
     */
    private void initSession(Stage primaryStage) {
        try {
            Session.init();
        } catch (PreferencesError e) {
            Scene scene = new Scene(new StackPane());
            primaryStage.setScene(scene);
            primaryStage.initStyle(StageStyle.TRANSPARENT);
            primaryStage.show();
            Alert alert = Utils.generateBasicErrorAlert("程序运行时出现错误");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
            System.exit(555);
        }
    }

    @Override
    public void stop() {
        // 保存Session
        Session.saveSession();
    }

    /**
     * 获取主窗口
     */
    public static Stage getPrimaryStage() {
        return stage;
    }

    public static void main(String[] args) {
        // 禁用Jaudiotagger的日志
        turnOffJaudiotaggerLog();

        // 配置 Jaudiotagger 库的选项
        TagOption.setupOptions();

        // 设置主题
        Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());

        launch(args);
    }

    /**
     * 禁用Jaudiotagger的日志
     */
    private static void turnOffJaudiotaggerLog() {
        LogManager logManager = LogManager.getLogManager();
        try (InputStream input = StartUp.class.getResourceAsStream("log.properties")) {
            logManager.readConfiguration(input);
        } catch (Exception e) {
            System.out.println("读取日志配置失败");
            e.printStackTrace();
        }
    }
}