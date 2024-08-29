package xyz.idaoteng.audiotag;

import javafx.scene.control.Alert;
import net.coobird.thumbnailator.Thumbnails;

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

public class Utils {
    public static Alert generateBasicErrorAlert(String headText) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("错误");
        alert.setGraphic(ImageInApp.getErrorIcon());
        alert.setHeaderText(headText);
        return alert;
    }

    public static String getExtension(File file) {
        String name = file.getName().toLowerCase();
        int i = name.lastIndexOf('.');
        if (i == -1) {
            return "";
        } else {
            return name.substring(i + 1);
        }
    }

    public static String getFilenameWithoutExtension(String filename) {
        int i = filename.lastIndexOf('.');
        if (i == -1) {
            return filename;
        } else {
            return filename.substring(0, i);
        }
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
