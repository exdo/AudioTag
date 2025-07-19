package xyz.idaoteng.audiotag.component;

import atlantafx.base.controls.Notification;
import atlantafx.base.theme.Styles;
import javafx.animation.PauseTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

public class Message {
    private final Notification notification;

    public Message() {
        Notification notification = new Notification();
        notification.setVisible(false);
        notification.setMaxHeight(50);
        notification.getStyleClass().addAll(Styles.ELEVATED_1, Styles.ACCENT);
        StackPane.setAlignment(notification, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(notification, new Insets(0, 10, 10, 0));
        this.notification = notification;
    }

    public Notification getNode() {
        return notification;
    }

    public void showMessage(String message) {
        notification.setMessage(message);
        notification.setVisible(true);
        PauseTransition pauseTransition = new PauseTransition(Duration.seconds(2));
        pauseTransition.setOnFinished(event -> notification.setVisible(false));
        pauseTransition.play();
    }
}
