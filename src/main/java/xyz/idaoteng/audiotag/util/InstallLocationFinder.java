package xyz.idaoteng.audiotag.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Properties;

public class InstallLocationFinder {

    // --- Windows Specific ---
    public static final String REG_COMMAND = "reg query ";
    public static final String REG_ARG = " /v ";
    private static final String REG_KEY = "\"HKEY_LOCAL_MACHINE\\SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\App Paths\\AudioTag.exe\"";
    // 此注册表项的值会由 ExeInstaller.nsi 脚本写入到 windows 系统的注册表中
    private static final String ITEM_NAME = "install_dir";
    public static final String COMPLETE_REG_COMMAND = REG_COMMAND + REG_KEY + REG_ARG + ITEM_NAME;

    // 注意：这里的 APP_NAME_LOWER 应该与 macOS/Linux 安装程序中使用的名称保持一致
    private static final String APP_NAME_LOWER = "audiotag";
    private static final String INSTALL_INFO_FILENAME = "install_info.properties"; // 约定一个配置文件名
    private static final String INSTALL_DIR_PROPERTY_KEY = "install_dir"; // 配置文件中存储安装路径的键名

    /**
     * 获取此软件的安装位置，同时适配 Windows, macOS, Linux。
     * <p>
     * 对于 macOS 和 Linux，我们假设其安装程序会将安装路径写入一个特定位置的 properties 文件中。
     * 例如：
     * - macOS: ~/Library/Application Support/audiotag/install_info.properties
     * - Linux: ~/.config/audiotag/install_info.properties 或 /etc/audiotag/install_info.properties
     * <p>
     * install_info.properties 文件内容示例：
     * install_dir=/path/to/your/application
     *
     * @return 软件的安装目录，如果无法找到则返回 null。
     */
    public static String getInstallDir() {
        String os = System.getProperty("os.name").toLowerCase();

        if (os.contains("win")) {
            return getInstallDirWindows();
        } else if (os.contains("mac")) {
            return getInstallDirMac();
        } else if (os.contains("nix") || os.contains("nux") || os.contains("aix")) { // Linux, Unix, AIX
            return getInstallDirLinux();
        } else {
            System.err.println("Unsupported operating system: " + os);
            return null;
        }
    }

    /**
     * 获取 Windows 系统下软件的安装位置（通过注册表）。
     *
     * @return 安装目录或 null。
     */
    private static String getInstallDirWindows() {
        String result = "";
        try {
            Process process = Runtime.getRuntime().exec(COMPLETE_REG_COMMAND);

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    // 注册表查询结果通常是 "    ITEM_NAME    REG_SZ    C:\Path\To\Install"
                    if (line.trim().startsWith(ITEM_NAME)) {
                        // 使用 split("\\s+", 3) 来确保即使路径中包含空格也能正确解析
                        String[] parts = line.trim().split("\\s+", 3);
                        if (parts.length == 3) {
                            result = parts[2];
                        }
                    }
                }
            }

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                // 检查 result 是否为空，如果为空说明注册表项可能不存在或值为空
                return result.isEmpty() ? null : result;
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 获取 macOS 系统下软件的安装位置（通过配置文件）。
     * 假设配置文件位于 ~/Library/Application Support/audiotag/install_info.properties
     *
     * @return 安装目录或 null。
     */
    private static String getInstallDirMac() {
        String userHome = System.getProperty("user.home");
        // macOS 标准的应用程序支持目录
        File configFile = new File(userHome + File.separator + "Library" +
                File.separator + "Application Support" + File.separator + APP_NAME_LOWER +
                File.separator + INSTALL_INFO_FILENAME);

        return readInstallDirFromFile(configFile);
    }

    /**
     * 获取 Linux 系统下软件的安装位置（通过配置文件）。
     * 假设配置文件位于 ~/.config/audiotag/install_info.properties 或 /etc/audiotag/install_info.properties
     *
     * @return 安装目录或 null。
     */
    private static String getInstallDirLinux() {
        String userHome = System.getProperty("user.home");
        // 优先查找用户配置目录 (XDG Base Directory Specification)
        File configFile = new File(userHome + File.separator + ".config"
                + File.separator + APP_NAME_LOWER
                + File.separator + INSTALL_INFO_FILENAME);

        // 如果用户配置不存在，尝试查找系统级配置
        if (!configFile.exists()) {
            configFile = new File(File.separator + "etc"
                    + File.separator + APP_NAME_LOWER
                    + File.separator + INSTALL_INFO_FILENAME);
        }

        return readInstallDirFromFile(configFile);
    }

    /**
     * 从指定的 properties 配置文件中读取安装目录。
     *
     * @param configFile 配置文件对象。
     * @return 安装目录或 null。
     */
    private static String readInstallDirFromFile(File configFile) {
        if (configFile.exists() && configFile.isFile()) {
            Properties props = new Properties();
            try (FileInputStream fis = new FileInputStream(configFile)) {
                props.load(fis);
                String installDir = props.getProperty(INSTALL_DIR_PROPERTY_KEY);
                if (installDir == null || installDir.trim().isEmpty()) {
                    return null;
                }
                return installDir.trim();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        } else {
            return null;
        }
    }
}
