package xyz.idaoteng.audiotag.constant;

import java.util.HashMap;

public class ColumnsDefaultStatus {
    private static final HashMap<Integer, String> DEFAULT_ORDER = new HashMap<>();
    private static final HashMap<String, Boolean> VISIBLE_MAP = new HashMap<>();

    static {
        DEFAULT_ORDER.put(0, Column.FILENAME.getName());
        DEFAULT_ORDER.put(1, Column.ARTIST.getName());
        DEFAULT_ORDER.put(2, Column.TITLE.getName());
        DEFAULT_ORDER.put(3, Column.ALBUM.getName());
        DEFAULT_ORDER.put(4, Column.TRACK.getName());
        DEFAULT_ORDER.put(5, Column.GENRE.getName());
        DEFAULT_ORDER.put(6, Column.DATE.getName());
        DEFAULT_ORDER.put(7, Column.COMMENT.getName());
        DEFAULT_ORDER.put(8, Column.BITRATE.getName());
        DEFAULT_ORDER.put(9, Column.LENGTH.getName());
        DEFAULT_ORDER.put(10, Column.FORMAT.getName());
        DEFAULT_ORDER.put(11, Column.SIZE.getName());

        for (int i = 0; i < 12; i++) {
            VISIBLE_MAP.put(DEFAULT_ORDER.get(i), true);
        }
    }

    public static HashMap<Integer, String> order() {
        return new HashMap<>(DEFAULT_ORDER);
    }

    public static HashMap<String, Boolean> visible() {
        return new HashMap<>(VISIBLE_MAP);
    }
}
