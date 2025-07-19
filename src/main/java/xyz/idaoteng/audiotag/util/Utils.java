package xyz.idaoteng.audiotag.util;

import javafx.scene.control.Alert;
import net.coobird.thumbnailator.Thumbnails;
import xyz.idaoteng.audiotag.Session;
import xyz.idaoteng.audiotag.UiCoordinator;
import xyz.idaoteng.audiotag.bean.Configuration;

import javax.imageio.ImageIO;
import java.io.*;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.List;

public class Utils {
    public static Alert errorAlert(String headText) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("错误");
        alert.setGraphic(ImageInApp.getErrorIcon());
        alert.setHeaderText(headText);
        return alert;
    }

    public static String getFilenameWithoutExtension(String filename) {
        int index = filename.lastIndexOf(".");
        if (index == -1) {
            return filename;
        } else {
            return filename.substring(0, index);
        }
    }

    public static String getExtension(String filename) {
        int index = filename.lastIndexOf(".");
        if (index == -1) {
            return "";
        } else {
            return filename.substring(index + 1);
        }
    }

    public static byte[] retouchedOrItself(File file) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Configuration config = Session.getConfig();
        try {
            if (config.getNeedRetouch()) {
                Thumbnails.of(ImageIO.read(file))
                        .forceSize(config.getWidth(), config.getHeight())
                        .outputFormat(config.getFormat())
                        .toOutputStream(outputStream);
                return outputStream.toByteArray();
            } else {
                return Files.readAllBytes(file.toPath());
            }
        } catch (IOException e) {
            UiCoordinator.showNotification("设置封面时发生错误：" + e.getMessage());
            return null;
        }
    }

    public static byte[] retouchedOrItself(byte[] cover) {
        Configuration config = Session.getConfig();
        if (config.getNeedRetouch()) {
            try {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                Thumbnails.of(new ByteArrayInputStream(cover))
                        .forceSize(360, 360)
                        .outputFormat("jpg")
                        .toOutputStream(outputStream);
                return outputStream.toByteArray();
            } catch (IOException e) {
                UiCoordinator.showNotification("设置封面时发生错误：" + e.getMessage());
                return null;
            }
        } else {
            return cover;
        }
    }

    private static final HashSet<String> SUPPORTED_IMG = new HashSet<>(List.of("jpg", "jpeg", "bmp", "png"));
    public static void saveCover(byte[] cover, File file) {
        try {
            Configuration config = Session.getConfig();

            String extension = getExtension(file.getName());
            if (!SUPPORTED_IMG.contains(extension)) {
                if (SUPPORTED_IMG.contains(config.getFormat())) {
                    extension = config.getFormat();
                } else {
                    extension = "jpg";
                }
            }
            String filename = getFilenameWithoutExtension(file.getName());
            file = new File(file.getParentFile(), filename + "." + extension);

            if (config.getNeedRetouch()) {
                Thumbnails.of(new ByteArrayInputStream(cover))
                        .size(config.getWidth(), config.getHeight())
                        .outputFormat(extension)
                        .toFile(file);
            } else {
                Thumbnails.of(new ByteArrayInputStream(cover)).scale(1.0).toFile(file);
            }
        } catch (Exception e) {
            UiCoordinator.showNotification("保存封面时发生错误：" + e.getMessage());
        }
    }
}
