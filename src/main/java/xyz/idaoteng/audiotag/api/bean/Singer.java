package xyz.idaoteng.audiotag.api.bean;

public class Singer {
    private long id;
    /**
     * 歌手名称
     */
    private String name;

    public long getId() { return id; }
    public void setId(long value) { this.id = value; }

    public String getName() { return name; }
    public void setName(String value) { this.name = value; }
}
