package xyz.idaoteng.audiotag.core;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.AudioHeader;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import xyz.idaoteng.audiotag.AudioMetaData;
import xyz.idaoteng.audiotag.Utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class MetaDataReader {
    private static final String BITRATE_UNIT = " kbit/s";

    public static AudioMetaData readFile(File file) {
        AudioFile audioFile;
        try {
            audioFile = AudioFileIO.read(file);
        } catch (Exception e) {
            System.out.println("读取文件失败");
            e.printStackTrace();
            return null;
        }

        AudioMetaData audioMetaData = new AudioMetaData();
        audioMetaData.setAbsolutePath(file.getAbsolutePath());
        audioMetaData.setFilename(file.getName());

        Tag tag = audioFile.getTag();
        if (tag != null) {
            audioMetaData.setTitle(tag.getFirst(FieldKey.TITLE));
            audioMetaData.setArtist(tag.getFirst(FieldKey.ARTIST));
            audioMetaData.setAlbum(tag.getFirst(FieldKey.ALBUM));
            audioMetaData.setGenre(tag.getFirst(FieldKey.GENRE));
            audioMetaData.setTrack(tag.getFirst(FieldKey.TRACK));
            audioMetaData.setCover(tag.getFirstArtwork() != null ? tag.getFirstArtwork().getBinaryData() : null);
        }

        AudioHeader audioHeader = audioFile.getAudioHeader();
        long bitRate = audioHeader.getBitRateAsNumber();
        if ("dsf".equals(Utils.getExtension(file))) {
            bitRate = Math.round(bitRate / 1000.0);
        }
        audioMetaData.setBitrate(bitRate + BITRATE_UNIT);
        audioMetaData.setLength(Utils.secondsToMinutes(audioHeader.getTrackLength()));

        return audioMetaData;
    }

    public static List<AudioMetaData> readDirectory(File directory) {
        return getAudioMetaData(directory);
    }

    private static List<AudioMetaData> readSubfolders(File directory) {
        return getAudioMetaData(directory);
    }

    private static List<AudioMetaData> getAudioMetaData(File directory) {
        List<AudioMetaData> dataList = new ArrayList<>();
        Path dirPath = directory.toPath();
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(dirPath)) {
            for (Path path : directoryStream) {
                File theFile = path.toFile();
                if (theFile.isDirectory()) {
                    dataList.addAll(readSubfolders(theFile));
                } else {
                    if (SupportedFileTypes.isSupported(theFile)) {
                        dataList.add(readFile(theFile));
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("读取目录失败：" + directory.getAbsolutePath());
            e.printStackTrace();
        }
        return dataList;
    }
}
