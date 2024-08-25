package xyz.idaoteng.audiotag;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import net.coobird.thumbnailator.Thumbnails;

import javax.imageio.ImageIO;
import java.io.*;

public class Utils {
    private static final ByteArrayOutputStream DEFAULT_COVER = new ByteArrayOutputStream();
    private static final ByteArrayOutputStream RENAME_ICON = new ByteArrayOutputStream();

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
        icon.setFitWidth(16);
        icon.setFitHeight(16);
        icon.setImage(new Image(new ByteArrayInputStream(RENAME_ICON.toByteArray())));
        return icon;
    }

    public static String getExtension(final File file) {
        final String name = file.getName().toLowerCase();
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
