package xyz.idaoteng.audiotag.core;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldDataInvalidException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagOptionSingleton;
import org.jaudiotagger.tag.images.StandardArtwork;
import org.jaudiotagger.tag.reference.ID3V2Version;
import xyz.idaoteng.audiotag.bean.AudioMetaData;

import java.io.File;

public class MetaDataWriter {
    static {
        // 默认使用ID3v2.4
        TagOptionSingleton.getInstance().setID3V2Version(ID3V2Version.ID3_V24);
    }

    public static void write(AudioMetaData metaData) {
        File file = new File(metaData.getAbsolutePath());
        AudioFile audioFile;
        try {
            audioFile = AudioFileIO.read(file);
            audioFile.delete();
        } catch (Exception e) {
            System.out.println("读取音频文件或删除音频文件标签失败");
            System.out.println(e.getMessage());
            e.printStackTrace();
            return;
        }

        Tag tag = audioFile.createDefaultTag();
        try {
            if (!"".equals(metaData.getTitle())) {
                tag.setField(FieldKey.TITLE, metaData.getTitle());
            }
            if (!"".equals(metaData.getArtist())) {
                tag.setField(FieldKey.ARTIST, metaData.getArtist());
            }
            if (!"".equals(metaData.getAlbum())) {
                tag.setField(FieldKey.ALBUM, metaData.getAlbum());
            }
            if (!"".equals(metaData.getDate())) {
                tag.setField(FieldKey.YEAR, metaData.getDate());
            }
            if (!"".equals(metaData.getGenre())) {
                tag.setField(FieldKey.GENRE, metaData.getGenre());
            }
            if (!"".equals(metaData.getTrack())) {
                tag.setField(FieldKey.TRACK, metaData.getTrack());
            }
            if (!"".equals(metaData.getComment())) {
                tag.setField(FieldKey.COMMENT, metaData.getComment());
            }
            if (metaData.getCover() != null) {
                tag.setField(generateArtwork(metaData.getCover()));
            }
            audioFile.setTag(tag);
        } catch (FieldDataInvalidException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }

        try {
            AudioFileIO.write(audioFile);
        } catch (Exception e) {
            System.out.println("写入标签时IO异常");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    private static StandardArtwork generateArtwork(byte[] binaryData) {
        StandardArtwork artwork = new StandardArtwork();
        artwork.setBinaryData(binaryData);
        artwork.setImageFromData();
        return artwork;
    }
}
