module xyz.idaoteng.audiotag {
    requires javafx.controls;

    requires java.logging;
    requires java.desktop;
    requires java.net.http;

    requires jaudiotagger;
    requires net.coobird.thumbnailator;
    requires com.google.gson;

    exports xyz.idaoteng.audiotag;
    exports xyz.idaoteng.audiotag.bean;

    opens xyz.idaoteng.audiotag.api.bean to com.google.gson;
}