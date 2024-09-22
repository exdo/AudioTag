package xyz.idaoteng.audiotag.notification;

import javafx.animation.PauseTransition;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import xyz.idaoteng.audiotag.StartUp;

public class Notification {
    private static final Stage STAGE = new Stage();
    private static final String Label_STYLE = "-fx-background-color: white; -fx-text-fill: #00fa9a; -fx-padding: 10;";
    private static final StackPane PANE = new StackPane();
    private static final Scene SCENE = new Scene(PANE);

    static {
        STAGE.initStyle(StageStyle.TRANSPARENT);
        STAGE.setScene(SCENE);
        PANE.setMaxHeight(35);
        PANE.setMinHeight(35);
    }

    public static void showNotification(String message) {
        Label notificationLabel = new Label(message);
        notificationLabel.setStyle(Label_STYLE);

        PANE.getChildren().clear();
        PANE.getChildren().add(notificationLabel);

        Stage primaryStage = StartUp.getPrimaryStage();
        STAGE.show();
        STAGE.setY(primaryStage.getY() + primaryStage.getHeight() - 42.5);
        STAGE.setX(primaryStage.getX() + primaryStage.getWidth() - PANE.getWidth() - 7);

        PauseTransition pause = new PauseTransition(Duration.seconds(2.5));
        pause.setOnFinished(event -> STAGE.close());
        pause.play();
    }
}
