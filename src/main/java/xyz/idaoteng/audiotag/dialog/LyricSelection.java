package xyz.idaoteng.audiotag.dialog;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Optional;

public class LyricSelection extends Dialog<String> {
    private final List<String> allLyricVersions;
    private int currentIndex;

    private final TextArea lyricDisplayArea;
    private final Label indexLabel;
    private final Button prevButton;
    private final Button nextButton;
    private final ButtonType applyButtonType;

    /**
     * 构造函数，创建歌词版本选择对话框。
     *
     * @param lyricVersions 待选择的歌词版本列表
     */
    public LyricSelection(List<String> lyricVersions) {
        this.allLyricVersions = lyricVersions;
        this.currentIndex = 0; // 默认从第一个版本开始

        // 设置对话框标题和头部文本
        setTitle("选择歌词版本");
        setHeaderText("请选择一个歌词版本：");

        // --- UI 组件初始化 ---

        // 歌词显示区域
        lyricDisplayArea = new TextArea();
        lyricDisplayArea.setEditable(false); // 不可编辑
        lyricDisplayArea.setPrefSize(450, 600);

        // 索引标签 (例如 "1/5")
        indexLabel = new Label();
        indexLabel.setStyle("-fx-font-weight: bold;");

        // 导航按钮
        prevButton = new Button("上一版本");
        nextButton = new Button("下一版本");

        // 导航按钮事件处理
        prevButton.setOnAction(e -> {
            if (currentIndex > 0) {
                currentIndex--;
                updateDisplay();
            }
        });
        nextButton.setOnAction(e -> {
            if (currentIndex < allLyricVersions.size() - 1) {
                currentIndex++;
                updateDisplay();
            }
        });

        // --- 布局 ---

        // 导航控制区域 (上一版本、索引、下一版本)
        HBox navControlBox = new HBox(15);
        navControlBox.setAlignment(Pos.CENTER);
        navControlBox.getChildren().addAll(prevButton, indexLabel, nextButton);

        // 主内容区域
        VBox content = new VBox(5);
        content.setPadding(new Insets(10));
        content.getChildren().addAll(navControlBox, lyricDisplayArea);

        // 将内容设置到 DialogPane
        getDialogPane().setContent(content);

        // --- 对话框按钮 ---
        applyButtonType = new ButtonType("选择", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("取消", ButtonBar.ButtonData.CANCEL_CLOSE);
        getDialogPane().getButtonTypes().addAll(applyButtonType, cancelButtonType);

        // 初始更新显示和按钮状态
        updateDisplay();

        // 设置结果转换器
        setResultConverter(dialogButton -> {
            if (dialogButton == applyButtonType) {
                // 如果点击了“应用”，返回当前选中的歌词版本
                if (!allLyricVersions.isEmpty()) {
                    return allLyricVersions.get(currentIndex);
                }
            }
            // 否则（点击了“取消”或关闭），返回 null
            return null;
        });
    }

    /**
     * 更新歌词显示区域和导航按钮的状态。
     */
    private void updateDisplay() {
        if (allLyricVersions.isEmpty()) {
            lyricDisplayArea.setText("没有可用的歌词版本。");
            indexLabel.setText("0/0");
            prevButton.setDisable(true);
            nextButton.setDisable(true);
            return;
        }

        // 确保 currentIndex 在有效范围内
        if (currentIndex < 0) currentIndex = 0;
        if (currentIndex >= allLyricVersions.size()) currentIndex = allLyricVersions.size() - 1;

        // 获取当前歌词版本并显示
        String currentVersion = allLyricVersions.get(currentIndex);
        lyricDisplayArea.setText(currentVersion);

        // 更新索引标签
        indexLabel.setText(String.format("%d/%d", currentIndex + 1, allLyricVersions.size()));

        // 更新导航按钮状态
        prevButton.setDisable(currentIndex == 0);
        nextButton.setDisable(currentIndex == allLyricVersions.size() - 1);
    }

    /**
     * 显示对话框并获取用户选择的歌词版本。
     *
     * @return 一个 Optional<String>，如果用户选择了歌词版本则包含该版本，否则为空。
     */
    public Optional<String> showAndGetSelection() {
        return showAndWait();
    }
}
