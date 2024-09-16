package xyz.idaoteng.audiotag.api.bean;

public class SongsResult {
    private Songs data;
    private String errmsg;
    private long errno;

    public Songs getData() { return data; }
    public void setData(Songs value) { this.data = value; }

    public String getErrmsg() { return errmsg; }
    public void setErrmsg(String value) { this.errmsg = value; }

    public long getErrno() { return errno; }
    public void setErrno(long value) { this.errno = value; }
}
