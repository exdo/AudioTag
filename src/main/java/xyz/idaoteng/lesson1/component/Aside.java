package xyz.idaoteng.lesson1.component;

import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

public class Aside {
    private static final VBox aside = new VBox(10);

    static {
        init();
    }

    public static void init() {
        aside.setMinWidth(250);
        aside.setStyle("-fx-border-style: solid; -fx-border-color: #cccccc; -fx-border-width: 1 0 0 1");
        Text text2 = new Text("测试右边");
        aside.getChildren().add(text2);
    }

    public static VBox getAside() {
        return aside;
    }
}
