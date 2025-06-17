package xyz.idaoteng.audiotag.constant;

public enum EditableTag {
    TITLE("标题"),
    ARTIST("艺术家"),
    ALBUM("专辑"),
    DATE("日期"),
    GENRE("流派"),
    TRACK("序号"),
    COMMENT("备注"),
    COVER("封面"),
    ALL("所有标签");

    private final String text;

    EditableTag(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}
