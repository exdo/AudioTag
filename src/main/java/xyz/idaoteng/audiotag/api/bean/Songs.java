package xyz.idaoteng.audiotag.api.bean;

public class Songs {
    private SongDetail[] list;
    /**
     * 总数
     */
    private long total;

    public SongDetail[] getList() { return list; }
    public void setList(SongDetail[] value) { this.list = value; }

    public long getTotal() { return total; }
    public void setTotal(long value) { this.total = value; }
}
