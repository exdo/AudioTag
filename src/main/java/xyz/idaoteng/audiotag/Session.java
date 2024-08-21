package xyz.idaoteng.audiotag;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Session {
    private static String LAST_SELECTED_FILE_PARENT_PATH;
    private static String LAST_SELECTED_DIRECTORY_PATH;
    private static String NEED_REFRESH_DIRECTORY_PATH;

    private static final List<String> CURRENT_TABLEVIEW_CONTENT = new ArrayList<>();

    static {
        readHistorySession();
    }

    private static void readHistorySession() {
        String historyFilePath = Session.class.getResource("session.history").getPath();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(historyFilePath, StandardCharsets.UTF_8));
            String lastSelectedFileParentPath = reader.readLine();
            LAST_SELECTED_FILE_PARENT_PATH = processPath(lastSelectedFileParentPath);

            String lastSelectedDirectoryPath = reader.readLine();
            LAST_SELECTED_DIRECTORY_PATH = processPath(lastSelectedDirectoryPath);

            String needRefreshDirectoryPath = reader.readLine();
            NEED_REFRESH_DIRECTORY_PATH = processPath(needRefreshDirectoryPath);

            String currentTableViewContent = reader.readLine();
            CURRENT_TABLEVIEW_CONTENT.addAll(processPaths(currentTableViewContent));
            reader.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static List<String> processPaths(String currentTableViewContent) {
        currentTableViewContent = currentTableViewContent.substring(currentTableViewContent.lastIndexOf('=') + 1);
        if ("".equals(currentTableViewContent)) {
            return Collections.emptyList();
        } else {
            return Arrays.stream(currentTableViewContent.split(",")).filter(path -> new File(path).exists()).toList();
        }
    }

    private static String processPath(String path) {
        path = path.substring(path.lastIndexOf('=') + 1);
        if ("".equals(path)) {
            return System.getProperty("user.home");
        } else {
            if (new File(path).isDirectory()) {
                return path;
            } else {
                return System.getProperty("user.home");
            }
        }
    }

    public static String getLastSelectedFileParentPath() {
        return LAST_SELECTED_FILE_PARENT_PATH;
    }

    public static void setLastSelectedFileParentPath(String path) {
        LAST_SELECTED_FILE_PARENT_PATH = path;
    }

    public static String getLastSelectedDirectoryPath() {
        return LAST_SELECTED_DIRECTORY_PATH;
    }

    public static void setLastSelectedDirectoryPath(String path) {
        LAST_SELECTED_DIRECTORY_PATH = path;
    }

    public static void setCurrentTableViewContent(List<String> absolutePaths) {
        CURRENT_TABLEVIEW_CONTENT.clear();
        CURRENT_TABLEVIEW_CONTENT.addAll(absolutePaths);
    }

    public static List<String> getCurrentTableViewContent() {
        return CURRENT_TABLEVIEW_CONTENT;
    }

    public static void saveSession() {
        String historyFilePath = Session.class.getResource("session.history").getPath();
        try (FileOutputStream outputStream = new FileOutputStream(historyFilePath)) {
            PrintWriter writer = new PrintWriter(outputStream);
            writer.println("LAST_SELECTED_DIRECTORY_PATH=" + LAST_SELECTED_DIRECTORY_PATH);
            writer.println("LAST_SELECTED_FILE_PARENT_PATH=" + LAST_SELECTED_FILE_PARENT_PATH);
            writer.println("NEED_REFRESH_DIRECTORY_PATH=" + NEED_REFRESH_DIRECTORY_PATH);
            writer.println("CURRENT_TABLEVIEW_CONTENT=" + String.join(",", CURRENT_TABLEVIEW_CONTENT));
            writer.flush();
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getNeedRefreshDirectoryPath() {
        return NEED_REFRESH_DIRECTORY_PATH;
    }

    public static void setNeedRefreshDirectoryPath(String needRefreshDirectoryPath) {
        NEED_REFRESH_DIRECTORY_PATH = needRefreshDirectoryPath;
    }
}
