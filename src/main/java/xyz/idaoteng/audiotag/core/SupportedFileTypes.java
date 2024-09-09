package xyz.idaoteng.audiotag.core;

import org.jaudiotagger.audio.SupportedFileFormat;
import xyz.idaoteng.audiotag.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class SupportedFileTypes {
    public static final HashSet<String> SUPPORTED_FILE_TYPES = new HashSet<>();
    static {
        SupportedFileFormat[] formats = SupportedFileFormat.values();
        for (SupportedFileFormat format : formats) {
            SUPPORTED_FILE_TYPES.add(format.getFilesuffix());
        }
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
