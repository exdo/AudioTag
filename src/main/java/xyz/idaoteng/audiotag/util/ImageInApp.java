package xyz.idaoteng.audiotag.util;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.InputStream;

public class ImageInApp {
    private static Image DEFAULT_COVER;
    private static Image DELETE_ICON;
    private static Image ERROR_ICON;
    private static Image CLEAR_ICON;
    private static Image ALBUM_ICON;
    private static Image APP_ICON;
    private static Image LYRIC_ICON;
    private static Image SEARCH_EMPTY_ICON;

    public static void loadAllImage() {
        try (InputStream inputStream = ImageInApp.class.getResourceAsStream("cover.png")) {
            if (inputStream == null) throw new RuntimeException("无法加载默认封面图片");
            DEFAULT_COVER = new Image(inputStream);
        } catch (Exception e) {
            throw new RuntimeException("无法加载默认封面图片");
        }

        try (InputStream inputStream = ImageInApp.class.getResourceAsStream("delete.png")) {
            if (inputStream == null) throw new RuntimeException("无法加载删除图标");
            DELETE_ICON = new Image(inputStream);
        } catch (Exception e) {
            throw new RuntimeException("无法加载删除图标");
        }

        try (InputStream inputStream = ImageInApp.class.getResourceAsStream("error.png")) {
            if (inputStream == null) throw new RuntimeException("无法加载错误图标");
            ERROR_ICON = new Image(inputStream);
        } catch (Exception e) {
            throw new RuntimeException("无法加载错误图标");
        }

        try (InputStream inputStream = ImageInApp.class.getResourceAsStream("clear.png")) {
            if (inputStream == null) throw new RuntimeException("无法加载清空图标");
            CLEAR_ICON = new Image(inputStream);
        } catch (Exception e) {
            throw new RuntimeException("无法加载清空图标");
        }

        try (InputStream inputStream = ImageInApp.class.getResourceAsStream("album.png")) {
            if (inputStream == null) throw new RuntimeException("无法加载专辑图标");
            ALBUM_ICON = new Image(inputStream);
        } catch (Exception e) {
            throw new RuntimeException("无法加载专辑图标");
        }

        try (InputStream inputStream = ImageInApp.class.getResourceAsStream("app.png")) {
            if (inputStream == null) throw new RuntimeException("无法加载应用图标");
            APP_ICON = new Image(inputStream);
        } catch (Exception e) {
            throw new RuntimeException("无法加载应用图标");
        }

        try (InputStream inputStream = ImageInApp.class.getResourceAsStream("lyric.png")) {
            if (inputStream == null) throw new RuntimeException("无法加载歌词图标");
            LYRIC_ICON = new Image(inputStream);
        } catch (Exception e) {
            throw new RuntimeException("无法加载歌词图标");
        }

        try (InputStream inputStream = ImageInApp.class.getResourceAsStream("search-empty.png")) {
            if (inputStream == null) throw new RuntimeException("无法加载搜索为空图标");
            SEARCH_EMPTY_ICON = new Image(inputStream);
        } catch (Exception e) {
            throw new RuntimeException("无法加载搜索为空图标");
        }
    }

    public static ImageView getDefaultCover() {
        ImageView cover = new ImageView();
        cover.setFitWidth(90);
        cover.setFitHeight(90);
        cover.setImage(DEFAULT_COVER);
        return cover;
    }

    public static ImageView getDeleteIcon() {
        ImageView icon = new ImageView();
        icon.setFitWidth(35);
        icon.setFitHeight(35);
        icon.setImage(DELETE_ICON);
        return icon;
    }

    public static ImageView getErrorIcon() {
        ImageView icon = new ImageView();
        icon.setFitWidth(35);
        icon.setFitHeight(35);
        icon.setImage(ERROR_ICON);
        return icon;
    }

    public static ImageView getClearIcon() {
        ImageView icon = new ImageView();
        icon.setFitWidth(20);
        icon.setFitHeight(20);
        icon.setImage(CLEAR_ICON);
        return icon;
    }

    public static ImageView getAlbumIcon() {
        ImageView icon = new ImageView();
        icon.setFitWidth(35);
        icon.setFitHeight(35);
        icon.setImage(ALBUM_ICON);
        return icon;
    }

    public static ImageView getLyricIcon() {
        ImageView icon = new ImageView();
        icon.setFitWidth(20);
        icon.setFitHeight(20);
        icon.setImage(LYRIC_ICON);
        return icon;
    }

    public static ImageView getSearchEmptyIcon() {
        ImageView icon = new ImageView();
        icon.setFitWidth(35);
        icon.setFitHeight(35);
        icon.setImage(SEARCH_EMPTY_ICON);
        return icon;
    }

    public static Image getAppIcon() {
        return APP_ICON;
    }
}
