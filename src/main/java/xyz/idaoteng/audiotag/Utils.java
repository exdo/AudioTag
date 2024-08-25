package xyz.idaoteng.audiotag;

import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import net.coobird.thumbnailator.Thumbnails;

import javax.imageio.ImageIO;
import java.io.*;

public class Utils {
    private static final ByteArrayOutputStream DEFAULT_COVER = new ByteArrayOutputStream();
    private static final ByteArrayOutputStream RENAME_ICON = new ByteArrayOutputStream();
    private static final ByteArrayOutputStream DELETE_ICON = new ByteArrayOutputStream();
    private static final ByteArrayOutputStream ERROR_ICON = new ByteArrayOutputStream();

    static {
        // 将默认封面图片加载到内存中
        try (InputStream inputStream = Utils.class.getResourceAsStream("cover.png")) {
            if (inputStream == null) throw new RuntimeException("无法加载默认封面图片");
            inputStream.transferTo(DEFAULT_COVER);
        } catch (Exception e) {
            throw new RuntimeException("无法加载默认封面图片");
        }

        // 将重命名图标加载到内存中
        try (InputStream inputStream = Utils.class.getResourceAsStream("rename.png")) {
            if (inputStream == null) throw new RuntimeException("无法加载重命名图标");
            inputStream.transferTo(RENAME_ICON);
        } catch (Exception e) {
            throw new RuntimeException("无法加载重命名图标");
        }

        // 将删除图标加载到内存中
        try (InputStream inputStream = Utils.class.getResourceAsStream("delete.png")) {
            if (inputStream == null) throw new RuntimeException("无法加载删除图标");
            inputStream.transferTo(DELETE_ICON);
        } catch (Exception e) {
            throw new RuntimeException("无法加载删除图标");
        }

        // 将错误图标加载到内存中
        try (InputStream inputStream = Utils.class.getResourceAsStream("error.png")) {
            if (inputStream == null) throw new RuntimeException("无法加载错误图标");
            inputStream.transferTo(ERROR_ICON);
        } catch (Exception e) {
            throw new RuntimeException("无法加载错误图标");
        }
    }

    public static ImageView getDefaultCover() {
        ImageView cover = new ImageView();
        cover.setFitWidth(90);
        cover.setFitHeight(90);
        cover.setImage(new Image(new ByteArrayInputStream(DEFAULT_COVER.toByteArray())));
        return cover;
    }

    public static ImageView getRenameIcon() {
        ImageView icon = new ImageView();
        icon.setFitWidth(30);
        icon.setFitHeight(30);
        icon.setImage(new Image(new ByteArrayInputStream(RENAME_ICON.toByteArray())));
        return icon;
    }

    public static ImageView getDeleteIcon() {
        ImageView icon = new ImageView();
        icon.setFitWidth(35);
        icon.setFitHeight(35);
        icon.setImage(new Image(new ByteArrayInputStream(DELETE_ICON.toByteArray())));
        return icon;
    }

    public static Alert generateBasicErrorAlert(String headText) {
        ImageView icon = new ImageView();
        icon.setFitWidth(35);
        icon.setFitHeight(35);
        icon.setImage(new Image(new ByteArrayInputStream(ERROR_ICON.toByteArray())));

        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("错误");
        alert.setGraphic(icon);
        alert.setHeaderText(headText);
        return alert;
    }

    public static String getExtension(File file) {
        String name = file.getName().toLowerCase();
        int i = name.lastIndexOf('.');
        if (i == -1) {
            return "";
        }

        return name.substring(i + 1);
    }

    public static String getFilenameWithoutExtension(final File file) {
        int i = file.getName().lastIndexOf('.');
        return i == -1 ? file.getName() : file.getName().substring(0, i);
    }

    public static String secondsToMinutes(int seconds) {
        int minutes = seconds / 60;
        int remainingSeconds = seconds % 60;
        return String.format("%02d:%02d", minutes, remainingSeconds);
    }

    public static byte[] retouchCover(File file) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            Thumbnails.of(ImageIO.read(file))
                    .forceSize(360, 360)
                    .outputFormat("jpg")
                    .toOutputStream(outputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return outputStream.toByteArray();
    }

    public static void saveCover(byte[] cover, File file) {
        try {
            Thumbnails.of(new ByteArrayInputStream(cover)).size(360, 360).toFile(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
