package xyz.idaoteng.lesson1;

import java.io.File;

public class Utils {
    public static String getExtension(final File f)
    {
        final String name = f.getName().toLowerCase();
        final int i = name.lastIndexOf(".");
        if (i == -1) {
            return "";
        }

        return name.substring(i + 1);
    }
}
