package xyz.idaoteng.lesson1;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import xyz.idaoteng.lesson1.component.Aside;
import xyz.idaoteng.lesson1.component.Center;
import xyz.idaoteng.lesson1.component.Head;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) {
        ApplicationContext.register("stage", stage);

        BorderPane root = new BorderPane();
        root.setTop(Head.getHead());
        root.setCenter(Center.getCenter());
        root.setRight(Aside.getAside());

        Scene scene = new Scene(root, 1131, 580);
        stage.setScene(scene);
        stage.setTitle("音乐信息编辑器");
        stage.setMinHeight(300);
        stage.setMinWidth(450);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}