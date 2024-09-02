package xyz.idaoteng.audiotag.dialog;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import xyz.idaoteng.audiotag.ImageInApp;
import xyz.idaoteng.audiotag.component.Center;

import java.util.Optional;

public class NeedToSelectAll {
    public static boolean isSelectAll() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setGraphic(ImageInApp.getSelectAllIcon());
        alert.setTitle("未选择任何条目");
        alert.setHeaderText("未选择任何条目, 是否全选？");
        Optional<ButtonType> buttonType = alert.showAndWait();
        if (buttonType.isPresent() && buttonType.get() == ButtonType.OK) {
            Center.selectAll();
            return true;
        } else {
            return false;
        }
    }
}
