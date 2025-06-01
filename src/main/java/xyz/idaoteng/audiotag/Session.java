package xyz.idaoteng.audiotag;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javafx.scene.control.Alert;
import xyz.idaoteng.audiotag.bean.Preferences;
import xyz.idaoteng.audiotag.component.Center;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;

public class Session {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static final Preferences PREFERENCES = new Preferences();

    private static String preferencesFilePath;

    static {
        // 读取注册表中的 preferences 文件路径，
        // 该路径会在 InstallerGenerationScript.nsi 脚本中写入注册表
        String filePath = Utils.getPreferencesFilePathInRegistry();

        if (filePath == null) {
            // 开发环境不从注册表中读取，而是从系统临时目录中读取
            readPreferences(System.getProperty("java.io.tmpdir"));
        }else {
            readPreferences(filePath);
        }
    }

    private static void readPreferences(String path) {
        File file = new File(path, "preferences.json");
        if (!file.exists()) { // 文件不存在，一般见于程序第一次运行时
            try {
                // 创建一个空白文件即可，
                // 默认 new 出来的 Preferences 对象就是默认配置，
                // 程序关闭时会保存该对象的 json 格式到此空白文件中
                if (!file.createNewFile()) {
                    showErrorThenExit("无法创建配置文件");
                }
            } catch (IOException e) {
                showErrorThenExit("无法创建配置文件：\n" + e.getMessage());
            }
        } else {
             try (FileReader reader = new FileReader(file, StandardCharsets.UTF_8)) {
                 Preferences fromJson = GSON.fromJson(reader, Preferences.class);
                 PREFERENCES.setLastSelectedFile(fromJson.getLastSelectedFile());
                 PREFERENCES.setLastSelectedFolder(fromJson.getLastSelectedFolder());
                 PREFERENCES.setLastSelectedImage(fromJson.getLastSelectedImage());
                 PREFERENCES.setImageSavingPath(fromJson.getImageSavingPath());
                 PREFERENCES.setCurrentPaths(fromJson.getCurrentPaths());
                 PREFERENCES.setColumnsOrder(fromJson.getColumnsOrder());
                 PREFERENCES.setRetouchCover(fromJson.getRetouchCover());

             } catch (IOException e) {
                 showErrorThenExit("无法读取配置文件：\n"  + e.getMessage());
             }
        }
        // 确认配置文件的路径供保存时使用
        preferencesFilePath = file.getAbsolutePath();
    }

    private static void showErrorThenExit(String content) {
        Alert alert = Utils.generateBasicErrorAlert("程序运行时出现错误");
        alert.setContentText(content);
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.exit(555);
    }

    public static String getFolderPathOfTheLastSelectedFile() {
        String filePath = PREFERENCES.getLastSelectedFile();
        if (!new File(filePath).exists()) {
            filePath = System.getProperty("user.home");
            PREFERENCES.setLastSelectedFile(filePath);
        }
        return filePath;
    }

    public static void setFolderPathOfTheLastSelectedFile(String path) {
        PREFERENCES.setLastSelectedFile(path);
    }

    public static String getPathToTheLastSelectedFolder() {
        String folderPath = PREFERENCES.getLastSelectedFolder();
        if (!new File(folderPath).exists()) {
            folderPath = System.getProperty("user.home");
            PREFERENCES.setLastSelectedFolder(folderPath);
        }
        return folderPath;
    }

    public static void setPathToTheLastSelectedFolder(String path) {
        PREFERENCES.setLastSelectedFolder(path);
    }

    public static String getFolderPathOfTheLastSelectedImage() {
        String imageFolder = PREFERENCES.getLastSelectedImage();
        if (!new File(imageFolder).exists()) {
            imageFolder = System.getProperty("user.home");
        }
        return imageFolder;
    }

    public static void setFolderPathOfTheLastSelectedImage(String path) {
        PREFERENCES.setLastSelectedImage(path);
    }

    public static String getLastSelectedImageSavingPath() {
        String imageSavingPath = PREFERENCES.getImageSavingPath();
        if (!new File(imageSavingPath).exists()) {
            imageSavingPath = System.getProperty("user.home");
            PREFERENCES.setImageSavingPath(imageSavingPath);
        }
        return imageSavingPath;
    }

    public static void setLastSelectedImageSavingPath(String path) {
        PREFERENCES.setImageSavingPath(path);
    }

    public static List<String> getCurrentTableViewContentPaths() {
        return PREFERENCES.getCurrentPaths();
    }

    public static void setCurrentTableViewContentPaths(List<String> absolutePaths) {
        PREFERENCES.getCurrentPaths().clear();
        PREFERENCES.getCurrentPaths().addAll( absolutePaths);
    }

    public static HashMap<Integer, String> getColumnsOrder() {
        return PREFERENCES.getColumnsOrder();
    }

    public static boolean needRetouchCover() {
        return PREFERENCES.getRetouchCover();
    }

    public static void saveSession() {
        // 保存前先关闭过滤器，防止保存时丢失数据
        Center.turnOffFilter();

        try (FileWriter writer = new FileWriter(preferencesFilePath, StandardCharsets.UTF_8)) {
            PREFERENCES.setColumnsOrder(Center.getColumnOrder());
            GSON.toJson(PREFERENCES, writer);
        } catch (IOException e) {
            showErrorThenExit("无法保存配置文件：\n" + e.getMessage());
        }
    }
}
