package xyz.idaoteng.audiotag.component;

import atlantafx.base.controls.ModalPane;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;

public class Modal {
    private final ModalPane modalPane = new ModalPane();

    public Modal() {
        StackPane.setAlignment(modalPane, Pos.CENTER);
    }

    public Node getNode() {
        return modalPane;
    }

    public void show(Node node) {
        modalPane.show(node);
        modalPane.requestFocus();
    }
}
