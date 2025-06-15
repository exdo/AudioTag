package xyz.idaoteng.audiotag;

import javafx.scene.control.Alert;
import net.coobird.thumbnailator.Thumbnails;

import javax.imageio.ImageIO;
import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class Utils {
    public static String getPreferencesFilePathInRegistry() {
        String result = "";
        try {
            String regKey = "HKEY_LOCAL_MACHINE\\SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\App Paths\\AudioTag.exe";
            String itemName = "install_dir";

            Process process = Runtime.getRuntime().exec("reg query \"" + regKey + "\" /v " + itemName);

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), Charset.forName("GBK")))
            ) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.trim().startsWith(itemName)) {
                        String[] parts = line.trim().split("\\s+", 3);
                        if (parts.length == 3) {
                            result = parts[2];
                        }
                    }
                }
            }

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                return result;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

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
            if (Session.needRetouchCover()) {
                Thumbnails.of(ImageIO.read(file))
                        .forceSize(360, 360)
                        .outputFormat("jpg")
                        .toOutputStream(outputStream);
            } else {
                Thumbnails.of(ImageIO.read(file)).toOutputStream(outputStream);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return outputStream.toByteArray();
    }

    public static void saveCover(byte[] cover, File file) {
        try {
            if (Session.needRetouchCover()) {
                Thumbnails.of(new ByteArrayInputStream(cover)).size(360, 360).toFile(file);
            } else {
                Thumbnails.of(new ByteArrayInputStream(cover)).toFile(file);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] retouchCover(byte[] cover) {
        if (!Session.needRetouchCover()) return cover;

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            Thumbnails.of(new ByteArrayInputStream(cover))
                    .forceSize(360, 360)
                    .outputFormat("jpg")
                    .toOutputStream(outputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return outputStream.toByteArray();
    }

    public static String encodeKeyword(String original) {
        if (original == null || original.trim().equals("")) {
            return original;
        }

        return URLEncoder.encode(original, StandardCharsets.UTF_8);
    }
}
