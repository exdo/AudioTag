package xyz.idaoteng.audiotag.dialog;

import atlantafx.base.layout.InputGroup;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import xyz.idaoteng.audiotag.Session;
import xyz.idaoteng.audiotag.UiCoordinator;
import xyz.idaoteng.audiotag.bean.AudioFileData;
import xyz.idaoteng.audiotag.constant.MusicGenre;

import java.util.ArrayList;
import java.util.List;

/**
 * Filter 类用于创建一个过滤音频元数据的对话框。
 * 允许用户根据标题、艺术家、专辑、流派和封面等条件过滤音频文件。
 */
public class Filter {
    // 定义静态变量，保证所有实例共享同一个窗口和数据
    private static final Stage STAGE = new Stage(); // 过滤窗口

    private static final CheckBox TITLE_CHECK_BOX = new CheckBox("标题：");
    private static final TextField TITLE_TEXT_FIELD = new TextField();

    private static final CheckBox ARTIST_CHECK_BOX = new CheckBox("艺术家：");
    private static final ComboBox<String> ARTIST_COMBO_BOX = new ComboBox<>();

    private static final CheckBox ALBUM_CHECK_BOX = new CheckBox("专辑：");
    private static final ComboBox<String> ALBUM_COMBO_BOX = new ComboBox<>();

    private static final CheckBox GENRE_CHECK_BOX = new CheckBox("流派：");
    private static final ComboBox<String> GENRE_COMBO_BOX = new ComboBox<>();

    private static final CheckBox COVER_CHECK_BOX = new CheckBox("封面为空");
    private static final CheckBox LYRIC_CHECK_BOX = new CheckBox("歌词为空");

    private static final HBox OK_AND_CANCEL = new HBox(); // 按钮水平布局

    private static final Font FONT = new Font(13); // 默认字体

    private static final List<AudioFileData> ALL_ITEMS = new ArrayList<>(); // 存储所有音频元数据

    private static boolean filterViewIsOn = false;

    private static int selectedCheckBox = 0;

    // 初始化UI组件和布局
    static {
        // 监听标题复选框的事件，启用/禁用标题文本框
        TITLE_CHECK_BOX.setOnAction(event -> {
            selectedCheckBox = TITLE_CHECK_BOX.isSelected() ? selectedCheckBox + 1 : selectedCheckBox - 1;
            TITLE_TEXT_FIELD.setDisable(!TITLE_CHECK_BOX.isSelected());
        });

        // 监听艺术家复选框的事件，启用/禁用艺术家下拉框
        ARTIST_CHECK_BOX.setOnAction(event -> {
            selectedCheckBox = ARTIST_CHECK_BOX.isSelected() ? selectedCheckBox + 1 : selectedCheckBox - 1;
            ARTIST_COMBO_BOX.setDisable(!ARTIST_CHECK_BOX.isSelected());
        });

        // 监听专辑复选框的事件，启用/禁用专辑下拉框
        ALBUM_CHECK_BOX.setOnAction(event -> {
            selectedCheckBox = ALBUM_CHECK_BOX.isSelected() ? selectedCheckBox + 1 : selectedCheckBox - 1;
            ALBUM_COMBO_BOX.setDisable(!ALBUM_CHECK_BOX.isSelected());
        });

        // 监听流派复选框的事件，启用/禁用流派下拉框
        GENRE_CHECK_BOX.setOnAction(event -> {
            selectedCheckBox = GENRE_CHECK_BOX.isSelected() ? selectedCheckBox + 1 : selectedCheckBox - 1;
            GENRE_COMBO_BOX.setDisable(!GENRE_CHECK_BOX.isSelected());
        });

        COVER_CHECK_BOX.setOnAction(event -> {
            if (COVER_CHECK_BOX.isSelected()) {
                selectedCheckBox++;
            } else {
                selectedCheckBox--;
            }
        });

        LYRIC_CHECK_BOX.setOnAction(event -> {
            if (LYRIC_CHECK_BOX.isSelected()) {
                selectedCheckBox++;
            } else {
                selectedCheckBox--;
            }
        });
    }

    static {
        TITLE_TEXT_FIELD.setDisable(true); // 默认禁用

        ARTIST_COMBO_BOX.setEditable(true); // 允许手动输入
        ARTIST_COMBO_BOX.setDisable(true); // 默认禁用

        ALBUM_COMBO_BOX.setEditable(true); // 允许手动输入
        ALBUM_COMBO_BOX.setDisable(true); // 默认禁用

        GENRE_COMBO_BOX.setEditable(true); // 允许手动输入
        GENRE_COMBO_BOX.setDisable(true); // 默认禁用
        GENRE_COMBO_BOX.getItems().add(""); // 添加一个空选项
        GENRE_COMBO_BOX.getItems().addAll(MusicGenre.getGenres()); // 添加所有流派到下拉框
    }

    static {
        Button confirmButton = new Button("确定"); // 确定按钮
        confirmButton.setFont(FONT);
        confirmButton.setOnAction(event -> doFilter()); // 配置确定按钮的事件处理

        Button cancelButton = new Button("取消"); // 取消按钮
        cancelButton.setFont(FONT);
        cancelButton.setOnAction(event -> {
            filterViewIsOn = false;
            STAGE.close();
        });

        OK_AND_CANCEL.getChildren().addAll(cancelButton, confirmButton);
        OK_AND_CANCEL.setSpacing(10); // 按钮间距
        OK_AND_CANCEL.setAlignment(Pos.CENTER_RIGHT); // 居右对齐
        OK_AND_CANCEL.setPadding(new Insets(15, 0, 0, 0)); // 上边距
    }

    static {
        Label label = new Label("请勾选需要参与过滤的条件"); // 提示标签
        label.setFont(FONT);

        InputGroup group1 = new InputGroup(TITLE_CHECK_BOX, TITLE_TEXT_FIELD);
        group1.setSpacing(5);
        InputGroup group2 = new InputGroup(ARTIST_CHECK_BOX, ARTIST_COMBO_BOX);
        group2.setSpacing(5);
        InputGroup group3 = new InputGroup(ALBUM_CHECK_BOX, ALBUM_COMBO_BOX);
        group3.setSpacing(5);
        InputGroup group4 = new InputGroup(GENRE_CHECK_BOX, GENRE_COMBO_BOX);
        group4.setSpacing(5);
        InputGroup group5 = new InputGroup(COVER_CHECK_BOX, LYRIC_CHECK_BOX);
        group5.setSpacing(5);
        VBox mainContent = new VBox(10); // 主内容布局，组件间距10
        mainContent.getChildren().addAll(group1, group2, group3, group4, group5);

        VBox body = new VBox(10); // 主体垂直布局，组件间距为10
        body.setPadding(new Insets(10, 15, 0, 15));
        body.getChildren().addAll(label, mainContent, OK_AND_CANCEL);

        Scene scene = new Scene(body, 450, 330); // 创建场景

        STAGE.setScene(scene); // 设置场景
        STAGE.setTitle("过滤"); // 设置标题
        STAGE.setResizable(false); // 禁止调整大小
        STAGE.initModality(Modality.APPLICATION_MODAL); // 设置为模态窗口

        STAGE.setOnCloseRequest(event -> {
            filterViewIsOn = false;
            STAGE.close();
            event.consume();
        });
    }

    /**
     * 配置确认按钮的事件处理器
     */
    private static void doFilter() {
        if (selectedCheckBox == 0) {
            filterViewIsOn = false;
            STAGE.close();
            return;
        }

        List<AudioFileData> filtered = ALL_ITEMS.stream().filter(data -> {
            if (ARTIST_CHECK_BOX.isSelected()) {
                String value = ARTIST_COMBO_BOX.getValue();
                if (value == null || value.trim().isBlank()) {
                    return "".equals(data.getArtist());
                }
                return data.getArtist().contains(value.trim());
            } else {
                return true;
            }
        }).filter(data -> {
            if (ALBUM_CHECK_BOX.isSelected()) {
                String value = ALBUM_COMBO_BOX.getValue();
                if (value == null || value.trim().isBlank()) {
                    return "".equals(data.getAlbum());
                }
                return data.getAlbum().contains(value.trim());
            } else {
                return true;
            }
        }).filter(data -> {
            if (GENRE_CHECK_BOX.isSelected()) {
                String value = GENRE_COMBO_BOX.getValue();
                if (value == null || value.trim().isBlank()) {
                    return "".equals(data.getGenre());
                }
                return data.getGenre().contains(value.trim());
            } else {
                return true;
            }
        }).filter(data -> {
            if (TITLE_CHECK_BOX.isSelected()) {
                String text = TITLE_TEXT_FIELD.getText();
                if (text == null || text.trim().isBlank()) {
                    return "".equals(data.getTitle());
                } else {
                    // 否则判断标题是否包含输入框中的值
                    return data.getTitle().contains(text);
                }
            } else {
                return true;
            }
        }).filter(data -> {
            if (COVER_CHECK_BOX.isSelected()) {
                return data.getCover() == null;
            } else {
                return true;
            }
        }).filter(data -> {
            if (LYRIC_CHECK_BOX.isSelected()) {
                return data.getLyric().trim().isBlank();
            } else {
                return true;
            }
        }).toList();

        if (filtered.isEmpty()) {
            filterViewIsOn = false;
            UiCoordinator.showNotification("没有找到任何结果");
            STAGE.close();
        } else {
            UiCoordinator.setTableViewItems(filtered); // 更新表格视图
            filterViewIsOn = true;
            STAGE.close(); // 关闭窗口
        }
    }

    /**
     * 显示过滤对话框并设置初始数据
     *
     * @param dataList 音频元数据列表
     */
    public static boolean show(List<AudioFileData> dataList) {
        filterViewIsOn = false;

        resetCheckBox();

        resetInputText();

        ALL_ITEMS.clear();
        ALL_ITEMS.addAll(dataList);

        STAGE.showAndWait();

        return filterViewIsOn;
    }

    private static void resetInputText() {
        TITLE_TEXT_FIELD.clear();

        ARTIST_COMBO_BOX.getItems().clear(); // 清空艺术家下拉框选项
        ARTIST_COMBO_BOX.getItems().add(""); // 添加空选项
        ARTIST_COMBO_BOX.getItems().addAll(Session.getAlternativeArtists()); // 添加备选艺术家

        ALBUM_COMBO_BOX.getItems().clear(); // 清空专辑下拉框选项
        ALBUM_COMBO_BOX.getItems().add(""); // 添加空选项
        ALBUM_COMBO_BOX.getItems().addAll(Session.getAlternativeAlbums()); // 添加备选专辑
    }

    private static void resetCheckBox() {
        TITLE_CHECK_BOX.setSelected(false);
        ARTIST_CHECK_BOX.setSelected(false);
        ALBUM_CHECK_BOX.setSelected(false);
        GENRE_CHECK_BOX.setSelected(false);
        COVER_CHECK_BOX.setSelected(false);
        LYRIC_CHECK_BOX.setSelected(false);

        selectedCheckBox = 0;
    }
}
