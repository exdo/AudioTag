package xyz.idaoteng.audiotag.component;

import atlantafx.base.controls.ModalPane;
import javafx.scene.Node;

public class Modal {
    private static final ModalPane MODAL_PANE = new ModalPane();

    public static Node getModal() {
        return MODAL_PANE;
    }

    public static void show(Node node) {
        MODAL_PANE.show(node);
        MODAL_PANE.requestFocus();
    }
}
