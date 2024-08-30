package xyz.idaoteng.audiotag;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Session {
    private static final String PATH_SEPARATOR = "&#&";
    private static final String GENRE_SEPARATOR = "&@&";

    private static String folderPathOfTheLastSelectedFile;
    private static String pathToTheLastSelectedFolder;
    private static String folderPathOfTheLastSelectedImage;
    private static String lastSelectedImageSavingPath;

    private static final List<String> CURRENT_TABLEVIEW_CONTENT_PATHS = new ArrayList<>();

    private static final ArrayList<String> ALTERNATIVE_GENRES = new ArrayList<>();

    static {
        readHistorySession();
    }

    private static void readHistorySession() {
        try (InputStream history = Session.class.getResourceAsStream("session.history")) {
            if (history == null) {
                throw new RuntimeException("找不到历史会话文件");
            }
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(history, StandardCharsets.UTF_8));

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
                processGenres(line6);

                reader.close();
            } catch (IOException e) {
                throw new RuntimeException("读取历史会话失败");
            }
        } catch (IOException e) {
            throw new RuntimeException("找不到历史会话文件");
        }

    }

    private static void processGenres(String line) {
        line = line.substring(line.indexOf('=') + 1);
        if (!"".equals(line)) {
            if (line.contains(GENRE_SEPARATOR)) {
                ALTERNATIVE_GENRES.addAll(List.of(line.split(GENRE_SEPARATOR)));
            } else {
                ALTERNATIVE_GENRES.add(line);
            }
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
        return folderPathOfTheLastSelectedFile;
    }

    public static void setFolderPathOfTheLastSelectedFile(String path) {
        folderPathOfTheLastSelectedFile = path;
    }

    public static String getPathToTheLastSelectedFolder() {
        return pathToTheLastSelectedFolder;
    }

    public static void setPathToTheLastSelectedFolder(String path) {
        pathToTheLastSelectedFolder = path;
    }

    public static String getFolderPathOfTheLastSelectedImage() {
        return folderPathOfTheLastSelectedImage;
    }

    public static void setFolderPathOfTheLastSelectedImage(String path) {
        folderPathOfTheLastSelectedImage = path;
    }

    public static String getLastSelectedImageSavingPath() {
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
            writer.println("folder_path_of_the_last_selected_file=" + folderPathOfTheLastSelectedFile);
            writer.println("path_to_the_last_selected_folder=" + pathToTheLastSelectedFolder);
            writer.println("folder_path_of_the_last_selected_image=" + folderPathOfTheLastSelectedImage);
            writer.println("last_selected_image_saving_path=" + lastSelectedImageSavingPath);
            writer.println("current_tableview_content_paths=" + String.join(PATH_SEPARATOR, CURRENT_TABLEVIEW_CONTENT_PATHS));
            writer.println("alternative_genres=" + String.join(GENRE_SEPARATOR, ALTERNATIVE_GENRES));
            writer.flush();
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
