module xyz.idaoteng.audiotag {
    requires javafx.controls;

    requires java.logging;
    requires java.desktop;

    requires jaudiotagger;
    requires net.coobird.thumbnailator;

    exports xyz.idaoteng.audiotag;
    exports xyz.idaoteng.audiotag.bean;
}