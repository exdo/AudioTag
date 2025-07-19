package xyz.idaoteng.audiotag.jaudiotagger;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class SupportedFile {
    private static final String[] SUPPORTED_FILES = new String[]{
            "ogg",
            "oga",
            "mp3",
            "flac",
            "m4a",
            "m4p",
            "wma",
            "wav",
            "ra",
            "rm",
            "m4b",
            "aif",
            "aiff",
            "aifc",
            "dsf",
    };
    private static final HashSet<String> SUPPORTED_FILE_FORMATS = new HashSet<>(Arrays.asList(SUPPORTED_FILES));

    public static boolean isSupport(File file) {
        String name = file.getName().toLowerCase();
        int index = name.lastIndexOf('.');
        if (index == -1) {
            return false;
        }
        return SUPPORTED_FILE_FORMATS.contains(name.substring(index + 1));
    }

    public static List<String> allFormats() {
        return new ArrayList<>(SUPPORTED_FILE_FORMATS);
    }
}
