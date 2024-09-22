package xyz.idaoteng.audiotag.dialog;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import xyz.idaoteng.audiotag.ImageInApp;
import xyz.idaoteng.audiotag.Utils;
import xyz.idaoteng.audiotag.api.Api;
import xyz.idaoteng.audiotag.api.TimelessApi;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Optional;

public class SelectCover {
    private static final Dialog<byte[]> DIALOG = new Dialog<>();
    private static final ToggleGroup TOGGLE_GROUP = new ToggleGroup();
    private static final String TITLE = "选择封面";
    private static final VBox BODY = new VBox(10);
    private static final Api API = new TimelessApi();
    
    static {
        DIALOG.setTitle(TITLE);
    }
    
    public static byte[] show(String title, String artist, String album) {
        List<byte[]> coves = API.getCover(title, artist, album);
        if (coves == null) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("很遗憾");
            alert.setGraphic(ImageInApp.getSorryIcon());
            alert.setHeaderText("搜索结果为空");
            alert.show();
            return null;
        }

        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(10));
        gridPane.setHgap(5);
        gridPane.setVgap(5);

        for (int i = 0; i < coves.size(); i++) {
            Node node = generateAlternativeImageNode(coves.get(i), i);
            gridPane.add(node, i % 5, i / 5);
        }

        Button okButton = new Button("确定");
        okButton.setOnAction(e -> {
            RadioButton selectedRadioButton = (RadioButton) TOGGLE_GROUP.getSelectedToggle();
            if (selectedRadioButton != null) {
                byte[] selectedImage = coves.get(Integer.parseInt(selectedRadioButton.getId()));
                DIALOG.setResult(selectedImage);
                DIALOG.close();
            } else {
                DIALOG.setResult(new byte[0]);
                DIALOG.close();
            }
        });

        Button cancelButton = new Button("取消");
        cancelButton.setOnAction(e -> {
            DIALOG.setResult(new byte[0]);
            DIALOG.close();
        });
        cancelButton.requestFocus();

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.getChildren().addAll(okButton, cancelButton);

        BODY.getChildren().clear();
        BODY.getChildren().addAll(gridPane, buttonBox);

        DIALOG.getDialogPane().setContent(BODY);
        Optional<byte[]> result = DIALOG.showAndWait();
        if (result.isPresent() && result.get().length > 0) {
            return Utils.retouchCover(result.get());
        }
        return null;
    }

    private static Node generateAlternativeImageNode(byte[] cove, int index) {
        AnchorPane pane = new AnchorPane();
        pane.setMinHeight(150);
        pane.setMaxHeight(150);
        pane.setMinWidth(150);
        pane.setMaxWidth(150);

        ImageView imageView = new ImageView();
        imageView.setFitHeight(150);
        imageView.setFitWidth(150);
        imageView.setImage(new Image(new ByteArrayInputStream(cove)));
        AnchorPane.setTopAnchor(imageView, 0.0);
        AnchorPane.setBottomAnchor(imageView, 0.0);
        AnchorPane.setLeftAnchor(imageView, 0.0);
        AnchorPane.setRightAnchor(imageView, 0.0);

        RadioButton radioButton = new RadioButton();
        radioButton.setId(String.valueOf(index));
        radioButton.setToggleGroup(TOGGLE_GROUP);
        AnchorPane.setTopAnchor(radioButton, 0.0);
        AnchorPane.setRightAnchor(radioButton, 0.0);

        pane.setOnMouseClicked(event -> TOGGLE_GROUP.selectToggle(radioButton));
        pane.getChildren().addAll(imageView, radioButton);
        return pane;
    }
}
