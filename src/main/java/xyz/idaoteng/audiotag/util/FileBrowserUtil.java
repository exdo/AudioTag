package xyz.idaoteng.audiotag.util;

import java.awt.*;
import java.io.File;
import java.io.IOException;

import static xyz.idaoteng.audiotag.util.FileBrowserUtil.Message.*;

public class FileBrowserUtil {
    public enum Message {

        // 错误信息
        ERROR_NULL_FILE("错误：传入的 File 对象为 null。"),
        ERROR_FILE_NOT_EXIST("错误：文件或目录不存在"),
        ERROR_UNSUPPORTED_OS("错误：当前操作系统不支持此功能。"),
        ERROR_IO_EXCEPTION("打开文件管理器时发生 IO 错误"),
        ERROR_SECURITY_EXCEPTION("安全管理器阻止了文件管理器操作"),
        ERROR_UNSUPPORTED_OPERATION("当前桌面环境不支持此操作"),
        ERROR_UNKNOWN_EXCEPTION("发生未知错误"),
        ERROR_LINUX_NO_COMMAND("错误：在 Linux 上无法找到合适的文件管理器命令来打开目录。"),
        ERROR_LINUX_NO_DIRECTORY("错误：无法确定要打开的目录。"),

        // 提示信息
        INFO_OPERATION_DONE("操作成功");


        private final String message;

        Message(String message) {
            this.message = message;
        }

        /**
         * 获取消息字符串。
         *
         * @return 消息字符串。
         */
        public String getMessage() {
            return message;
        }
    }


    /**
     * 在系统的文件管理器中打开指定文件所在的目录，并尝试高亮该文件或文件夹。
     *
     * @param file 要定位的文件或目录。
     * @return 如果操作成功，返回 true；否则返回 false。
     */
    public static Message highlightFile(File file) {
        if (file == null) {
            return ERROR_NULL_FILE;
        }

        if (!file.exists()) {
            return ERROR_FILE_NOT_EXIST;
        }

        try {
            String os = System.getProperty("os.name").toLowerCase();

            if (os.contains("windows")) {
                String command = "explorer.exe /e,/select,\"" + file.getAbsolutePath() + "\"";
                Runtime.getRuntime().exec(command);
                return INFO_OPERATION_DONE;
            } else if (os.contains("mac os x") || os.contains("darwin")) {
                String command = "open -R \"" + file.getAbsolutePath() + "\"";
                Runtime.getRuntime().exec(command);
                return INFO_OPERATION_DONE;
            } else if (os.contains("linux")) {
                File directoryToOpen = file.isDirectory() ? file : file.getParentFile();
                if (directoryToOpen != null && directoryToOpen.exists()) {
                    if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
                        Desktop.getDesktop().open(directoryToOpen);
                        return INFO_OPERATION_DONE;
                    } else {
                        String[] commands = {"xdg-open", "gnome-open", "kde-open", "nautilus"};
                        for (String cmd : commands) {
                            try {
                                ProcessBuilder pb = new ProcessBuilder(cmd, directoryToOpen.getAbsolutePath());
                                Process p = pb.start();
                                p.waitFor(); // 等待命令执行完成
                                return INFO_OPERATION_DONE;
                            } catch (IOException | InterruptedException ignore) {
                                // 尝试下一个命令
                            }
                        }
                        return ERROR_LINUX_NO_COMMAND;
                    }
                } else {
                    return ERROR_LINUX_NO_DIRECTORY;
                }
            } else {
                return ERROR_UNSUPPORTED_OS;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return ERROR_IO_EXCEPTION;
        } catch (SecurityException e) {
            e.printStackTrace();
            return ERROR_SECURITY_EXCEPTION;
        } catch (UnsupportedOperationException e) {
            e.printStackTrace();
            return ERROR_UNSUPPORTED_OPERATION;
        } catch (Exception e) { // 捕获所有其他未预料的异常
            e.printStackTrace();
            return ERROR_UNKNOWN_EXCEPTION;
        }
    }
}
