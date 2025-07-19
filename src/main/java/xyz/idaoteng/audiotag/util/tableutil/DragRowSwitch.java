package xyz.idaoteng.audiotag.util.tableutil;

public class DragRowSwitch {
    private boolean enable = false;

    public void turnOn() {
        enable = true;
    }

    public void turnOff() {
        enable = false;
    }

    public boolean isDisable() {
        return !enable;
    }
}
