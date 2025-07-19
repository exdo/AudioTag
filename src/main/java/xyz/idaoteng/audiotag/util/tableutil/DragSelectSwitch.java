package xyz.idaoteng.audiotag.util.tableutil;

public class DragSelectSwitch {
    private boolean enable = true;

    public void turnOn() {
        enable = true;
    }

    public void turnOff() {
        enable = false;
    }

    public boolean isEnable() {
        return enable;
    }
}
