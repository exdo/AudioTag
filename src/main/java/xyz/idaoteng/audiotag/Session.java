package xyz.idaoteng.audiotag;

import javafx.scene.control.Alert;
import xyz.idaoteng.audiotag.component.Center;
import xyz.idaoteng.audiotag.constant.DefaultColumnOrder;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Session {
    private static final String PATH_SEPARATOR = "&#&";

    private static String folderPathOfTheLastSelectedFile;
    private static String pathToTheLastSelectedFolder;
    private static String folderPathOfTheLastSelectedImage;
    private static String lastSelectedImageSavingPath;

    private static final List<String> CURRENT_TABLEVIEW_CONTENT_PATHS = new ArrayList<>();

    private static final String sessionHistoryFilePath;

    private static HashMap<Integer, String> columnsOrder = null;

    static {
        String tmpDir = System.getProperty("java.io.tmpdir");
        File historyFile = new File(tmpDir, "audioTag.session.history");
        sessionHistoryFilePath = historyFile.getAbsolutePath();
        if (!historyFile.exists()) {
            try {
                boolean created = historyFile.createNewFile();
                if (!created) {
                    Alert alert = Utils.generateBasicErrorAlert("创建历史会话文件失败");
                    alert.show();
                    System.exit(403);
                } else {
                    System.out.println("sessionHistoryFilePath = " + sessionHistoryFilePath);
                    initHistoryFile();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        try {
            readHistorySession();
        } catch (Exception e) {
            initHistoryFile();
            readHistorySession();
        }
    }

    private static void readHistorySession() {
        try (FileReader fileReader = new FileReader(sessionHistoryFilePath, StandardCharsets.UTF_8)) {
            try {
                BufferedReader reader = new BufferedReader(fileReader);

                String line1 = reader.readLine();
                folderPathOfTheLastSelectedFile = processPath(line1);

                String line2 = reader.readLine();
                pathToTheLastSelectedFolder = processPath(line2);

                String line3 = reader.readLine();
                folderPathOfTheLastSelectedImage = processPath(line3);

                String line4 = reader.readLine();
                lastSelectedImageSavingPath = processPath(line4);

                String line5 = reader.readLine();
                CURRENT_TABLEVIEW_CONTENT_PATHS.addAll(processPaths(line5));

                String line6 = reader.readLine();
                columnsOrder = processOrder(line6);

                reader.close();
            } catch (Exception e) {
                throw new RuntimeException("读取历史会话失败");
            }
        } catch (Exception e) {
            throw new RuntimeException("找不到历史会话文件");
        }

    }

    private static HashMap<Integer, String> processOrder(String line6) {
        line6 = line6.substring(line6.indexOf('=') + 1);
        if ("".equals(line6)) {
            return DefaultColumnOrder.defaultOrder();
        } else {
            HashMap<Integer, String> order = new HashMap<>();
            String[] entries = line6.split(";");
            if  (entries.length != 10) {
                return DefaultColumnOrder.defaultOrder();
            }
            for (String entry : entries) {
                String[] kv = entry.split(":");
                order.put(Integer.parseInt(kv[0]), kv[1]);
            }
            return order;
        }
    }

    private static List<String> processPaths(String line) {
        line = line.substring(line.indexOf('=') + 1);
        if ("".equals(line)) {
            return Collections.emptyList();
        } else {
            return Arrays.stream(line.split(PATH_SEPARATOR)).filter(path -> new File(path).exists()).toList();
        }
    }

    private static String processPath(String line) {
        line = line.substring(line.indexOf('=') + 1);
        if ("".equals(line)) {
            return System.getProperty("user.home");
        } else {
            if (new File(line).isDirectory()) {
                return line;
            } else {
                return System.getProperty("user.home");
            }
        }
    }

    public static String getFolderPathOfTheLastSelectedFile() {
        if (!new File(folderPathOfTheLastSelectedFile).exists()) {
            folderPathOfTheLastSelectedFile = System.getProperty("user.home");
        }
        return folderPathOfTheLastSelectedFile;
    }

    public static void setFolderPathOfTheLastSelectedFile(String path) {
        folderPathOfTheLastSelectedFile = path;
    }

    public static String getPathToTheLastSelectedFolder() {
        if (!new File(pathToTheLastSelectedFolder).exists()) {
            pathToTheLastSelectedFolder = System.getProperty("user.home");
        }
        return pathToTheLastSelectedFolder;
    }

    public static void setPathToTheLastSelectedFolder(String path) {
        pathToTheLastSelectedFolder = path;
    }

    public static String getFolderPathOfTheLastSelectedImage() {
        if (!new File(folderPathOfTheLastSelectedImage).exists()) {
            folderPathOfTheLastSelectedImage = System.getProperty("user.home");
        }
        return folderPathOfTheLastSelectedImage;
    }

    public static void setFolderPathOfTheLastSelectedImage(String path) {
        folderPathOfTheLastSelectedImage = path;
    }

    public static String getLastSelectedImageSavingPath() {
        if (!new File(lastSelectedImageSavingPath).exists()) {
            lastSelectedImageSavingPath = System.getProperty("user.home");
        }
        return lastSelectedImageSavingPath;
    }

    public static void setLastSelectedImageSavingPath(String path) {
        lastSelectedImageSavingPath = path;
    }

    public static void setCurrentTableViewContentPaths(List<String> absolutePaths) {
        CURRENT_TABLEVIEW_CONTENT_PATHS.clear();
        CURRENT_TABLEVIEW_CONTENT_PATHS.addAll(absolutePaths);
    }

    public static List<String> getCurrentTableViewContentPaths() {
        return CURRENT_TABLEVIEW_CONTENT_PATHS;
    }

    public static HashMap<Integer, String> getColumnsOrder() {
        return columnsOrder;
    }

    private static void initHistoryFile() {
        try (FileOutputStream outputStream = new FileOutputStream(sessionHistoryFilePath)) {
            PrintWriter writer = new PrintWriter(outputStream, true, StandardCharsets.UTF_8);
            writer.println("folder_path_of_the_last_selected_file=");
            writer.println("path_to_the_last_selected_folder=");
            writer.println("folder_path_of_the_last_selected_image=");
            writer.println("last_selected_image_saving_path=");
            writer.println("current_tableview_content_paths=");
            writer.println("column_order=");
            writer.flush();
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void saveSession() {
        // 关闭过滤器，防止保存时丢失数据
        Center.turnOffFilter();

        try (FileOutputStream outputStream = new FileOutputStream(sessionHistoryFilePath)) {
            PrintWriter writer = new PrintWriter(outputStream, true, StandardCharsets.UTF_8);
            writer.println("folder_path_of_the_last_selected_file=" + folderPathOfTheLastSelectedFile);
            writer.println("path_to_the_last_selected_folder=" + pathToTheLastSelectedFolder);
            writer.println("folder_path_of_the_last_selected_image=" + folderPathOfTheLastSelectedImage);
            writer.println("last_selected_image_saving_path=" + lastSelectedImageSavingPath);
            writer.println("current_tableview_content_paths=" + String.join(PATH_SEPARATOR, CURRENT_TABLEVIEW_CONTENT_PATHS));
            writer.println(generateColumnOrderString());
            writer.flush();
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String generateColumnOrderString() {
        HashMap<Integer, String> columnOrder = Center.getColumnOrder();
        StringBuilder sb = new StringBuilder();
        sb.append("column_order=");
        for (int i = 0; i < 10; i++) {
            sb.append(i);
            sb.append(":");
            sb.append(columnOrder.get(i));
            sb.append(";");
        }
        // 去除尾部的分号
        return sb.substring(0, sb.length() - 1);
    }
}
