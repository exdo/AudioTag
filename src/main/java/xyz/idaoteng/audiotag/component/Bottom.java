package xyz.idaoteng.audiotag.component;

import javafx.scene.Node;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;

public class Bottom {
    private static final AnchorPane ANCHOR_PANE = new AnchorPane();
    private static final TextArea TEXT_AREA = new TextArea();

    static {
        TEXT_AREA.setEditable(false);
        TEXT_AREA.setWrapText(true);

        AnchorPane.setTopAnchor(TEXT_AREA, 0.0);
        AnchorPane.setLeftAnchor(TEXT_AREA, 3.0);
        AnchorPane.setRightAnchor(TEXT_AREA, 0.0);
        AnchorPane.setBottomAnchor(TEXT_AREA, 3.0);

        ANCHOR_PANE.getChildren().add(TEXT_AREA);
        ANCHOR_PANE.setPrefWidth(1131);
        ANCHOR_PANE.setMinHeight(55);
        ANCHOR_PANE.setMaxHeight(55);
    }

    public static void print(String msg) {
        TEXT_AREA.appendText(msg + "\n");
    }

    public static Node getBottom() {
        return ANCHOR_PANE;
    }
}
