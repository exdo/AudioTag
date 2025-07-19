package xyz.idaoteng.audiotag.jaudiotagger;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.AudioHeader;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import xyz.idaoteng.audiotag.UiCoordinator;
import xyz.idaoteng.audiotag.bean.AudioFileData;
import xyz.idaoteng.audiotag.util.Utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class AudioFileReader {
    private static final String BITRATE_UNIT = " kbit/s";

    /**
     * 读取文件元数据
     *
     * @param file 文件
     * @return AudioFileData/null 文件元数据或 null
     */
    public static AudioFileData readFile(File file) {
        AudioFile audioFile;
        try {
            audioFile = AudioFileIO.read(file);
        } catch (Exception e) {
            e.printStackTrace();
            UiCoordinator.showNotification("文件：" + file.getAbsolutePath() + " 读取失败");
            return null;
        }

        AudioFileData data = new AudioFileData();
        data.setAbsolutePath(file.getAbsolutePath());
        data.setFilename(Utils.getFilenameWithoutExtension(file.getName()));
        data.setFormat(Utils.getExtension(file.getName()));
        data.setSize(getFileSize(file));

        Tag tag = audioFile.getTag();
        if (tag != null) {
            data.setTitle(tag.getFirst(FieldKey.TITLE));
            data.setArtist(tag.getFirst(FieldKey.ARTIST));
            data.setAlbum(tag.getFirst(FieldKey.ALBUM));
            data.setDate(tag.getFirst(FieldKey.YEAR));
            data.setGenre(tag.getFirst(FieldKey.GENRE));
            data.setTrack(tag.getFirst(FieldKey.TRACK));
            data.setComment(tag.getFirst(FieldKey.COMMENT));
            data.setCover(tag.getFirstArtwork() != null ? tag.getFirstArtwork().getBinaryData() : null);
            data.setLyric(tag.getFirst(FieldKey.LYRICS));
        }

        AudioHeader audioHeader = audioFile.getAudioHeader();
        // 根据 jaudiotagger 的注释 bitrate 是以 kbps 为单位，但 dsf 文件实际得到的是以 bps 为单位
        long bitRate = audioHeader.getBitRateAsNumber();
        if ("dsf".equals(Utils.getExtension(file.getName()))) {
            bitRate = Math.round(bitRate / 1000.0);
        }
        data.setBitrate(bitRate + BITRATE_UNIT);
        data.setLength(secondsToMinutes(audioHeader.getTrackLength()));

        return data;
    }

    private static String getFileSize(File file) {
        long kb = file.length() / 1024;
        if (kb < 1024) {
            return kb + "KB";
        } else {
            return kb / 1024 + "." + kb % 1024 / 100 + "MB";
        }
    }

    private static String secondsToMinutes(int seconds) {
        int minutes = seconds / 60;
        int remainingSeconds = seconds % 60;
        return String.format("%02d:%02d", minutes, remainingSeconds);
    }

    /**
     * 读取文件夹下所有文件元数据
     *
     * @param directory         文件夹
     * @param includeSubfolders 是否包含子文件夹
     * @return List<AudioFileData> 文件元数据列表
     */
    public static List<AudioFileData> readDirectory(File directory, boolean includeSubfolders) {
        if (includeSubfolders) {
            return getAudioFileDataRecursively(directory);
        } else {
            return getAudioFileData(directory);
        }
    }

    /**
     * 读取文件夹下所有文件元数据（不包含子文件夹）
     *
     * @param directory 文件夹
     * @return List<AudioFileData> 文件元数据列表
     */
    private static List<AudioFileData> getAudioFileData(File directory) {
        List<AudioFileData> dataList = new ArrayList<>();
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(directory.toPath())) {
            for (Path path : directoryStream) {
                File file = path.toFile();
                if (SupportedFile.isSupport(file)) {
                    AudioFileData data = readFile(file);
                    if (data != null) {
                        dataList.add(data);
                    }
                }
            }
        } catch (IOException e) {
            UiCoordinator.showNotification("无法读取目录：" + directory.getAbsolutePath());
            e.printStackTrace();
        }
        return dataList;
    }

    /**
     * 递归读取文件夹下所有文件元数据（包含子文件夹）
     *
     * @param directory 文件夹
     * @return List<AudioFileData> 文件元数据列表
     */
    private static List<AudioFileData> getAudioFileDataRecursively(File directory) {
        List<AudioFileData> dataList = new ArrayList<>();
        Path dirPath = directory.toPath();
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(dirPath)) {
            for (Path path : directoryStream) {
                File file = path.toFile();
                if (file.isDirectory()) {
                    dataList.addAll(getAudioFileDataRecursively(file));
                } else {
                    if (SupportedFile.isSupport(file)) {
                        AudioFileData data = readFile(file);
                        if (data != null) {
                            dataList.add(data);
                        }
                    }
                }
            }
        } catch (IOException e) {
            UiCoordinator.showNotification("无法读取目录：" + directory.getAbsolutePath());
            e.printStackTrace();
        }
        return dataList;
    }
}
