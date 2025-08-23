package xyz.idaoteng.audiotag;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import xyz.idaoteng.audiotag.bean.AudioFileData;
import xyz.idaoteng.audiotag.bean.AppConfiguration;
import xyz.idaoteng.audiotag.bean.HistorySession;
import xyz.idaoteng.audiotag.constant.ColumnsDefaultStatus;
import xyz.idaoteng.audiotag.constant.DaemonExecutor;
import xyz.idaoteng.audiotag.exception.FileCreationException;
import xyz.idaoteng.audiotag.util.InstallLocationFinder;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class Session {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    // 历史会话文件的路径
    private static String historySessionFilePath;

    private static final File DEFAULT_FOLDER = new File(System.getProperty("user.home"));

    // 上次打开的文件夹
    private static File lastSelectedFolder = DEFAULT_FOLDER;
    // 上次选择的图片所在的文件夹
    private static File lastSelectedImageFolder = DEFAULT_FOLDER;
    // 上次的图片保存路径
    private static File lastImageSavingPath = DEFAULT_FOLDER;
    // 当前表格中显示的条目的文件路径
    private static final ArrayList<String> openedPaths = new ArrayList<>();
    private static HashMap<Integer, String> columnsOrder = ColumnsDefaultStatus.order();
    private static HashMap<String, Boolean> columnsVisibleStatus = ColumnsDefaultStatus.visible();

    private static AppConfiguration cfg;
    private static String configFilePath;

    public static void init() throws FileCreationException {
        // 读取注册表中记录的软件安装目录，
        String installDir = InstallLocationFinder.getInstallDir();

        if (installDir == null) {
            // 开发环境不从注册表中读取，而是从系统临时目录中读取
            String systemTempDir = System.getProperty("java.io.tmpdir");
            File projectTempDir = new File(systemTempDir, "audio_tag_editor");
            if (projectTempDir.exists() || projectTempDir.mkdir()) {
                readHistorySessionAndSetItToNow(projectTempDir.getAbsolutePath());
                readConfiguration(projectTempDir.getAbsolutePath());
            } else {
                throw new FileCreationException("无法创建临时目录");
            }
        } else {
            readHistorySessionAndSetItToNow(installDir);
            readConfiguration(installDir);
        }
    }

    private static void readConfiguration(String path) throws FileCreationException {
        File file = new File(path, "config.json");
        if (file.exists()) {
            try (FileReader reader = new FileReader(file, StandardCharsets.UTF_8)) {
                cfg = GSON.fromJson(reader, AppConfiguration.class);
            } catch (IOException e) {
                throw new FileCreationException("无法读取配置文件：\n" + e.getMessage());
            }
        } else {
            try {
                if (file.createNewFile()) {
                    cfg = new AppConfiguration();
                } else {
                    throw new FileCreationException("无法创建配置文件");
                }
            } catch (IOException e) {
                throw new FileCreationException("无法创建配置文件：" + e.getMessage());
            }
        }
        configFilePath = file.getAbsolutePath();
    }

    private static void readHistorySessionAndSetItToNow(String path) throws FileCreationException {
        File file = new File(path, "history.json");
        if (file.exists()) {
            try (FileReader reader = new FileReader(file, StandardCharsets.UTF_8)) {
                HistorySession history = GSON.fromJson(reader, HistorySession.class);
                lastSelectedFolder = currentOrDefault(history.getLastSelectedFolder());
                lastSelectedImageFolder = currentOrDefault(history.getLastSelectedImageFolder());
                lastImageSavingPath = currentOrDefault(history.getLastImageSavingPath());
                openedPaths.addAll(history.getLastOpenedPaths());
                columnsOrder = history.getColumnsOrder();
                columnsVisibleStatus = history.getColumnsVisibleStatus();
            } catch (IOException e) {
                throw new FileCreationException("无法读取历史会话文件：\n" + e.getMessage());
            }
        } else { // 如果文件不存在（一般见于程序第一次运行时），
            try {
                // 尝试创建该文件，
                // 程序关闭时生成的 HistorySession 的 json 格式文本会写入到此空白文件中，
                if (!file.createNewFile()) {
                    throw new FileCreationException("无法创建历史会话文件");
                }
            } catch (IOException e) {
                throw new FileCreationException("无法创建历史会话文件：\n" + e.getMessage());
            }
            // Session 中的本应由 HistorySession 设置的字段保持默认值即可
        }
        // 确认历史会话文件的路径供保存时使用
        historySessionFilePath = file.getAbsolutePath();
    }

    private static File currentOrDefault(String path) {
        File file = new File(path);
        if (file.exists() && file.isDirectory()) {
            return file;
        } else {
            return DEFAULT_FOLDER;
        }
    }

    public static File getLastSelectedFolder() {
        if (!lastSelectedFolder.exists() || !lastSelectedFolder.isDirectory()) {
            lastSelectedFolder = DEFAULT_FOLDER;
        }
        return lastSelectedFolder;
    }

    public static void setLastSelectedFolder(File file) {
        if (file.exists() && file.isDirectory()) {
            lastSelectedFolder = file;
        } else {
            lastSelectedFolder = DEFAULT_FOLDER;
        }
    }

    public static File getLastSelectedImageFolder() {
        if (!lastSelectedImageFolder.exists()) {
            lastSelectedImageFolder = DEFAULT_FOLDER;
        }
        return lastSelectedImageFolder;
    }

    public static void setLastSelectedImageFolder(File file) {
        if (file.exists() && file.isDirectory()) {
            lastSelectedImageFolder = file;
        } else {
            lastSelectedImageFolder = DEFAULT_FOLDER;
        }
    }

    public static File getLastImageSavingPath() {
        if (!lastImageSavingPath.exists() || !lastImageSavingPath.isDirectory()) {
            lastImageSavingPath = DEFAULT_FOLDER;
        }
        return lastImageSavingPath;
    }

    public static void setLastImageSavingPath(File file) {
        if (file.exists() && file.isDirectory()) {
            lastImageSavingPath = file;
        } else {
            lastImageSavingPath = DEFAULT_FOLDER;
        }
    }

    public static List<String> getOpenedPaths() {
        return new ArrayList<>(openedPaths);
    }

    public static HashMap<Integer, String> getColumnsOrder() {
        return new HashMap<>(columnsOrder);
    }

    public static HashMap<String, Boolean> getColumnVisibleStatus() {
        return new HashMap<>(columnsVisibleStatus);
    }

    public static void saveSession() {
        // 保存前先关闭过滤器，防止保存时丢失数据
        UiCoordinator.recoverTableViewItems();

        try (FileWriter writer = new FileWriter(historySessionFilePath, StandardCharsets.UTF_8)) {
            HistorySession history = new HistorySession();
            history.setLastSelectedFolder(lastSelectedFolder.getAbsolutePath());
            history.setLastSelectedImageFolder(lastSelectedImageFolder.getAbsolutePath());
            history.setLastImageSavingPath(lastImageSavingPath.getAbsolutePath());
            history.setLastOpenedPaths(openedPaths);
            history.setColumnsOrder(columnsOrder);
            history.setColumnsVisibleStatus(columnsVisibleStatus);
            GSON.toJson(history, writer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void saveConfig() {
        try (FileWriter writer = new FileWriter(configFilePath, StandardCharsets.UTF_8)) {
            GSON.toJson(cfg, writer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static final HashMap<byte[], ImageView> CACHED_IMAGE_VIEW = new HashMap<>();
    public static ImageView getImageView(byte[] cover) {
        ImageView imageView = CACHED_IMAGE_VIEW.get(cover);
        if (imageView == null) {
            imageView = new ImageView();
            imageView.setFitWidth(200);
            imageView.setFitHeight(200);
            imageView.setImage(new Image(new ByteArrayInputStream(cover)));
            CACHED_IMAGE_VIEW.put(cover, imageView);
        }
        return imageView;
    }

    private static final HashSet<String> ALTERNATIVE_ARTISTS = new HashSet<>();
    private static final HashSet<String> ALTERNATIVE_ALBUMS = new HashSet<>();

    public static void recordItems(List<AudioFileData> dataList, boolean isAdditional) {
        if (!isAdditional) {
            openedPaths.clear();
            CACHED_IMAGE_VIEW.clear();
            ALTERNATIVE_ARTISTS.clear();
            ALTERNATIVE_ALBUMS.clear();
        }

        DaemonExecutor.TIME_CONSUMING_TASK_EXECUTOR.submit(() -> {
            for (AudioFileData data : dataList) {
                byte[] cover = data.getCover();
                if (cover != null && CACHED_IMAGE_VIEW.get(cover) == null) {
                    ImageView imageView = new ImageView();
                    imageView.setFitWidth(200);
                    imageView.setFitHeight(200);
                    imageView.setImage(new Image(new ByteArrayInputStream(cover)));
                    CACHED_IMAGE_VIEW.put(cover, imageView);
                }
            }
        });

        for (AudioFileData data : dataList) {
            openedPaths.add(data.getAbsolutePath());

            String artist = data.getArtist();
            if (artist != null && !"".equals(artist.trim())) {
                ALTERNATIVE_ARTISTS.add(artist.trim());
            }

            String album = data.getAlbum();
            if (album != null && !"".equals(album.trim())) {
                ALTERNATIVE_ALBUMS.add(album.trim());
            }
        }
    }

    public static HashSet<String> getAlternativeArtists() {
        return new HashSet<>(ALTERNATIVE_ARTISTS);
    }

    public static HashSet<String> getAlternativeAlbums() {
        return new HashSet<>(ALTERNATIVE_ALBUMS);
    }

    public static void updateVisibleStatus(String columnName, Boolean n) {
        columnsVisibleStatus.replace(columnName, n);
    }

    public static void updateColumnOrder(HashMap<Integer, String> map) {
        columnsOrder = map;
    }

    public static AppConfiguration getConfig() {
        return cfg;
    }

    public static void setConfig(AppConfiguration config) {
        cfg = new AppConfiguration(config);
    }
}
