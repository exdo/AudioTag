package xyz.idaoteng.audiotag.core;

import xyz.idaoteng.audiotag.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class SupportedFileTypes {
    public static final HashSet<String> SUPPORTED_FILE_TYPES = new HashSet<>();
    static {
        SUPPORTED_FILE_TYPES.add("flac");
        SUPPORTED_FILE_TYPES.add("mp3");
        SUPPORTED_FILE_TYPES.add("wav");
        SUPPORTED_FILE_TYPES.add("m4a");
        SUPPORTED_FILE_TYPES.add("dsf");
    }

    public static boolean isSupported(File file) {
        return SUPPORTED_FILE_TYPES.contains(Utils.getExtension(file));
    }

    public static List<String> getTypes() {
        List<String> types = new ArrayList<>(5);
        SUPPORTED_FILE_TYPES.forEach(type -> types.add("*." + type));
        return types;
    }
}
