package xyz.idaoteng.audiotag;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Session {
    private static String lastSelectedFileParentPath;
    private static String lastSelectedDirectoryPath;

    private static final List<String> CURRENT_TABLEVIEW_CONTENT_PATHS = new ArrayList<>();

    private static final ArrayList<String> ALTERNATIVE_GENRES = new ArrayList<>();

    static {
        readHistorySession();
    }

    private static void readHistorySession() {
        String historyFilePath = Session.class.getResource("session.history").getPath();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(historyFilePath, StandardCharsets.UTF_8));

            String lastSelectedDirectoryPath = reader.readLine();
            Session.lastSelectedDirectoryPath = processPath(lastSelectedDirectoryPath);

            String lastSelectedFileParentPath = reader.readLine();
            Session.lastSelectedFileParentPath = processPath(lastSelectedFileParentPath);

            String currentTableViewContent = reader.readLine();
            CURRENT_TABLEVIEW_CONTENT_PATHS.addAll(processPaths(currentTableViewContent));

            String alternativeGenres = reader.readLine();
            processGenres(alternativeGenres);

            reader.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void processGenres(String alternativeGenres) {
        alternativeGenres = alternativeGenres.substring(alternativeGenres.lastIndexOf('=') + 1);
        if (!"".equals(alternativeGenres)) {
            if (alternativeGenres.contains(",")) {
                ALTERNATIVE_GENRES.addAll(List.of(alternativeGenres.split(",")));
            } else {
                ALTERNATIVE_GENRES.add(alternativeGenres);
            }
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
        return lastSelectedFileParentPath;
    }

    public static void setLastSelectedFileParentPath(String path) {
        lastSelectedFileParentPath = path;
    }

    public static String getLastSelectedDirectoryPath() {
        return lastSelectedDirectoryPath;
    }

    public static void setLastSelectedDirectoryPath(String path) {
        lastSelectedDirectoryPath = path;
    }

    public static void setCurrentTableViewContentPaths(List<String> absolutePaths) {
        CURRENT_TABLEVIEW_CONTENT_PATHS.clear();
        CURRENT_TABLEVIEW_CONTENT_PATHS.addAll(absolutePaths);
    }

    public static List<String> getCurrentTableViewContentPaths() {
        return CURRENT_TABLEVIEW_CONTENT_PATHS;
    }

    public static void addGenre(String genre) {
        ALTERNATIVE_GENRES.add(genre);
    }

    public static List<String> getAlternativeGenres() {
        return ALTERNATIVE_GENRES;
    }

    public static void saveSession() {
        String historyFilePath = Session.class.getResource("session.history").getPath();
        try (FileOutputStream outputStream = new FileOutputStream(historyFilePath)) {
            PrintWriter writer = new PrintWriter(outputStream);
            writer.println("last_selected_directory_path=" + lastSelectedDirectoryPath);
            writer.println("last_selected_file_parent_path" + lastSelectedFileParentPath);
            writer.println("current_tableview_content_paths=" + String.join(",", CURRENT_TABLEVIEW_CONTENT_PATHS));
            writer.println("alternative_genres=" + String.join(",", ALTERNATIVE_GENRES));
            writer.flush();
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
