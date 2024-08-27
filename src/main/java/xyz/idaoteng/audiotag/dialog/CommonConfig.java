package xyz.idaoteng.audiotag.dialog;

import javafx.scene.control.Button;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import xyz.idaoteng.audiotag.ImageInApp;


public class CommonConfig {
    public static HBox packageIntoHBox(TextField textField, MenuButton menuButton) {
        HBox hBox = new HBox();

        textField.setMinWidth(350);
        textField.setMaxHeight(25);
        textField.setMinHeight(25);
        textField.setText("`artist` - `title`");

        Button clear = new Button("", ImageInApp.getClearIcon());
        clear.setMaxWidth(25);
        clear.setMinWidth(25);
        clear.setMaxHeight(25);
        clear.setStyle("-fx-background-color: transparent; -fx-border-style: none;");
        clear.setVisible(true);
        clear.setOnAction(event -> {
            textField.setText("");
            textField.requestFocus();
        });

        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            clear.setVisible(observable.getValue().length() > 0);
        });

        AnchorPane textFieldAndClear = new AnchorPane();
        textFieldAndClear.setMaxWidth(350);
        textFieldAndClear.setMinWidth(350);
        AnchorPane.setTopAnchor(textField, 0.0);
        AnchorPane.setRightAnchor(clear, 0.0);
        textFieldAndClear.getChildren().addAll(textField, clear);

        menuButton.setMinHeight(25);
        menuButton.setMaxHeight(25);

        hBox.setSpacing(5);
        hBox.getChildren().addAll(textFieldAndClear, menuButton);
        return hBox;
    }

    public static void configMenuButton(TextField textField, MenuButton menuButton) {
        MenuItem artist = new MenuItem("艺术家");
        artist.setOnAction(event -> textField.setText(textField.getText() + "`artist`"));
        MenuItem title = new MenuItem("标题");
        title.setOnAction(event -> textField.setText(textField.getText() + "`title`"));
        MenuItem album = new MenuItem("专辑");
        album.setOnAction(event -> textField.setText(textField.getText() + "`album`"));
        MenuItem date = new MenuItem("出版日期");
        date.setOnAction(event -> textField.setText(textField.getText() + "`date`"));
        MenuItem trackNumber = new MenuItem("音轨序号");
        trackNumber.setOnAction(event -> textField.setText(textField.getText() + "`track`"));
        menuButton.getItems().addAll(artist, title, album, date, trackNumber);
    }
}
