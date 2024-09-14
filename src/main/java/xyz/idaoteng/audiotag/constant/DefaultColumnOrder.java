package xyz.idaoteng.audiotag.constant;

import java.util.HashMap;

public class DefaultColumnOrder {
    private static final HashMap<Integer, String> DEFAULT_ORDER = new HashMap<>();

    static {
        DEFAULT_ORDER.put(0, "filename");
        DEFAULT_ORDER.put(1, "artist");
        DEFAULT_ORDER.put(2, "title");
        DEFAULT_ORDER.put(3, "album");
        DEFAULT_ORDER.put(4, "track");
        DEFAULT_ORDER.put(5, "genre");
        DEFAULT_ORDER.put(6, "date");
        DEFAULT_ORDER.put(7, "comment");
        DEFAULT_ORDER.put(8, "bitrate");
        DEFAULT_ORDER.put(9, "length");
    }

    public static HashMap<Integer, String> defaultOrder() {
        return new HashMap<>(DEFAULT_ORDER);
    }
}
