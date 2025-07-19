package xyz.idaoteng.audiotag.constant;

import javafx.stage.FileChooser;
import xyz.idaoteng.audiotag.jaudiotagger.SupportedFile;

public class ExtensionFilters {
    public static final FileChooser.ExtensionFilter FILE_FILTER = new FileChooser.ExtensionFilter("音频文件", SupportedFile.allFormats());

    private static final String[] imageExtensions = new String[]{"*.jpg", "*.jpeg", "*.png", "*.bmp"};
    public static final FileChooser.ExtensionFilter IMAGE_FILTER = new FileChooser.ExtensionFilter("图片文件", imageExtensions);
}
