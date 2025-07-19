package xyz.idaoteng.audiotag.util;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class ImageInApp {
    private static final ByteArrayOutputStream DEFAULT_COVER = new ByteArrayOutputStream();
    private static final ByteArrayOutputStream DELETE_ICON = new ByteArrayOutputStream();
    private static final ByteArrayOutputStream ERROR_ICON = new ByteArrayOutputStream();
    private static final ByteArrayOutputStream CLEAR_ICON = new ByteArrayOutputStream();
    private static final ByteArrayOutputStream ALBUM_ICON = new ByteArrayOutputStream();
    private static final ByteArrayOutputStream APP_ICON = new ByteArrayOutputStream();
    private static final ByteArrayOutputStream LYRIC_ICON = new ByteArrayOutputStream();
    private static final ByteArrayOutputStream SEARCH_EMPTY_ICON = new ByteArrayOutputStream();

    static {
        try (InputStream inputStream = ImageInApp.class.getResourceAsStream("cover.png")) {
            if (inputStream == null) throw new RuntimeException("无法加载默认封面图片");
            inputStream.transferTo(DEFAULT_COVER);
        } catch (Exception e) {
            throw new RuntimeException("无法加载默认封面图片");
        }

        try (InputStream inputStream = ImageInApp.class.getResourceAsStream("delete.png")) {
            if (inputStream == null) throw new RuntimeException("无法加载删除图标");
            inputStream.transferTo(DELETE_ICON);
        } catch (Exception e) {
            throw new RuntimeException("无法加载删除图标");
        }

        try (InputStream inputStream = ImageInApp.class.getResourceAsStream("error.png")) {
            if (inputStream == null) throw new RuntimeException("无法加载错误图标");
            inputStream.transferTo(ERROR_ICON);
        } catch (Exception e) {
            throw new RuntimeException("无法加载错误图标");
        }

        try (InputStream inputStream = ImageInApp.class.getResourceAsStream("clear.png")) {
            if (inputStream == null) throw new RuntimeException("无法加载清空图标");
            inputStream.transferTo(CLEAR_ICON);
        } catch (Exception e) {
            throw new RuntimeException("无法加载清空图标");
        }

        try (InputStream inputStream = ImageInApp.class.getResourceAsStream("album.png")) {
            if (inputStream == null) throw new RuntimeException("无法加载专辑图标");
            inputStream.transferTo(ALBUM_ICON);
        } catch (Exception e) {
            throw new RuntimeException("无法加载专辑图标");
        }

        try (InputStream inputStream = ImageInApp.class.getResourceAsStream("app.png")) {
            if (inputStream == null) throw new RuntimeException("无法加载应用图标");
            inputStream.transferTo(APP_ICON);
        } catch (Exception e) {
            throw new RuntimeException("无法加载应用图标");
        }

        try (InputStream inputStream = ImageInApp.class.getResourceAsStream("lyric.png")) {
            if (inputStream == null) throw new RuntimeException("无法加载歌词图标");
            inputStream.transferTo(LYRIC_ICON);
        } catch (Exception e) {
            throw new RuntimeException("无法加载歌词图标");
        }

        try (InputStream inputStream = ImageInApp.class.getResourceAsStream("search-empty.png")) {
            if (inputStream == null) throw new RuntimeException("无法加载搜索为空图标");
            inputStream.transferTo(LYRIC_ICON);
        } catch (Exception e) {
            throw new RuntimeException("无法加载搜索为空图标");
        }
    }

    public static ImageView getDefaultCover() {
        ImageView cover = new ImageView();
        cover.setFitWidth(90);
        cover.setFitHeight(90);
        cover.setImage(new Image(new ByteArrayInputStream(DEFAULT_COVER.toByteArray())));
        return cover;
    }

    public static ImageView getDeleteIcon() {
        ImageView icon = new ImageView();
        icon.setFitWidth(35);
        icon.setFitHeight(35);
        icon.setImage(new Image(new ByteArrayInputStream(DELETE_ICON.toByteArray())));
        return icon;
    }

    public static ImageView getErrorIcon() {
        ImageView icon = new ImageView();
        icon.setFitWidth(35);
        icon.setFitHeight(35);
        icon.setImage(new Image(new ByteArrayInputStream(ERROR_ICON.toByteArray())));
        return icon;
    }

    public static ImageView getClearIcon() {
        ImageView icon = new ImageView();
        icon.setFitWidth(20);
        icon.setFitHeight(20);
        icon.setImage(new Image(new ByteArrayInputStream(CLEAR_ICON.toByteArray())));
        return icon;
    }

    public static ImageView getAlbumIcon() {
        ImageView icon = new ImageView();
        icon.setFitWidth(35);
        icon.setFitHeight(35);
        icon.setImage(new Image(new ByteArrayInputStream(ALBUM_ICON.toByteArray())));
        return icon;
    }

    public static ImageView getLyricIcon() {
        ImageView icon = new ImageView();
        icon.setFitWidth(20);
        icon.setFitHeight(20);
        icon.setImage(new Image(new ByteArrayInputStream(LYRIC_ICON.toByteArray())));
        return icon;
    }

    public static ImageView getSearchEmptyIcon() {
        ImageView icon = new ImageView();
        icon.setFitWidth(35);
        icon.setFitHeight(35);
        icon.setImage(new Image(new ByteArrayInputStream(SEARCH_EMPTY_ICON.toByteArray())));
        return icon;
    }

    public static Image getAppIcon() {
        return new Image(new ByteArrayInputStream(APP_ICON.toByteArray()));
    }
}
