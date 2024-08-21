package xyz.idaoteng.audiotag;

import net.coobird.thumbnailator.Thumbnails;

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

public class Utils {
    public static String getExtension(final File f) {
        final String name = f.getName().toLowerCase();
        final int i = name.lastIndexOf(".");
        if (i == -1) {
            return "";
        }

        return name.substring(i + 1);
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
