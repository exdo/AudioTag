package xyz.idaoteng.audiotag.core;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.AudioHeader;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import xyz.idaoteng.audiotag.Utils;
import xyz.idaoteng.audiotag.bean.AudioMetaData;
import xyz.idaoteng.audiotag.exception.CantReadException;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class MetaDataReader {
    private static final String BITRATE_UNIT = " kbit/s";

    public static AudioMetaData readFile(File file) throws CantReadException {
        AudioFile audioFile;
        try {
            audioFile = AudioFileIO.read(file);
        } catch (Exception e) {
            System.out.println("文件：" + file.getAbsolutePath());
            System.out.println("读取失败：" + e.getMessage());
            e.printStackTrace();
            throw new CantReadException();
        }

        AudioMetaData audioMetaData = new AudioMetaData();
        audioMetaData.setAbsolutePath(file.getAbsolutePath());
        audioMetaData.setFilename(file.getName());

        Tag tag = audioFile.getTag();
        if (tag != null) {
            audioMetaData.setTitle(tag.getFirst(FieldKey.TITLE));
            audioMetaData.setArtist(tag.getFirst(FieldKey.ARTIST));
            audioMetaData.setAlbum(tag.getFirst(FieldKey.ALBUM));
            audioMetaData.setDate(tag.getFirst(FieldKey.YEAR));
            audioMetaData.setGenre(tag.getFirst(FieldKey.GENRE));
            audioMetaData.setTrack(tag.getFirst(FieldKey.TRACK));
            String comment = tag.getFirst(FieldKey.COMMENT);
            audioMetaData.setComment(comment.replaceAll("[\\r\\n]+", " "));
            audioMetaData.setCover(tag.getFirstArtwork() != null ? tag.getFirstArtwork().getBinaryData() : null);
        }

        AudioHeader audioHeader = audioFile.getAudioHeader();
        // 根据jaudiotagger 的注释 bitrate 是以 kbps 为单位，但 dsf 文件实际得到的是以 bps 为单位
        long bitRate = audioHeader.getBitRateAsNumber();
        if ("dsf".equals(Utils.getExtension(file))) {
            bitRate = Math.round(bitRate / 1000.0);
        }
        audioMetaData.setBitrate(bitRate + BITRATE_UNIT);
        audioMetaData.setLength(Utils.secondsToMinutes(audioHeader.getTrackLength()));

        return audioMetaData;
    }

    public static List<AudioMetaData> readDirectory(File directory, boolean includeSubfolders) {
        if (includeSubfolders) {
            return getAudioMetaDataRecursively(directory);
        } else {
            return getAudioMetaData(directory);
        }
    }

    private static List<AudioMetaData> getAudioMetaData(File directory) {
        List<AudioMetaData> dataList = new ArrayList<>();
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(directory.toPath())) {
            for (Path path : directoryStream) {
                File file = path.toFile();
                if (SupportedFileTypes.isSupported(file)) {
                    try {
                        dataList.add(readFile(file));
                    } catch (CantReadException ignored) {}
                }
            }
        } catch (IOException e) {
            System.out.println("读取目录失败：" + directory.getAbsolutePath());
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return dataList;
    }

    private static List<AudioMetaData> getAudioMetaDataRecursively(File directory) {
        List<AudioMetaData> dataList = new ArrayList<>();
        Path dirPath = directory.toPath();
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(dirPath)) {
            for (Path path : directoryStream) {
                File file = path.toFile();
                if (file.isDirectory()) {
                    dataList.addAll(readSubfolders(file));
                } else {
                    if (SupportedFileTypes.isSupported(file)) {
                        try {
                            dataList.add(readFile(file));
                        } catch (CantReadException ignored) {}
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("读取目录失败：" + directory.getAbsolutePath());
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return dataList;
    }

    private static List<AudioMetaData> readSubfolders(File directory) {
        return getAudioMetaDataRecursively(directory);
    }
}
