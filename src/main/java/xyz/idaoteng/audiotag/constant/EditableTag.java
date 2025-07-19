package xyz.idaoteng.audiotag.constant;

import org.jaudiotagger.tag.FieldKey;
import xyz.idaoteng.audiotag.bean.AudioFileData;

import java.util.ArrayList;
import java.util.Arrays;

public enum EditableTag {
    TITLE("标题", FieldKey.TITLE),
    ARTIST("艺术家", FieldKey.ARTIST),
    ALBUM("专辑", FieldKey.ALBUM),
    DATE("日期", FieldKey.YEAR),
    GENRE("流派", FieldKey.GENRE),
    TRACK("序号", FieldKey.TRACK),
    COMMENT("备注", FieldKey.COMMENT),
    COVER("封面", FieldKey.COVER_ART),
    LYRIC("歌词", FieldKey.LYRICS);

    private final String text;
    private final FieldKey key;

    EditableTag(String text, FieldKey key) {
        this.text = text;
        this.key = key;
    }

    public String getText() {
        return text;
    }

    public FieldKey getKey() {
        return key;
    }

    public static ArrayList<EditableTag> getAll() {
        return new ArrayList<>(Arrays.asList(EditableTag.values()));
    }

    public static String getValue(AudioFileData data, EditableTag tag) {
        return switch (tag) {
            case TITLE -> data.getTitle();
            case ARTIST -> data.getArtist();
            case ALBUM -> data.getAlbum();
            case DATE -> data.getDate();
            case GENRE -> data.getGenre();
            case TRACK -> data.getTrack();
            case COMMENT -> data.getComment();
            case COVER -> null;
            case LYRIC -> data.getLyric();
        };
    }
}